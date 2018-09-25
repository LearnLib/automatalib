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

import java.util.Collection;
import java.util.function.IntFunction;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Alphabet;

/**
 * Interface for a <i>mutable</i> deterministic automaton.
 *
 * @param <S>
 *         state class.
 * @param <I>
 *         input symbol class.
 * @param <T>
 *         transition class.
 * @param <SP>
 *         state property.
 * @param <TP>
 *         transition property.
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public interface MutableDeterministic<S, I, T, SP, TP>
        extends UniversalDeterministicAutomaton<S, I, T, SP, TP>, MutableAutomaton<S, I, T, SP, TP> {

    @Override
    default void setInitial(S state, boolean initial) {
        S currInitial = getInitialState();
        if (state.equals(currInitial)) {
            if (!initial) {
                setInitialState(null);
            }
        } else if (currInitial == null) {
            setInitialState(state);
        } else {
            throw new IllegalStateException(
                    "Cannot set state '" + state + "' as " + "additional initial state (current initial state: '" +
                    currInitial + "'.");
        }
    }

    @Override
    default void setTransitions(S state, I input, Collection<? extends T> transitions) {

        int num = transitions.size();
        if (num > 1) {
            throw new IllegalArgumentException(
                    "Deterministic automaton can not " + "have multiple transitions for the same input symbol.");
        }

        T trans = (num > 0) ? transitions.iterator().next() : null;

        setTransition(state, input, trans);
    }

    @Override
    default void removeTransition(S state, I input, T transition) {
        if (transition == null) {
            return;
        }
        T currTrans = getTransition(state, input);
        if (transition.equals(currTrans)) {
            setTransition(state, input, null);
        }
    }

    @Override
    default void removeAllTransitions(S state, I input) {
        setTransition(state, input, null);
    }

    @Override
    default void addTransition(S state, I input, T transition) {
        T currTrans = getTransition(state, input);
        if (currTrans != null) {
            throw new IllegalStateException("Cannot add transition " + transition +
                                            " to deterministic automaton: transition already defined for state " +
                                            state + " and input " + input + ".");
        }
        setTransition(state, input, transition);
    }

    /**
     * Sets the initial state to the given state. If the current initial state should be unset, {@code null} can be
     * passed.
     *
     * @param state
     *         the new initial state, or {@code null}.
     */
    void setInitialState(@Nullable S state);

    /**
     * Sets the transition for the given state and input symbol.
     *
     * @param state
     *         the source state
     * @param input
     *         the triggering input symbol
     * @param transition
     *         the transition
     */
    void setTransition(S state, @Nullable I input, @Nullable T transition);

    /**
     * Sets the transition for the given state and input symbol to a newly created one.
     *
     * @param state
     *         the source state
     * @param input
     *         the triggering input symbol
     * @param successor
     *         the target state
     * @param property
     *         the transition's property
     */
    default void setTransition(S state, @Nullable I input, @Nullable S successor, @Nullable TP property) {
        if (successor != null) {
            T trans = createTransition(successor, property);
            setTransition(state, input, trans);
        }
    }

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

    interface IntAbstraction<T, SP, TP> extends UniversalDeterministicAutomaton.IntAbstraction<T, SP, TP> {

        void setStateProperty(int state, @Nullable SP property);

        void setTransitionProperty(T transition, @Nullable TP property);

        void setInitialState(int state);

        T createTransition(int successor, @Nullable TP property);

        default int addIntState() {
            return addIntState(null);
        }

        int addIntState(@Nullable SP property);

        default int addIntInitialState() {
            return addIntInitialState(null);
        }

        int addIntInitialState(@Nullable SP property);
    }

    interface StateIntAbstraction<I, T, SP, TP>
            extends IntAbstraction<T, SP, TP>, UniversalDeterministicAutomaton.StateIntAbstraction<I, T, SP, TP> {

        void setTransition(int state, I input, @Nullable T transition);

        void setTransition(int state, I input, int successor, @Nullable TP property);

        class DefaultAbstraction<S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>>
                extends UniversalDeterministicAutomaton.StateIntAbstraction.DefaultAbstraction<S, I, T, SP, TP, A>
                implements StateIntAbstraction<I, T, SP, TP> {

            public DefaultAbstraction(A automaton) {
                super(automaton);
            }

            @Override
            public void setStateProperty(int state, @Nullable SP property) {
                automaton.setStateProperty(intToState(state), property);
            }

            @Override
            public void setTransition(int state, I input, @Nullable T transition) {
                automaton.setTransition(intToState(state), input, transition);
            }

            @Override
            public void setTransition(int state, I input, int successor, @Nullable TP property) {
                automaton.setTransition(intToState(state), input, intToState(successor), property);
            }

            @Override
            public void setTransitionProperty(T transition, @Nullable TP property) {
                automaton.setTransitionProperty(transition, property);
            }

            @Override
            public void setInitialState(int state) {
                automaton.setInitialState(intToState(state));
            }

            @Override
            public T createTransition(int successor, @Nullable TP property) {
                return automaton.createTransition(intToState(successor), property);
            }

            @Override
            public int addIntState() {
                return stateToInt(automaton.addState());
            }

            @Override
            public int addIntState(@Nullable SP property) {
                return stateToInt(automaton.addState(property));
            }

            @Override
            public int addIntInitialState() {
                return stateToInt(automaton.addInitialState());
            }

            @Override
            public int addIntInitialState(@Nullable SP property) {
                return stateToInt(automaton.addInitialState(property));
            }

        }
    }

    interface FullIntAbstraction<T, SP, TP>
            extends IntAbstraction<T, SP, TP>, UniversalDeterministicAutomaton.FullIntAbstraction<T, SP, TP> {

        void setTransition(int state, int input, @Nullable T transition);

        void setTransition(int state, int input, int successor, @Nullable TP property);

        class DefaultAbstraction<I, T, SP, TP, A extends StateIntAbstraction<I, T, SP, TP>>
                extends UniversalDeterministicAutomaton.FullIntAbstraction.DefaultAbstraction<I, T, SP, TP, A>
                implements FullIntAbstraction<T, SP, TP> {

            public DefaultAbstraction(A stateAbstraction, int numInputs, IntFunction<? extends I> symMapping) {
                super(stateAbstraction, numInputs, symMapping);
            }

            @Override
            public void setTransition(int state, int input, @Nullable T transition) {
                stateAbstraction.setTransition(state, intToSym(input), transition);
            }

            @Override
            public void setTransition(int state, int input, int successor, @Nullable TP property) {
                stateAbstraction.setTransition(state, intToSym(input), successor, property);
            }

            @Override
            public void setStateProperty(int state, @Nullable SP property) {
                stateAbstraction.setStateProperty(state, property);
            }

            @Override
            public void setTransitionProperty(T transition, @Nullable TP property) {
                stateAbstraction.setTransitionProperty(transition, property);
            }

            @Override
            public void setInitialState(int state) {
                stateAbstraction.setInitialState(state);
            }

            @Override
            public T createTransition(int successor, @Nullable TP property) {
                return stateAbstraction.createTransition(successor, property);
            }

            @Override
            public int addIntState() {
                return stateAbstraction.addIntState();
            }

            @Override
            public int addIntState(@Nullable SP property) {
                return stateAbstraction.addIntState(property);
            }

            @Override
            public int addIntInitialState() {
                return stateAbstraction.addIntInitialState();
            }

            @Override
            public int addIntInitialState(@Nullable SP property) {
                return stateAbstraction.addIntInitialState(property);
            }

        }
    }
}
