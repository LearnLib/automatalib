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
package net.automatalib.automaton;

import java.util.function.IntFunction;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.abstraction.DeterministicAbstractions;
import net.automatalib.automaton.simple.SimpleDeterministicAutomaton;
import net.automatalib.ts.DeterministicTransitionSystem;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Basic interface for a deterministic automaton. A deterministic automaton is a {@link DeterministicTransitionSystem}
 * with a finite number of states.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 */
public interface DeterministicAutomaton<S, I, T>
        extends Automaton<S, I, T>, SimpleDeterministicAutomaton<S, I>, DeterministicTransitionSystem<S, I, T> {

    @Override
    default FullIntAbstraction<T> fullIntAbstraction(Alphabet<I> alphabet) {
        return fullIntAbstraction(alphabet.size(), alphabet);
    }

    @Override
    default FullIntAbstraction<T> fullIntAbstraction(int numInputs, IntFunction<? extends I> symMapping) {
        return new DeterministicAbstractions.FullIntAbstraction<>(stateIntAbstraction(), numInputs, symMapping);
    }

    @Override
    default StateIntAbstraction<I, T> stateIntAbstraction() {
        return new DeterministicAbstractions.StateIntAbstraction<>(this);
    }

    /**
     * Base interface for {@link SimpleDeterministicAutomaton.IntAbstraction integer abstractions} of a {@link
     * DeterministicAutomaton}.
     *
     * @param <T>
     *         transition type
     */
    interface IntAbstraction<T> extends SimpleDeterministicAutomaton.IntAbstraction {

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
     * Interface for {@link SimpleDeterministicAutomaton.StateIntAbstraction state integer abstractions} of a {@link
     * DeterministicAutomaton}.
     *
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     */
    interface StateIntAbstraction<I, T> extends IntAbstraction<T>, SimpleDeterministicAutomaton.StateIntAbstraction<I> {

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
        @Nullable T getTransition(int state, I input);

    }

    /**
     * Interface for {@link SimpleDeterministicAutomaton.FullIntAbstraction full integer abstractions} of a {@link
     * DeterministicAutomaton}.
     *
     * @param <T>
     *         transition type
     */
    interface FullIntAbstraction<T> extends IntAbstraction<T>, SimpleDeterministicAutomaton.FullIntAbstraction {

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
        @Nullable T getTransition(int state, int input);

    }
}
