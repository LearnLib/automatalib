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
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.Pair;

/**
 * Parses a Mealy machine with alternating edge semantics from an FSM source.
 *
 * @param <I>
 *         the input type
 * @param <O>
 *         the output type
 */
public final class FSM2MealyParserAlternating<I, O> extends AbstractFSM2MealyParser<I, O> {

    public static final String PARTIAL_FSM =
            "FSM transition relation is incomplete: could not reach states '%s', from initial state '%s'";

    /**
     * A multi-map containing all outgoing transitions from a state in the FSM source.
     */
    private final Multimap<Integer, Pair<String, Integer>> transitionsFSM = ArrayListMultimap.create();

    /**
     * The alphabet in the FSM source, that consists of both input, and output.
     */
    private final Set<String> letters = new HashSet<>();

    private FSM2MealyParserAlternating(Reader reader,
                                       Function<String, I> inputParser,
                                       Function<String, O> outputParser) {
        super(reader, inputParser, outputParser);
    }

    /**
     * Parse a transition.
     *
     * @throws FSMParseException
     *         when the FSM source is invalid.
     * @throws IOException
     *         see {@link StreamTokenizer#nextToken()}.
     */
    @Override
    protected void parseTransition() throws FSMParseException, IOException {
        try {
            // check we read a state number
            if (getStreamTokenizer().nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMParseException(EXPECT_NUMBER, getStreamTokenizer());
            }

            // read the source state index
            int from = Integer.parseInt(getStreamTokenizer().sval);

            // check such a state exists
            if (!getStates().isEmpty() && !getStates().contains(from)) {
                throw new FSMParseException(String.format(NO_SUCH_STATE, from), getStreamTokenizer());
            }

            // check we read a state number
            if (getStreamTokenizer().nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMParseException(EXPECT_NUMBER, getStreamTokenizer());
            }

            // read the target state index
            int to = Integer.parseInt(getStreamTokenizer().sval);

            // check such a state exists
            if (!getStates().isEmpty() && !getStates().contains(to)) {
                throw new FSMParseException(String.format(NO_SUCH_STATE, to), getStreamTokenizer());
            }

            // check we will read an edge label
            if (getStreamTokenizer().nextToken() != '"') {
                throw new FSMParseException(EXPECT_STRING, getStreamTokenizer());
            }

            // read the letter
            final String letter = getStreamTokenizer().sval;

            // add it to the set of letters
            letters.add(letter);

            // create a transition
            final boolean isNew = transitionsFSM.put(from, Pair.of(letter, to));

            // test for non-determinism
            if (!isNew) {
                throw new FSMParseException(String.format(NON_DETERMINISM_DETECTED, from), getStreamTokenizer());
            }
        } catch (NumberFormatException | NoSuchElementException e) {
            throw new FSMParseException(e, getStreamTokenizer());
        }
    }

    /**
     * Converts all the transitions from the FSM to transitions in a
     * {@link net.automatalib.automata.transout.MealyMachine}.
     * <p>
     * This method will for each new state make transitions.
     * This is done by switching behavior between input, and output transitions in the FSM source.
     *
     * @param currentState
     *         the current state to make transitions for.
     * @param inputTrans
     *         when {@code null}, this means outgoing transitions from {@code currentState} will be output,
     *         otherwise input.
     * @param newStates
     *         the set of states that still need to be visited.
     *
     * @throws FSMParseException
     *         when non-determinism is detected.
     */
    private void makeTransitions(Integer currentState, Pair<Integer, I> inputTrans, Set<Integer> newStates)
            throws FSMParseException {

        // indicate we have seen currentState
        newStates.remove(currentState);

        // collect all outgoing transitions from currentState
        final Collection<Pair<String, Integer>> targets = transitionsFSM.get(currentState);

        // iterate over all outgoing transitions
        for (Pair<String, Integer> target : targets) {

            // the letter on the transition in the FSM source
            final String letter = target.getFirst();

            // the target state index in the FSM source
            final Integer to = target.getSecond();

            // check whether the transition is input, or output
            if (inputTrans == null) { // the transition is input
                // transform the string from the FSM source to actual input
                final I i = getInputParser().apply(letter);

                // add the input to the set of inputs
                getInputs().add(i);

                // recursive call to makeTransitions (we continue with output)
                makeTransitions(to, Pair.of(currentState, i), newStates);
            } else { // the transition is output

                // transform the string from the FSM to actual output
                final O o = getOutputParser().apply(letter);

                // create an actual Mealy machine transition
                final Pair<O, Integer> prev = getTransitions().put(inputTrans, Pair.of(o, to));

                // check for non-determinism
                if (prev != null) {
                    throw new FSMParseException(String.format(NON_DETERMINISM_DETECTED, prev), getStreamTokenizer());
                }

                // continue if we have not seen the target state yet
                if (newStates.contains(to)) {
                    makeTransitions(to, null, newStates);
                }
            }
        }
    }

    /**
     * Creates the actual Mealy machine transitions.
     *
     * @throws FSMParseException
     *         when the Mealy machine is partial.
     */
    @Override
    protected void checkTransitions() throws FSMParseException {

        // copy the set of states
        final Set<Integer> newStates = new HashSet<>(getStates());

        // retrieve the initial state in the FSM source
        final Integer initialState = getStates().iterator().next();

        // make the actual FSM transitions
        makeTransitions(initialState, null, newStates);

        // check we do not have a partial FSM
        if (!newStates.isEmpty()) {
            throw new FSMParseException(String.format(PARTIAL_FSM, newStates, initialState), getStreamTokenizer());
        }
    }

    public static <I, O> CompactMealy<I, O> parse(Reader reader,
                                                  Function<String, I> inputParser,
                                                  Function<String, O> outputParser)
            throws IOException, FSMParseException {
        return new FSM2MealyParserAlternating<>(reader, inputParser, outputParser).parseMealy();
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
