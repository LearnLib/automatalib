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
import java.util.function.Function;

import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.Pair;

/**
 * Parse a Mealy machine from an FSM source, with straightforward edge semantics.
 *
 * @param <I>
 *         the input type
 * @param <O>
 *         the output type
 *
 * @author Jeroen Meijer
 */
public final class FSM2MealyParserIO<I, O> extends AbstractFSM2MealyParser<I, O> {

    /**
     * Constructs a new FSM2MealyParserIO. Use one of the static parse() methods to actually parse an FSM source.
     *
     * @param reader
     *         the Reader.
     * @param inputParser
     *         the input parser (see {@link #inputParser}).
     * @param outputParser
     *         the output parser (similar to {@code inputParser}.
     */
    private FSM2MealyParserIO(Reader reader, Function<String, I> inputParser, Function<String, O> outputParser) {
        super(reader, inputParser, outputParser);
    }

    /**
     * Parse a transition.
     *
     * @throws FSMParseException
     *         when the transition is illegal.
     * @throws IOException
     *         see {@link StreamTokenizer#nextToken()}.
     */
    @Override
    protected void parseTransition() throws FSMParseException, IOException {
        try {

            // check we will read a state index
            if (getStreamTokenizer().nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMParseException(EXPECT_NUMBER, getStreamTokenizer());
            }

            // read the source state index
            int from = Integer.parseInt(getStreamTokenizer().sval);

            // check if such a state exists
            if (!getStates().isEmpty() && !getStates().contains(from)) {
                throw new FSMParseException(String.format(NO_SUCH_STATE, from), getStreamTokenizer());
            }

            // check we will read a state index
            if (getStreamTokenizer().nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMParseException(EXPECT_NUMBER, getStreamTokenizer());
            }

            // read the target state index
            int to = Integer.parseInt(getStreamTokenizer().sval);

            // check if such a state exists
            if (!getStates().isEmpty() && !getStates().contains(to)) {
                throw new FSMParseException(String.format(NO_SUCH_STATE, to), getStreamTokenizer());
            }

            // check we will read an input edge label
            if (getStreamTokenizer().nextToken() != '"') {
                throw new FSMParseException(EXPECT_STRING, getStreamTokenizer());
            }

            // read the input, and convert the input string to actual input
            final I input = getInputParser().apply(getStreamTokenizer().sval);

            // add it to the set of inputs
            getInputs().add(input);

            // check we will read an output edge label
            if (getStreamTokenizer().nextToken() != '"') {
                throw new FSMParseException(EXPECT_STRING, getStreamTokenizer());
            }

            // read the output, and convert the output string to actual output
            final O output = getOutputParser().apply(getStreamTokenizer().sval);

            // create the Mealy machine transition
            final Pair<O, Integer> prev = getTransitions().put(Pair.of(from, input), Pair.of(output, to));

            // check for non-determinism
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

    public static <I, O> CompactMealy<I, O> parse(Reader reader,
                                                  Function<String, I> inputParser,
                                                  Function<String, O> outputParser)
            throws IOException, FSMParseException {
        return new FSM2MealyParserIO<>(reader, inputParser, outputParser).parseMealy();
    }

    public static <I, O> CompactMealy<I, O> parse(File file,
                                                  Function<String, I> inputParser,
                                                  Function<String, O> outputParser)
            throws IOException, FSMParseException {
        return parse(IOUtil.asBufferedUTF8Reader(file), inputParser, outputParser);
    }

    public static <I, O> CompactMealy<I, O> parse(String string,
                                                  Function<String, I> inputParser,
                                                  Function<String, O> outputParser)
            throws IOException, FSMParseException {
        return parse(new StringReader(string), inputParser, outputParser);
    }

    public static <I, O> CompactMealy<I, O> parse(InputStream inputStream,
                                                  Function<String, I> inputParser,
                                                  Function<String, O> outputParser)
            throws IOException, FSMParseException {
        return parse(IOUtil.asBufferedUTF8Reader(inputStream), inputParser, outputParser);
    }

    public static <E> CompactMealy<E, E> parse(Reader reader, Function<String, E> edgeParser)
            throws IOException, FSMParseException {
        return parse(reader, edgeParser, edgeParser);
    }

    public static <E> CompactMealy<E, E> parse(File file, Function<String, E> edgeParser)
            throws IOException, FSMParseException {
        return parse(file, edgeParser, edgeParser);
    }

    public static <E> CompactMealy<E, E> parse(String string, Function<String, E> edgeParser)
            throws IOException, FSMParseException {
        return parse(string, edgeParser, edgeParser);
    }

    public static <E> CompactMealy<E, E> parse(InputStream inputStream, Function<String, E> edgeParser)
            throws IOException, FSMParseException {
        return parse(inputStream, edgeParser, edgeParser);
    }
}
