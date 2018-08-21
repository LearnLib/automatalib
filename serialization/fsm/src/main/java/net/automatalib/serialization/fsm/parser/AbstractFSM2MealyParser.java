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

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * An FSM parser for Mealy machines. It provides general functionality for both Mealy machines with straightforward edge
 * semantics, as well as alternating edge semantics.
 *
 * @param <I> the input type.
 * @param <O> the output type.
 */
public abstract class AbstractFSM2MealyParser<I, O> extends AbstractFSMParser<I> {

    /**
     * A Function that transform strings from the FSM source to actual output.
     */
    private final Function<String, O> outputParser;

    /**
     * A map of transitions for the Mealy machine.
     */
    private final Map<Pair<Integer, I>, Pair<O, Integer>> transitions = new HashMap<>();

    /**
     * A sorted set of states for the Mealy machine.
     */
    private final SortedSet<Integer> states = new TreeSet<>();

    /**
     * Constructs a new AbstractFSM2MealyParser.
     *
     * @param reader the reader
     * @param inputParser the input parser (see {@link #inputParser}).
     * @param outputParser the output parser (similar to {@code inputParser}).
     */
    protected AbstractFSM2MealyParser(Reader reader, Function<String, I> inputParser, Function<String, O> outputParser) {
        super(reader, inputParser);
        this.outputParser = outputParser;
    }

    /**
     * Gets the Function that transforms strings from the FSM source to actual output.
     *
     * @return the Function.
     */
    protected Function<String, O> getOutputParser() {
        return outputParser;
    }

    /**
     * Gets the map of transitions for the Mealy machine.
     *
     * @return the Map.
     */
    protected Map<Pair<Integer, I>, Pair<O, Integer>> getTransitions() {
        return transitions;
    }

    /**
     * Gets the sorted set of states for the Mealy machine.
     *
     * @return the SortedSet.
     */
    protected SortedSet<Integer> getStates() {
        return states;
    }

    /**
     * We don not care about data definitions.
     */
    @Override
    protected void parseDataDefinition() {}

    /**
     * We do not need to check data definitions.
     */
    @Override
    protected void checkDataDefinitions() {}

    /**
     * Parse a state vector by simply recording the line number in the current part.
     */
    @Override
    protected void parseStateVector() {
        getStates().add(getPartLineNumber());
    }

    /**
     * We do not check the state vectors.
     */
    @Override
    protected void checkStateVectors() {}

    /**
     * Constructs the actual {@link net.automatalib.automata.transout.MealyMachine}, using {@link #states}, and
     * {@link #transitions}.
     *
     * @return the Mealy machine defined in the FSM source.
     *
     * @throws FSMParseException (see {@link #parse()}).
     * @throws IOException (see {@link #parse()}).
     */
    protected CompactMealy<I, O> parseMealy() throws FSMParseException, IOException {

        parse();

        // create the alphabet
        final Alphabet<I> alphabet = Alphabets.fromCollection(getInputs());

        // create a CompactMealy
        final CompactMealy<I, O> mealy = new CompactMealy<>(alphabet);

        // create a mapping states in the FSM source to states in the CompactMealy
        final Map<Integer, Integer> stateMap = new HashMap<>();

        // set the initial state
        mealy.setInitialState(stateMap.computeIfAbsent(states.iterator().next(), i -> mealy.addState()));

        // iterate over all transitions, add them to the CompactMealy
        for (Map.Entry<Pair<Integer, I>, Pair<O, Integer>> transition : getTransitions().entrySet()) {
            final Integer from = stateMap.computeIfAbsent(transition.getKey().getFirst(), i -> mealy.addState());
            final Integer to = stateMap.computeIfAbsent(transition.getValue().getSecond(), i -> mealy.addState());

            final I i = transition.getKey().getSecond();
            final O o = transition.getValue().getFirst();

            mealy.addTransition(from, i, to, o);
        }

        return mealy;
    }
}
