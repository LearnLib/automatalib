/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.serialization.aut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.IOUtil;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import org.checkerframework.checker.nullness.qual.Nullable;

class InternalAUTParser<I, T, @Nullable TP, A extends MutableAutomaton<Integer, I, T, ?, TP>>
        implements InputModelDeserializer<I, A> {

    private final Function<String, I> inputTransformer;
    private final AutomatonCreator<A, I> creator;
    private final Set<String> alphabetSymbols;
    private final Map<Integer, Map<String, Set<Integer>>> transitionMap;

    private int initialState;
    private int numStates;
    private int currentLine;
    private int currentPos;

    InternalAUTParser(Function<String, I> inputTransformer, AutomatonCreator<A, I> creator) {
        this.inputTransformer = inputTransformer;
        this.creator = creator;
        this.alphabetSymbols = new HashSet<>();
        this.transitionMap = new HashMap<>();
    }

    @Override
    public InputModelData<I, A> readModel(InputStream is) throws IOException, FormatException {

        try (BufferedReader br = new BufferedReader(IOUtil.asNonClosingUTF8Reader(is))) {
            // parsing
            parseHeader(br);
            while (parseTransition(br)) {
                // consume transitions
            }

            // automaton construction
            final Map<String, I> inputMap = new HashMap<>(HashUtil.capacity(this.alphabetSymbols.size()));
            for (String s : this.alphabetSymbols) {
                inputMap.put(s, inputTransformer.apply(s));
            }
            final Alphabet<I> alphabet = Alphabets.fromCollection(inputMap.values());

            final A result = creator.createAutomaton(alphabet, numStates);

            for (int i = 0; i < numStates; i++) {
                result.addState();
            }

            for (Map.Entry<Integer, Map<String, Set<Integer>>> outgoing : transitionMap.entrySet()) {
                final Integer src = outgoing.getKey();
                for (Map.Entry<String, Set<Integer>> targets : outgoing.getValue().entrySet()) {
                    final String input = targets.getKey();
                    final Set<Integer> tgts = targets.getValue();
                    for (Integer tgt : tgts) {
                        result.addTransition(src, inputMap.get(input), tgt, null);
                    }
                }
            }
            result.setInitial(initialState, true);

            return new InputModelData<>(result, alphabet);
        } finally {
            // cleanup
            initialState = 0;
            numStates = 0;
            currentLine = 0;
            currentPos = 0;
            alphabetSymbols.clear();
            transitionMap.clear();
        }
    }

    private void parseHeader(BufferedReader reader) throws IOException, FormatException {
        final String line = reader.readLine();

        if (line == null) {
            throw new FormatException(buildErrorMessage("Missing description"));
        }

        char[] currentLineContent = line.toCharArray();
        currentPos = 0;

        shiftToNextNonWhitespace(currentLineContent);
        verifyDesAndShift(currentLineContent);
        verifyLBracketAndShift(currentLineContent);
        initialState = parseNumberAndShift(currentLineContent);
        verifyCommaAndShift(currentLineContent);
        parseNumberAndShift(currentLineContent); // ignore number of transitions
        verifyCommaAndShift(currentLineContent);
        numStates = parseNumberAndShift(currentLineContent); // store number of states
        if (numStates < 1) {
            throw new FormatException("Number of states must be >= 1");
        }
        verifyRBracketAndShift(currentLineContent);
    }

    private boolean parseTransition(BufferedReader reader) throws IOException, FormatException {
        final String line = reader.readLine();

        if (line == null) {
            return false;
        }

        char[] currentLineContent = line.toCharArray();
        currentLine++;
        currentPos = 0;

        final int start;
        final String label;
        final int dest;

        shiftToNextNonWhitespace(currentLineContent);
        verifyLBracketAndShift(currentLineContent);
        start = parseNumberAndShift(currentLineContent);
        verifyCommaAndShift(currentLineContent);
        label = parseLabelAndShift(currentLineContent);
        verifyCommaAndShift(currentLineContent);
        dest = parseNumberAndShift(currentLineContent);
        verifyRBracketAndShift(currentLineContent);

        alphabetSymbols.add(label);
        transitionMap.computeIfAbsent(start, k -> new HashMap<>())
                     .computeIfAbsent(label, k -> new HashSet<>())
                     .add(dest);

        return true;
    }

    private void verifyDesAndShift(char[] currentLineContent) throws FormatException {

        if (currentLineContent[currentPos] != 'd' || currentLineContent[currentPos + 1] != 'e' ||
            currentLineContent[currentPos + 2] != 's') {
            throw new FormatException(buildErrorMessage("Missing 'des' keyword"));
        }

        currentPos += 3;
        shiftToNextNonWhitespace(currentLineContent);
    }

    private void verifyLBracketAndShift(char[] currentLineContent) throws FormatException {
        verifySymbolAndShift(currentLineContent, '(');
    }

    private void verifyRBracketAndShift(char[] currentLineContent) throws FormatException {
        verifySymbolAndShift(currentLineContent, ')');
    }

    private void verifyCommaAndShift(char[] currentLineContent) throws FormatException {
        verifySymbolAndShift(currentLineContent, ',');
    }

    private void verifySymbolAndShift(char[] currentLineContent, char symbol) throws FormatException {

        if (currentLineContent[currentPos] != symbol) {
            throw new FormatException(buildErrorMessage("Expected: " + symbol));
        }

        currentPos++;
        shiftToNextNonWhitespace(currentLineContent);
    }

    private void shiftToNextNonWhitespace(char[] currentLineContent) {
        while (currentPos < currentLineContent.length && Character.isWhitespace(currentLineContent[currentPos])) {
            currentPos++;
        }
    }

    private int parseNumberAndShift(char[] currentLineContent) throws FormatException {

        final StringBuilder sb = new StringBuilder();

        char sym = currentLineContent[currentPos];

        while (Character.isDigit(sym)) {
            sb.append(sym);
            currentPos++;
            sym = currentLineContent[currentPos];
        }

        if (sb.length() == 0) {
            throw new FormatException(buildErrorMessage("Expected a positive number"));
        }

        // forward pointer
        shiftToNextNonWhitespace(currentLineContent);
        return Integer.parseInt(sb.toString());
    }

    private String parseLabelAndShift(char[] currentLineContent) throws FormatException {

        if (currentLineContent[currentPos] == '"') {
            return parseQuotedLabelAndShift(currentLineContent);
        } else {
            return parseNormalLabelAndShift(currentLineContent);
        }
    }

    private String parseQuotedLabelAndShift(char[] currentLineContent) {
        int openingIndex = currentPos;
        int closingIndex = currentLineContent.length - 1;

        // find terminating "
        while (currentLineContent[closingIndex--] != '"') {
            // consume characters
        }

        // skip terminating " as well
        currentPos = closingIndex + 2;
        shiftToNextNonWhitespace(currentLineContent);

        return new String(currentLineContent, openingIndex + 1, closingIndex - openingIndex);
    }

    private String parseNormalLabelAndShift(char[] currentLineContent) throws FormatException {

        final char firstChar = currentLineContent[currentPos];

        if (currentLineContent[currentPos] == '*') {
            currentPos++;
            shiftToNextNonWhitespace(currentLineContent);
            return "*";
        } else if (Character.isLetter(firstChar)) {
            int startIdx = currentPos;

            while (isValidIdentifier(currentLineContent)) {
                currentPos++;
            }

            int endIdx = currentPos;

            shiftToNextNonWhitespace(currentLineContent);
            return new String(currentLineContent, startIdx, endIdx - startIdx);
        } else {
            throw new FormatException(buildErrorMessage("Invalid unquoted label"));
        }
    }

    private boolean isValidIdentifier(char[] currentLineContent) {
        final char currentChar = currentLineContent[currentPos];
        return Character.isLetterOrDigit(currentChar) || currentChar == '_';
    }

    private String buildErrorMessage(String desc) {
        return "In line " + currentLine + ", col " + currentPos + ": " + desc;
    }
}
