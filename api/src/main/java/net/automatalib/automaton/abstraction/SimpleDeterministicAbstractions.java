/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.automaton.abstraction;

import java.util.function.IntFunction;

import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.simple.SimpleDeterministicAutomaton;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstractions for {@link SimpleDeterministicAutomaton}s.
 */
public interface SimpleDeterministicAbstractions {

    /**
     * Basic interface for integer abstractions of automata. In an integer abstraction, each state of an automaton is
     * identified with an integer in the range {@code [0, size() - 1]}. A similar abstraction may be imposed on the
     * input symbols, this is however not prescribed by this interface (see {@link StateIntAbstraction} and
     * {@link FullIntAbstraction}).
     */
    interface IntAbstraction extends FiniteRepresentation {

        /**
         * Representative for an invalid state. This is the value being returned by methods that would return
         * {@code null} in their non-abstracted version. However, for determining whether a state is valid or not, code
         * should never rely on the corresponding integer being equal to this value, since any integer outside the range
         * {@code [0, size() - 1]} is invalid, in particular all negative integers.
         */
        int INVALID_STATE = -1;

        /**
         * Retrieves the initial state of the (abstracted) automaton as an integer. If the automaton has no initial
         * state, {@link #INVALID_STATE} is returned.
         *
         * @return the integer representing the initial state, or {@link #INVALID_STATE}.
         */
        int getIntInitialState();

    }

    /**
     * Interface for {@link IntAbstraction integer abstractions} of an automaton that operate on non-abstracted input
     * symbols (i.e., input symbols are of type {@code I}).
     *
     * @param <I>
     *         input symbol type
     */
    interface StateIntAbstraction<I> extends IntAbstraction {

        /**
         * Retrieves the (abstracted) successor state for a given (abstracted) source state and input symbol.
         *
         * @param state
         *         the integer representing the source state
         * @param input
         *         the input symbol
         *
         * @return the integer representing the successor state, or {@link IntAbstraction#INVALID_STATE} if there is no
         * successor state.
         */
        int getSuccessor(int state, I input);

    }

    /**
     * Interface for an {@link IntAbstraction integer abstraction} that abstracts both states and input symbols to
     * integers. In addition to the modalities specified in {@link IntAbstraction}, this interface prescribes that input
     * symbols are abstracted to integers in the range {@code [0, numInputs() - 1]}.
     */
    interface FullIntAbstraction extends IntAbstraction {

        /**
         * Retrieves the (abstracted) successor for a given (abstracted) source state and (abstracted) input.
         *
         * @param state
         *         the integer representing the source state
         * @param input
         *         the integer representing the input symbol
         *
         * @return the integer representing the target state, or {@link IntAbstraction#INVALID_STATE} if there is no
         * successor state.
         */
        int getSuccessor(int state, int input);

        /**
         * Retrieves the number of input symbols. This determines the valid range of input symbols, which is
         * {@code [0, numInputs() - 1]}.
         *
         * @return the number of input symbols
         */
        int numInputs();

    }

    /**
     * Base class implementing the default way of obtaining an integer abstraction from an automaton, i.e., by mapping
     * states to integers and vice versa using the {@link StateIDs} mapping obtained via
     * {@link SimpleDeterministicAutomaton#stateIDs()}.
     *
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     */
    class IntAbstractionImpl<S, A extends SimpleDeterministicAutomaton<S, ?>> implements IntAbstraction {

        protected final A automaton;
        protected final StateIDs<S> stateIds;

        public IntAbstractionImpl(A automaton) {
            this.automaton = automaton;
            this.stateIds = automaton.stateIDs();
        }

        @Override
        public int size() {
            return automaton.size();
        }

        protected final S intToState(int stateId) {
            return stateIds.getState(stateId);
        }

        protected final @Nullable S safeIntToState(int stateId) {
            return (stateId == INVALID_STATE) ? null : intToState(stateId);
        }

        @Override
        public int getIntInitialState() {
            return safeStateToInt(automaton.getInitialState());
        }

        protected final int stateToInt(S state) {
            return stateIds.getStateId(state);
        }

        protected final int safeStateToInt(@Nullable S state) {
            return (state == null) ? INVALID_STATE : stateToInt(state);
        }
    }

    /**
     * Base class implementing the default way of obtaining a {@link StateIntAbstraction}.
     *
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <A>
     *         automaton type
     *
     * @see IntAbstractionImpl
     */
    class StateIntAbstractionImpl<S, I, A extends SimpleDeterministicAutomaton<S, I>>
            extends IntAbstractionImpl<S, A> implements StateIntAbstraction<I> {

        public StateIntAbstractionImpl(A automaton) {
            super(automaton);
        }

        @Override
        public int getSuccessor(int state, I input) {
            return safeStateToInt(automaton.getSuccessor(intToState(state), input));
        }
    }

    /**
     * Base class implementing the default way of obtaining a {@link FullIntAbstractionImpl}, i.e., building on top of a
     * {@link StateIntAbstractionImpl} and a mapping from integers to (concrete) input symbols.
     *
     * @param <I>
     *         input symbol type
     * @param <A>
     *         state abstraction type
     */
    class FullIntAbstractionImpl<I, A extends StateIntAbstraction<I>> implements FullIntAbstraction {

        protected final A stateAbstraction;
        protected final int numInputs;
        protected final IntFunction<? extends I> symMapping;

        public FullIntAbstractionImpl(A stateAbstraction, int numInputs, IntFunction<? extends I> symMapping) {
            this.stateAbstraction = stateAbstraction;
            this.numInputs = numInputs;
            this.symMapping = symMapping;
        }

        @Override
        public int getSuccessor(int state, int input) {
            return stateAbstraction.getSuccessor(state, intToSym(input));
        }

        @Override
        public int numInputs() {
            return numInputs;
        }

        protected final I intToSym(int input) {
            return symMapping.apply(input); // TODO range checks?
        }

        @Override
        public int size() {
            return stateAbstraction.size();
        }

        @Override
        public int getIntInitialState() {
            return stateAbstraction.getIntInitialState();
        }
    }
}
