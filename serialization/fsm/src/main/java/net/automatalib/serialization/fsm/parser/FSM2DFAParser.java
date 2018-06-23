/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.serialization.fsm.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * Parses an FSM to a DFA.
 *
 * @param <I>
 *         the input type
 */
public final class FSM2DFAParser<I> extends AbstractFSMParser<I> {

    // some Exception messages
    public static final String ACCEPT_NOT_FOUND = "accepting state label (%s) not found";
    public static final String ACCEPT_VALUE_NOT_FOUND = "accepting state label value (%s) not found";
    public static final String ACCEPT_INDEX_NOT_FOUND = "index for accepting state label (%d) not found";

    /**
     * The index at which the data definition contains acceptance information.
     */
    private int acceptIndex = -1;

    /**
     * The string index indicating whether the state is accepting.
     */
    private int acceptValue = -1;

    /**
     * A sorted map of states indices, and whether they are accepting.
     */
    private final SortedMap<Integer, Boolean> states = new TreeMap<>();

    /**
     * The map of transitions. Like a {@link net.automatalib.automata.fsa.DFA}, a transition is a pair of a state
     * index, and an input, that maps to a next state index.
     */
    private final Map<Pair<Integer, I>, Integer> transitions = new HashMap<>();

    /**
     * The variable name in the data definition that tells whether the state is accepting.
     */
    private final String acceptingDataVariableName;

    /**
     * The string indicating whether the state is accepting.
     */
    private final String acceptingDataValue;

    /**
     * Constructs an FSM2DFAParser. To parse a DFA use one of the parse() methods.
     *
     * @param reader
     *         the Reader
     * @param inputParser
     *         the input parser (see {@link AbstractFSMParser#inputParser}).
     * @param acceptingDataVariableName
     *         the variable name for acceptance (see {@link #acceptingDataVariableName})
     * @param acceptingDataValue
     *         the string for acceptance (see {@link #acceptingDataValue})
     */
    private FSM2DFAParser(Reader reader,
                          Function<String, I> inputParser,
                          String acceptingDataVariableName,
                          String acceptingDataValue) {
        super(reader, inputParser);
        this.acceptingDataVariableName = acceptingDataVariableName;
        this.acceptingDataValue = acceptingDataValue;
    }

    /**
     * Parse a data definition.
     * <p>
     * This line is only parsed when we still have to find {@link #acceptIndex}, and {@link #acceptValue}.
     *
     * @throws FSMParseException
     *         when the data definition is illegal.
     * @throws IOException
     *         see {@link StreamTokenizer#nextToken()}.
     */
    @Override
    protected void parseDataDefinition() throws FSMParseException, IOException {
        if (acceptIndex == -1 && acceptValue == -1) {

            // check we will read an identifier.
            if (getStreamTokenizer().nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMParseException(EXPECT_IDENTIFIER, getStreamTokenizer());
            }

            final String dataVariableName = getStreamTokenizer().sval;

            if (dataVariableName.equals(acceptingDataVariableName)) {
                acceptIndex = getPartLineNumber();

                // skip a (
                if (getStreamTokenizer().nextToken() != '(') {
                    throw new FSMParseException(String.format(EXPECT_CHAR, '('), getStreamTokenizer());
                }

                // skip a number
                if (getStreamTokenizer().nextToken() != StreamTokenizer.TT_WORD) {
                    throw new FSMParseException(EXPECT_NUMBER, getStreamTokenizer());
                }

                // skip a )
                if (getStreamTokenizer().nextToken() != ')') {
                    throw new FSMParseException(String.format(EXPECT_CHAR, ')'), getStreamTokenizer());
                }

                // skip an identifier
                if (getStreamTokenizer().nextToken() != StreamTokenizer.TT_WORD) {
                    throw new FSMParseException(EXPECT_IDENTIFIER, getStreamTokenizer());
                }

                int dataValueIndex = 0;

                // find the string containing the acceptance information
                while (getStreamTokenizer().nextToken() == '"' && acceptValue == -1) {
                    final String dataValue = getStreamTokenizer().sval;
                    if (dataValue.equals(acceptingDataValue)) {
                        acceptValue = dataValueIndex;
                    } else {
                        dataValueIndex++;
                    }
                }
                // push back the EOL, or EOF we accidentally read
                getStreamTokenizer().pushBack();

                // throw an Exception when the string containing acceptance information is not found in the current line
                if (acceptValue == -1) {
                    throw new FSMParseException(String.format(ACCEPT_VALUE_NOT_FOUND, acceptingDataValue),
                                                getStreamTokenizer());
                }
            }
        }
    }

    /**
     * Checks the data definition by ensuring the index in the state vector containing acceptance information is
     * defined.
     *
     * @throws FSMParseException
     *         when the acceptance information could not be found.
     */
    @Override
    protected void checkDataDefinitions() throws FSMParseException {
        if (acceptIndex == -1) {
            throw new FSMParseException(String.format(ACCEPT_NOT_FOUND, acceptingDataVariableName),
                                        getStreamTokenizer());
        }
    }

