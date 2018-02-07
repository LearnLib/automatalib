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
package net.automatalib.automata.simple;

import java.util.function.IntFunction;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Alphabet;

/**
 * A simple deterministic automaton.
 *
 * @param <S>
 *         state class.
 * @param <I>
 *         input symbol class.
 *
 * @author Malte Isberner
 */
public interface SimpleDeterministicAutomaton<S, I> extends SimpleAutomaton<S, I>, SimpleDTS<S, I> {

    /**
     * Retrieves a {@link FullIntAbstraction} of this automaton, using the mapping induced by the given alphabet as the
     * abstraction for the input symbols.
     * <p>
     * This method is provided for convenience. It is equivalent to calling {@code fullIntAbstraction(alphabet.size(),
     * alphabet)}.
     *
     * @param alphabet
     *         the alphabet inducing the abstraction
     *
     * @return a {@link FullIntAbstraction}
     */
    default FullIntAbstraction fullIntAbstraction(Alphabet<I> alphabet) {
        return fullIntAbstraction(alphabet.size(), alphabet);
    }

    /**
     * Retrieves a {@link FullIntAbstraction} of this automaton, using the given number of (abstract) inputs and the
     * inputs mapping.
     *
     * @param numInputs
     *         the number of inputs represented in the full abstraction
     * @param symMapping
     *         the mapping from integers in the range {@code [0, numInputs - 1]} to input symbols.
     *
     * @return a {@link FullIntAbstraction}
     */
    default FullIntAbstraction fullIntAbstraction(int numInputs, IntFunction<? extends I> symMapping) {
        return new FullIntAbstraction.DefaultAbstraction<>(stateIntAbstraction(), numInputs, symMapping);
    }

    /**
     * Retrieves a {@link StateIntAbstraction} of this automaton.
     *
     * @return a {@link StateIntAbstraction}
     */
    default StateIntAbstraction<I> stateIntAbstraction() {
        return new StateIntAbstraction.DefaultAbstraction<>(this);
    }

    /**
     * Basic interface for integer abstractions of automata. In an integer abstraction, each state of an automaton is
     * identified with an integer in the range {@code [0, size() - 1]}. A similar abstraction may be imposed on the
     * input symbols, this is however not prescribed by this interface (see {@link StateIntAbstraction} and {@link
     * FullIntAbstraction}).
     *
     * @author Malte Isberner
     */
    interface IntAbstraction {

        /**
         * Representative for an invalid state. This is the value being returned by methods that would return {@code
         * null} in their non-abstracted version. However, for determining whether a state is valid or not, code should
         * never rely on the corresponding integer being equal to this value, since any integer outside of the range
         * {@code [0, size() - 1]} is invalid, in particular all negative integers.
         */
        int INVALID_STATE = -1;

        /**
         * Retrieves the number of states of the (abstracted) automaton.
         *
         * @return the number of states.
         */
        int size();

        /**
         * Retrieves the initial state of the (abstracted) automaton as an integer. If the automaton has no initial
         * state, {@link #INVALID_STATE} is returned.
         *
         * @return the integer representing the initial state, or {@link #INVALID_STATE}.
         */
        int getIntInitialState();

        /**
         * Base class implementing the default way of obtaining an integer abstraction from an automaton, i.e., by
         * mapping states to integers and vice versa using the {@link StateIDs} mapping obtained via {@link
         * SimpleDeterministicAutomaton#stateIDs()}.
         *
         * @param <S>
         *         state type
         * @param <A>
         *         automaton type
         *
         * @author Malte Isberner
         */
        class DefaultAbstraction<S, A extends SimpleDeterministicAutomaton<S, ?>> implements IntAbstraction {

            protected final A automaton;
            protected final StateIDs<S> stateIds;

            public DefaultAbstraction(A automaton) {
                this.automaton = automaton;
                this.stateIds = automaton.stateIDs();
            }

            @Override
            public int size() {
                return automaton.size();
            }

            protected final S intToState(int stateId) {
                return (stateId >= 0) ? stateIds.getState(stateId) : null;
            }

            @Override
            public int getIntInitialState() {
                return stateToInt(automaton.getInitialState());
            }

            protected final int stateToInt(S state) {
                return (state != null) ? stateIds.getStateId(state) : INVALID_STATE;
            }

        }
    }

    /**
     * Interface for {@link IntAbstraction integer abstractions} of an automaton that operate on non-abstracted input
     * symbols (i.e., input symbols are of type {@code I}).
     *
     * @param <I>
     *         input symbol type
     *
     * @author Malte Isberner
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
         * @author Malte Isberner
         * @see IntAbstraction.DefaultAbstraction
         */
        class DefaultAbstraction<S, I, A extends SimpleDeterministicAutomaton<S, I>>
                extends IntAbstraction.DefaultAbstraction<S, A> implements StateIntAbstraction<I> {

            public DefaultAbstraction(A automaton) {
                super(automaton);
            }

            @Override
            public int getSuccessor(int state, I input) {
                if (state == INVALID_STATE) {
                    return INVALID_STATE;
                }
                return stateToInt(automaton.getSuccessor(intToState(state), input));
            }
        }
    }

    /**
     * Interface for an {@link IntAbstraction integer abstraction} that abstracts both states and input symbols to
     * integers. In addition to the modalities specified in {@link IntAbstraction}, this interface prescribes that input
     * symbols are abstracted to integers in the range {@code [0, numInputs() - 1]}.
     *
     * @author Malte Isberner
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
         * Retrieves the number of input symbols. This determines the valid range of input symbols, which is {@code [0,
         * numInputs() - 1]}.
         *
         * @return the number of input symbols
         */
        int numInputs();

        /**
         * Base class implementing the default way of obtaining a {@link FullIntAbstraction}, i.e., building on top of a
         * {@link StateIntAbstraction} and a mapping from integers to (concrete) input symbols.
         *
         * @param <I>
         *         input symbol type
         * @param <A>
         *         state abstraction type
         *
         * @author Malte Isberner
         */
        class DefaultAbstraction<I, A extends StateIntAbstraction<I>> implements FullIntAbstraction {

            protected final A stateAbstraction;
            protected final int numInputs;
            protected final IntFunction<? extends I> symMapping;

            public DefaultAbstraction(A stateAbstraction, int numInputs, IntFunction<? extends I> symMapping) {
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

}
