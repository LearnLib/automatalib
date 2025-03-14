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
package net.automatalib.serialization.fsm.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.Pair;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.ModelDeserializer;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An FSM parser for Mealy machines. It provides general functionality for both Mealy machines with straightforward edge
 * semantics, and alternating edge semantics.
 *
 * @param <I>
 *         the input type.
 * @param <O>
 *         the output type.
 * @param <A>
 *         the parsed automaton type
 */
public abstract class AbstractFSM2MealyParser<I, O, A extends MutableMealyMachine<Integer, I, ?, O>> extends AbstractFSMParser<I>
        implements ModelDeserializer<A> {

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
     * The creator for the returned automaton.
     */
    private final AutomatonCreator<A, I> creator;

    /**
     * Constructs a new AbstractFSM2MealyParser.
     *
     * @param targetInputs
     *         A collection containing the inputs which should constitute the input alphabet of the parsed automaton. If
     *         {@code null}, the inputs will be automatically gathered from the read FSM file
     * @param inputParser
     *         the input parser
     * @param outputParser
     *         the output parser (similar to {@code inputParser})
     * @param creator
     *         the creator to construct the returned automaton instance
     *
     */
    protected AbstractFSM2MealyParser(@Nullable Collection<? extends I> targetInputs,
                                      Function<String, I> inputParser,
                                      Function<String, O> outputParser,
                                      AutomatonCreator<A, I> creator) {
        super(targetInputs, inputParser);
        this.outputParser = outputParser;
        this.creator = creator;
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
     * We do not care about data definitions.
     */
    @Override
    protected void parseDataDefinition(StreamTokenizer streamTokenizer) {}

    /**
     * We do not need to check data definitions.
     */
    @Override
    protected void checkDataDefinitions(StreamTokenizer streamTokenizer) {}

    /**
     * Parse a state vector by simply recording the line number in the current part.
     */
    @Override
    protected void parseStateVector(StreamTokenizer streamTokenizer) {
        getStates().add(getPartLineNumber());
    }

    /**
     * We do not check the state vectors.
     */
    @Override
    protected void checkStateVectors(StreamTokenizer streamTokenizer) {}

    /**
     * Constructs the actual {@link MealyMachine}, using {@link #states}, and {@link #transitions}.
     *
     * @param reader
     *         the reader providing the contents of the FSM file
     *
     * @return the Mealy machine defined in the FSM source.
     *
     * @throws FSMFormatException
     *         (see {@link #parse(Reader)}).
     * @throws IOException
     *         (see {@link #parse(Reader)}).
     */
    protected A parseMealy(Reader reader) throws IOException, FormatException {

        parse(reader);

        // create the alphabet
        final Alphabet<I> alphabet;

        if (targetInputs != null) {
            alphabet = Alphabets.fromCollection(targetInputs);
        } else {
            alphabet = Alphabets.fromCollection(getInputs());
        }

        // create a CompactMealy
        final A mealy = creator.createAutomaton(alphabet);

        // create a mapping states in the FSM source to states in the CompactMealy
        final Map<Integer, Integer> stateMap = new HashMap<>();

        // set the initial state
        mealy.setInitialState(stateMap.computeIfAbsent(states.iterator().next(), i -> mealy.addState()));

        // iterate over all transitions, add them to the CompactMealy
        getTransitions().entrySet().stream().filter(e -> alphabet.containsSymbol(e.getKey().getSecond())).forEach(e -> {
            final Integer from = stateMap.computeIfAbsent(e.getKey().getFirst(), i -> mealy.addState());
            final Integer to = stateMap.computeIfAbsent(e.getValue().getSecond(), i -> mealy.addState());

            final I i = e.getKey().getSecond();
            final O o = e.getValue().getFirst();

            mealy.addTransition(from, i, to, o);
        });

        // clean our state for next parse call
        states.clear();
        transitions.clear();

        return mealy;
    }

    @Override
    public A readModel(InputStream is) throws IOException, FormatException {
        try (Reader r = IOUtil.asNonClosingUTF8Reader(is)) {
            return parseMealy(r);
        }
    }
}