    /**
     * Parse a state vector.
     * <p>
     * This method will only search for whether the state is accepting or not. The state index will be equal to the
     * current {@link #getPartLineNumber()}.
     *
     * @throws FSMParseException
     *         when the current line is an illegal state vector.
     * @throws IOException
     *         see {@link StreamTokenizer#nextToken()}.
     */
    @Override
    protected void parseStateVector() throws FSMParseException, IOException {
        Boolean accepting = null;
        for (int i = 0;
             i <= acceptIndex && getStreamTokenizer().nextToken() == StreamTokenizer.TT_WORD && accepting == null;
             i++) {
            final String value = getStreamTokenizer().sval;
            if (i == acceptIndex) {
                try {
                    accepting = acceptValue == Integer.parseInt(value);
                } catch (NumberFormatException nfe) {
                    throw new FSMParseException(nfe, getStreamTokenizer());
                }
            }
        }
        if (accepting == null) {
            throw new FSMParseException(String.format(ACCEPT_INDEX_NOT_FOUND, acceptIndex), getStreamTokenizer());
        } else {
            states.put(getPartLineNumber(), accepting);
        }
    }

    /**
     * Does nothing.
     */
    @Override
    protected void checkStateVectors() {}

    /**
     * Parse a transition by searching the current line for the source state, target state and the input.
     *
     * @throws FSMParseException
     *         when the current line is an illegal transition.
     * @throws IOException
     *         see {@link StreamTokenizer#nextToken()}.
     */
    @Override
    protected void parseTransition() throws FSMParseException, IOException {
        try {
            // check whether we will read a number
            if (getStreamTokenizer().nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMParseException(EXPECT_NUMBER, getStreamTokenizer());
            }

            // read the source state index
            int from = Integer.parseInt(getStreamTokenizer().sval);

            // check if such a state exists
            if (!states.isEmpty() && !states.containsKey(from)) {
                throw new FSMParseException(String.format(NO_SUCH_STATE, from), getStreamTokenizer());
            }

            // check whether we will read a number
            if (getStreamTokenizer().nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMParseException(EXPECT_NUMBER, getStreamTokenizer());
            }

            // read the target state
            int to = Integer.parseInt(getStreamTokenizer().sval);

            // check if such a state exists
            if (!states.isEmpty() && !states.containsKey(to)) {
                throw new FSMParseException(String.format(NO_SUCH_STATE, to), getStreamTokenizer());
            }

            // check we will read a string
            if (getStreamTokenizer().nextToken() != '"') {
                throw new FSMParseException(EXPECT_STRING, getStreamTokenizer());
            }

            // read the input on the transition
            final I input = getInputParser().apply(getStreamTokenizer().sval);

            // add it to the set of inputs
            getInputs().add(input);

            // add the new transition
            final Integer prev = transitions.put(Pair.of(from, input), to);
            if (prev != null) {
                throw new FSMParseException(String.format(NON_DETERMINISM_DETECTED, prev), getStreamTokenizer());
            }
        } catch (NumberFormatException nfe) {
            throw new FSMParseException(nfe, getStreamTokenizer());
        }
    }

    /**
     * Do nothing.
     */
    @Override
    protected void checkTransitions() {}

    /**
     * Constructs the actual {@link net.automatalib.automata.fsa.DFA}.
     *
     * @return the DFA represented by the FSM file.
     *
     * @throws FSMParseException
     *         see {@link #parse()}.
     * @throws IOException
     *         see {@link #parse()}.
     */
    private CompactDFA<I> parseDFA() throws FSMParseException, IOException {

        parse();

        final Alphabet<I> alphabet = Alphabets.fromCollection(getInputs());
        final CompactDFA<I> dfa = new CompactDFA<>(alphabet);

        // add all the states
        states.forEach((key, value) -> dfa.addState(value));

        // set the initial state (substract one because the StreamTokenizer starts counting at 1
        dfa.setInitialState(states.firstKey() - 1);

        // add all the transitions
        for (Map.Entry<Pair<Integer, I>, Integer> transition : transitions.entrySet()) {
            dfa.addTransition(transition.getKey().getFirst() - 1,
                              transition.getKey().getSecond(),
                              transition.getValue() - 1);
        }

        return dfa;
    }

    public static <I> CompactDFA<I> parse(Reader reader,
                                          Function<String, I> inputParser,
                                          String acceptingDataVariableName,
                                          String acceptingDataValue) throws IOException, FSMParseException {
        return new FSM2DFAParser<>(reader, inputParser, acceptingDataVariableName, acceptingDataValue).parseDFA();
    }

    public static <I> CompactDFA<I> parse(File file,
                                          Function<String, I> inputParser,
                                          String acceptingDataVariableName,
                                          String acceptingDataValue) throws IOException, FSMParseException {
        return parse(IOUtil.asBufferedUTF8Reader(file), inputParser, acceptingDataVariableName, acceptingDataValue);
    }

    public static <I> CompactDFA<I> parse(String string,
                                          Function<String, I> inputParser,
                                          String acceptingDataVariableName,
                                          String acceptingDataValue) throws IOException, FSMParseException {
        return parse(new StringReader(string), inputParser, acceptingDataVariableName, acceptingDataValue);
    }

    public static <I> CompactDFA<I> parse(InputStream inputStream,
                                          Function<String, I> inputParser,
                                          String acceptingDataVariableName,
                                          String acceptingDataValue) throws IOException, FSMParseException {
        return parse(IOUtil.asBufferedUTF8Reader(inputStream),
                     inputParser,
                     acceptingDataVariableName,
                     acceptingDataValue);
    }
}
