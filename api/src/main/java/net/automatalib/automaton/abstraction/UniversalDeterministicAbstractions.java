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

import net.automatalib.automaton.UniversalDeterministicAutomaton;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstractions for {@link UniversalDeterministicAutomaton}s.
 */
public interface UniversalDeterministicAbstractions {

    /**
     * Base interface for {@link DeterministicAbstractions.IntAbstraction integer abstractions} of a
     * {@link UniversalDeterministicAutomaton}.
     *
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     */
    interface IntAbstraction<T, SP, TP> extends DeterministicAbstractions.IntAbstraction<T> {

        /**
         * Retrieves the state property of a given (abstracted) state.
         *
         * @param state
         *         the integer representing the state of which to retrieve the property
         *
         * @return the property for the given state
         */
        SP getStateProperty(int state);

        /**
         * Retrieves the transition property of a given transition.
         *
         * @param transition
         *         the transition of which to retrieve the property
         *
         * @return the property for the given transition
         */
        TP getTransitionProperty(T transition);
    }

    /**
     * Interface for {@link DeterministicAbstractions.StateIntAbstraction state integer abstractions} of a
     * {@link UniversalDeterministicAutomaton}.
     *
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     */
    interface StateIntAbstraction<I, T, SP, TP>
            extends IntAbstraction<T, SP, TP>, DeterministicAbstractions.StateIntAbstraction<I, T> {

        default @Nullable TP getTransitionProperty(int state, I input) {
            T trans = getTransition(state, input);
            if (trans != null) {
                return getTransitionProperty(trans);
            }
            return null;
        }

    }

    /**
     * Interface for {@link DeterministicAbstractions.FullIntAbstraction full integer abstractions} of a
     * {@link UniversalDeterministicAutomaton}.
     *
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     */
    interface FullIntAbstraction<T, SP, TP>
            extends IntAbstraction<T, SP, TP>, DeterministicAbstractions.FullIntAbstraction<T> {

        default @Nullable TP getTransitionProperty(int state, int input) {
            T trans = getTransition(state, input);
            if (trans != null) {
                return getTransitionProperty(trans);
            }
            return null;
        }

    }

    class StateIntAbstractionImpl<S, I, T, SP, TP, A extends UniversalDeterministicAutomaton<S, I, T, SP, TP>>
            extends DeterministicAbstractions.StateIntAbstractionImpl<S, I, T, A>
            implements StateIntAbstraction<I, T, SP, TP> {

        public StateIntAbstractionImpl(A automaton) {
            super(automaton);
        }

        @Override
        public SP getStateProperty(int state) {
            return automaton.getStateProperty(intToState(state));
        }

        @Override
        public TP getTransitionProperty(T transition) {
            return automaton.getTransitionProperty(transition);
        }
    }

    class FullIntAbstractionImpl<I, T, SP, TP, A extends StateIntAbstraction<I, T, SP, TP>>
            extends DeterministicAbstractions.FullIntAbstractionImpl<I, T, A> implements FullIntAbstraction<T, SP, TP> {

        public FullIntAbstractionImpl(A stateAbstraction, int numInputs, IntFunction<? extends I> symMapping) {
            super(stateAbstraction, numInputs, symMapping);
        }

        @Override
        public SP getStateProperty(int state) {
            return stateAbstraction.getStateProperty(state);
        }

        @Override
        public TP getTransitionProperty(T transition) {
            return stateAbstraction.getTransitionProperty(transition);
        }
    }
}
