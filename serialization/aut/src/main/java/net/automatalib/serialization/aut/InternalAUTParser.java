/* Copyright (C) 2013-2017 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
class InternalAUTParser {

    private int initialState;
    private Set<String> alphabetSymbols = new HashSet<>();
    private Map<Integer, Map<String, Integer>> transitionMap = new HashMap<>();

    private final InputStream inputStream;

    private char[] contents;
    private int currentLine;
    private int currentPos;

    InternalAUTParser(InputStream is) {
        this.inputStream = is;
    }

    public <I> Automaton<Integer, I, ?> parse(Function<String, I> inputTransformer) throws IOException {
        try (Reader isr = new InputStreamReader(inputStream); BufferedReader bisr = new BufferedReader(isr)) {

            // parsing
            parseHeader(bisr);
            while (parseTransition(bisr)) {}

            // hypothesis construction
            final Map<String, I> inputMap =
                    alphabetSymbols.stream().collect(Collectors.toMap(Function.identity(), inputTransformer));
            final Alphabet<I> alphabet = Alphabets.fromCollection(inputMap.values());

            final CompactNFA<I> result = new CompactNFA<>(alphabet, transitionMap.size());

            for (int i = 0; i < transitionMap.size(); i++) {
                result.addState();
            }

            for (final Map.Entry<Integer, Map<String, Integer>> outgoing : transitionMap.entrySet()) {
                final Integer src = outgoing.getKey();
                for (final Map.Entry<String, Integer> targets : outgoing.getValue().entrySet()) {
                    final String input = targets.getKey();
                    final Integer dest = targets.getValue();
                    result.addTransition(src, inputMap.get(input), dest);
                }
            }
            result.setInitial(initialState, true);

            return result;
        }
    }

    private void parseHeader(BufferedReader reader) throws IOException {
        final String line = reader.readLine();

        if (line == null) {
            throw new IllegalArgumentException(buildErrorMessage("Missing description"));
        }

        contents = line.toCharArray();
        currentPos = 0;

        shiftToNextNonWhitespace();
        verifyDesAndShift();
        verifyLBracketAndShift();
        initialState = parseNumberAndShift();
        verifyCommaAndShift();
        parseNumberAndShift(); // ignore number of states
        verifyCommaAndShift();
        parseNumberAndShift(); // ignore number of transitions
        verifyRBracketAndShift();
    }

    private boolean parseTransition(BufferedReader reader) throws IOException {
        final String line = reader.readLine();

        if (line == null) {
            return false;
        }

        contents = line.toCharArray();
        currentLine++;
        currentPos = 0;

        final int start;
        final String label;
        final int dest;

        shiftToNextNonWhitespace();
        verifyLBracketAndShift();
        start = parseNumberAndShift();
        verifyCommaAndShift();
        label = parseLabelAndShift();
        verifyCommaAndShift();
        dest = parseNumberAndShift();
        verifyRBracketAndShift();

        alphabetSymbols.add(label);
        transitionMap.computeIfAbsent(start, k -> new HashMap<>()).put(label, dest);

        return true;
    }

    private void verifyDesAndShift() {

        shiftToNextNonWhitespace();

        if (contents[currentPos] != 'd' || contents[currentPos + 1] != 'e' || contents[currentPos + 2] != 's') {
            throw new IllegalArgumentException(buildErrorMessage("Missing 'des' keyword"));
        }

        currentPos += 3;
        shiftToNextNonWhitespace();
    }

    private void verifyLBracketAndShift() {
        verifySymbolAndShift('(');
    }

    private void verifyRBracketAndShift() {
        verifySymbolAndShift(')');
    }

    private void verifyCommaAndShift() {
        verifySymbolAndShift(',');
    }

    private void verifySymbolAndShift(char symbol) {

        if (contents[currentPos] != symbol) {
            throw new IllegalArgumentException(buildErrorMessage("Expected: " + symbol));
        }

        currentPos++;
        shiftToNextNonWhitespace();
    }

    private void shiftToNextNonWhitespace() {
        for (int i = currentPos; i < contents.length; i++) {
            switch (contents[i]) {
                case ' ':
                case '\t':
                case '\r': // probably already filtered by readline
                case '\n': // probably already filtered by readline
                    break;
                default:
                    currentPos = i;
                    return;
            }
        }
    }

    private int parseNumberAndShift() {

        final StringBuilder sb = new StringBuilder();

        char sym = contents[currentPos];

        while (Character.isDigit(sym)) {
            sb.append(sym);
            currentPos++;
            sym = contents[currentPos];
        }

        // forward pointer
        shiftToNextNonWhitespace();
        return Integer.parseInt(sb.toString());
    }

    private String parseLabelAndShift() {

        if (contents[currentPos] == '"') {
            return parseQuotedLabelAndShift();
        } else {
            return parseNormalLabelAndShift();
        }
    }

    private String parseQuotedLabelAndShift() {
        int openingIndex = currentPos;
        int closingIndex = contents.length - 1;

        // find terminating "
        while (contents[closingIndex--] != '"') {}

        // skip terminating " as well
        currentPos = closingIndex + 2;
        shiftToNextNonWhitespace();

        return new String(contents, openingIndex + 1, closingIndex - openingIndex);
    }

    private String parseNormalLabelAndShift() {

        final char firstChar = contents[currentPos];

        if (contents[currentPos] == '*') {
            currentPos++;
            shiftToNextNonWhitespace();
            return "*";
        } else if (Character.isLetter(firstChar)) {
            int startIdx = currentPos;

            while (isValidIdentifier()) {
                currentPos++;
            }

            int endIdx = currentPos;

            shiftToNextNonWhitespace();
            return new String(contents, startIdx, endIdx - startIdx);
        } else {
            throw new IllegalArgumentException(buildErrorMessage("Invalid unquoted label"));
        }
    }

    private boolean isValidIdentifier() {
        final char currentChar = contents[currentPos];
        return Character.isLetterOrDigit(currentChar) || currentChar == '_';
    }

    private String buildErrorMessage(String desc) {
        return "In line " + currentLine + ", col " + currentPos + ": " + desc;
    }

}
