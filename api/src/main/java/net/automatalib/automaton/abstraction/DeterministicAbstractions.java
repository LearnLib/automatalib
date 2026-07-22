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

import net.automatalib.automaton.DeterministicAutomaton;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstractions for {@link DeterministicAutomaton}s.
 */
public interface DeterministicAbstractions {

    /**
     * Base interface for {@link SimpleDeterministicAbstractions.IntAbstraction integer abstractions} of a
     * {@link DeterministicAutomaton}.
     *
     * @param <T>
     *         transition type
     */
    interface IntAbstraction<T> extends SimpleDeterministicAbstractions.IntAbstraction {

        /**
         * Retrieves the (abstracted) successor of a transition object.
         *
         * @param transition
         *         the transition object
         *
         * @return the integer representing the successor of the given transition
         */
        int getIntSuccessor(T transition);
    }

    /**
     * Interface for {@link SimpleDeterministicAbstractions.StateIntAbstraction state integer abstractions} of a
     * {@link DeterministicAutomaton}.
     *
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     */
    interface StateIntAbstraction<I, T>
            extends IntAbstraction<T>, SimpleDeterministicAbstractions.StateIntAbstraction<I> {

        @Override
        default int getSuccessor(int state, I input) {
            T trans = getTransition(state, input);
            if (trans == null) {
                return INVALID_STATE;
            }
            return getIntSuccessor(trans);
        }

        /**
         * Retrieves the outgoing transition for an (abstracted) source state and input symbol, or returns {@code null}
         * if the automaton has no transition for this state and input.
         *
         * @param state
         *         the integer representing the source state
         * @param input
         *         the input symbol
         *
         * @return the outgoing transition, or {@code null}
         */
        @Nullable
        T getTransition(int state, I input);

    }

    /**
     * Interface for {@link SimpleDeterministicAbstractions.FullIntAbstraction full integer abstractions} of a
     * {@link DeterministicAutomaton}.
     *
     * @param <T>
     *         transition type
     */
    interface FullIntAbstraction<T> extends IntAbstraction<T>, SimpleDeterministicAbstractions.FullIntAbstraction {

        @Override
        default int getSuccessor(int state, int input) {
            T trans = getTransition(state, input);
            if (trans == null) {
                return INVALID_STATE;
            }
            return getIntSuccessor(trans);
        }

        /**
         * Retrieves the outgoing transition for an (abstracted) source state and (abstracted) input symbol, or returns
         * {@code null} if the automaton has no transition for this state and input.
         *
         * @param state
         *         the integer representing the source state
         * @param input
         *         the integer representing the input symbol
         *
         * @return the outgoing transition, or {@code null}
         */
        @Nullable
        T getTransition(int state, int input);

    }

    class StateIntAbstractionImpl<S, I, T, A extends DeterministicAutomaton<S, I, T>>
            extends SimpleDeterministicAbstractions.StateIntAbstractionImpl<S, I, A>
            implements StateIntAbstraction<I, T> {

        public StateIntAbstractionImpl(A automaton) {
            super(automaton);
        }

        @Override
        public int getIntSuccessor(T transition) {
            return stateToInt(automaton.getSuccessor(transition));
        }

        @Override
        public @Nullable T getTransition(int state, I input) {
            return automaton.getTransition(intToState(state), input);
        }
    }

    class FullIntAbstractionImpl<I, T, A extends StateIntAbstraction<I, T>>
            extends SimpleDeterministicAbstractions.FullIntAbstractionImpl<I, A> implements FullIntAbstraction<T> {

        public FullIntAbstractionImpl(A stateAbstraction, int numInputs, IntFunction<? extends I> symMapping) {
            super(stateAbstraction, numInputs, symMapping);
        }

        @Override
        public @Nullable T getTransition(int state, int input) {
            return stateAbstraction.getTransition(state, intToSym(input));
        }

        @Override
        public int getIntSuccessor(T transition) {
            return stateAbstraction.getIntSuccessor(transition);
        }
    }
}
