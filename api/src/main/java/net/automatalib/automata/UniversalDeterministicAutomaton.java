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
package net.automatalib.automata;

import java.util.function.IntFunction;

import net.automatalib.ts.UniversalDTS;
import net.automatalib.words.Alphabet;

/**
 * A {@link DeterministicAutomaton} with state and transition properties.
 *
 * @param <S>
 *         state class
 * @param <I>
 *         input symbol class
 * @param <T>
 *         transition class
 * @param <SP>
 *         state property class
 * @param <TP>
 *         transition property class
 *
 * @author Malte Isberner
 * @see UniversalAutomaton
 */
public interface UniversalDeterministicAutomaton<S, I, T, SP, TP>
        extends DeterministicAutomaton<S, I, T>, UniversalDTS<S, I, T, SP, TP>, UniversalAutomaton<S, I, T, SP, TP> {

    @Override
    default FullIntAbstraction<T, SP, TP> fullIntAbstraction(Alphabet<I> alphabet) {
        return fullIntAbstraction(alphabet.size(), alphabet);
    }

    @Override
    default FullIntAbstraction<T, SP, TP> fullIntAbstraction(int numInputs, IntFunction<? extends I> symMapping) {
        return new FullIntAbstraction.DefaultAbstraction<>(stateIntAbstraction(), numInputs, symMapping);
    }

    @Override
    default StateIntAbstraction<I, T, SP, TP> stateIntAbstraction() {
        return new StateIntAbstraction.DefaultAbstraction<>(this);
    }

    /**
     * Base interface for {@link IntAbstraction integer abstractions} of a {@link
     * UniversalDeterministicAutomaton}.
     *
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     *
     * @author Malte Isberner
     */
    interface IntAbstraction<T, SP, TP> extends DeterministicAutomaton.IntAbstraction<T> {

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
     * Interface for {@link StateIntAbstraction state integer abstractions} of a {@link
     * UniversalDeterministicAutomaton}.
     *
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     *
     * @author Malte Isberner
     */
    interface StateIntAbstraction<I, T, SP, TP>
            extends IntAbstraction<T, SP, TP>, DeterministicAutomaton.StateIntAbstraction<I, T> {

        default TP getTransitionProperty(int state, I input) {
            T trans = getTransition(state, input);
            if (trans != null) {
                return getTransitionProperty(trans);
            }
            return null;
        }

        class DefaultAbstraction<S, I, T, SP, TP, A extends UniversalDeterministicAutomaton<S, I, T, SP, TP>>
                extends DeterministicAutomaton.StateIntAbstraction.DefaultAbstraction<S, I, T, A>
                implements StateIntAbstraction<I, T, SP, TP> {

            public DefaultAbstraction(A automaton) {
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
    }

    /**
     * Interface for {@link FullIntAbstraction full integer abstractions} of a {@link
     * UniversalDeterministicAutomaton}.
     *
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     *
     * @author Malte Isberner
     */
    interface FullIntAbstraction<T, SP, TP>
            extends IntAbstraction<T, SP, TP>, DeterministicAutomaton.FullIntAbstraction<T> {

        default TP getTransitionProperty(int state, int input) {
            T trans = getTransition(state, input);
            if (trans != null) {
                return getTransitionProperty(trans);
            }
            return null;
        }

        class DefaultAbstraction<I, T, SP, TP, A extends StateIntAbstraction<I, T, SP, TP>>
                extends DeterministicAutomaton.FullIntAbstraction.DefaultAbstraction<I, T, A>
                implements FullIntAbstraction<T, SP, TP> {

            public DefaultAbstraction(A stateAbstraction, int numInputs, IntFunction<? extends I> symMapping) {
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
}

