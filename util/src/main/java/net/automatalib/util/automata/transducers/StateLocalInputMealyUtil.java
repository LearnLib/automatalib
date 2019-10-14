/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.util.automata.transducers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utilities for {@link net.automatalib.automata.transducers.StateLocalInputMealyMachine}s.
 *
 * @author frohme
 */
public final class StateLocalInputMealyUtil {

    private StateLocalInputMealyUtil() {
        throw new AssertionError("Should not be instantiated");
    }

    /**
     * Convenience method for {@link #partialToObservableOutput(StateLocalInputMealyMachine, Object)}, where the size of
     * the given reference is used as the new sink state.
     *
     * @param reference
     *         the (partial) mealy to transform
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return see {@link #partialToObservableOutput(StateLocalInputMealyMachine, Object)}.
     */
    public static <I, O> StateLocalInputMealyMachine<Integer, I, ?, OutputAndLocalInputs<I, O>> partialToObservableOutput(
            StateLocalInputMealyMachine<Integer, I, ?, O> reference) {
        return partialToObservableOutput(reference, reference.size());
    }

    /**
     * Transforms a (potentially partial) {@link StateLocalInputMealyMachine} to a complete {@link
     * StateLocalInputMealyMachine} that explicitly outputs the available input symbols on the transition outputs. This
     * this primarily useful if one can only use the {@link net.automatalib.automata.transducers.MealyMachine}
     * interpretation of the result.
     * <p>
     * The returned automaton is a wrapper of the original reference automaton. As such, changes to the reference will
     * also be reflected in the returned automaton.
     *
     * @param reference
     *         the (partial) mealy to transform
     * @param sink
     *         the sink state that should be used to collect all undefined transitions of the reference automaton.
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     * @param <O>
     *         output symbol type (of the reference)
     *
     * @return A fully defined automaton (even for inputs where the reference automaton would return {@code null}) which
     * transforms the information of the available input symbols to transition outputs.
     */
    public static <S, I, T, O> StateLocalInputMealyMachine<S, I, ?, OutputAndLocalInputs<I, O>> partialToObservableOutput(
            StateLocalInputMealyMachine<S, I, T, O> reference,
            S sink) {
        return new SLIWrapper<>(reference, sink);
    }

    private static class SLIWrapper<S, I, T, O>
            implements StateLocalInputMealyMachine<S, I, WrapperTransition<S, I, T, O>, OutputAndLocalInputs<I, O>> {

        private final StateLocalInputMealyMachine<S, I, T, O> reference;
        private final List<S> listOfStates;
        private final S sinkState;

        SLIWrapper(StateLocalInputMealyMachine<S, I, T, O> reference, S sink) {
            this.reference = reference;
            this.sinkState = sink;

            this.listOfStates = new ArrayList<>(reference.size() + 1);
            this.listOfStates.addAll(reference.getStates());
            this.listOfStates.add(sinkState);
        }

        @Override
        public Collection<S> getStates() {
            return listOfStates;
        }


        @Override
        public OutputAndLocalInputs<I, O> getTransitionOutput(WrapperTransition<S, I, T, O> transition) {
            return transition.getOutput();
        }


        @Override
        public @Nullable WrapperTransition<S, I, T, O> getTransition(S state, I input) {
            if (Objects.equals(state, sinkState)) {
                return new WrapperTransition<>(null, null);
            }

            return new WrapperTransition<>(reference, reference.getTransition(state, input));
        }

        @Override
        public S getSuccessor(WrapperTransition<S, I, T, O> transition) {
            final T trans = transition.getTransition();

            return trans == null ? sinkState : reference.getSuccessor(trans);
        }

        @Override
        public @Nullable S getInitialState() {
            return reference.getInitialState();
        }

        @Override
        public Collection<I> getLocalInputs(S state) {
            if (Objects.equals(state, sinkState)) {
                return Collections.emptySet();
            }

            return reference.getLocalInputs(state);
        }
    }

    private static class WrapperTransition<S, I, T, O> {

        private final StateLocalInputMealyMachine<S, I, T, O> reference;
        private final @Nullable T transition;
        private @MonotonicNonNull OutputAndLocalInputs<I, O> output;

        WrapperTransition(StateLocalInputMealyMachine<S, I, T, O> reference, @Nullable T transition) {
            this.reference = reference;
            this.transition = transition;
        }

        public @Nullable T getTransition() {
            return transition;
        }

        public OutputAndLocalInputs<I, O> getOutput() {
            if (transition == null) {
                return OutputAndLocalInputs.undefined();
            }

            if (output == null) {
                final S succ = reference.getSuccessor(transition);
                final O out = reference.getTransitionOutput(transition);
                output = new OutputAndLocalInputs<>(out, reference.getLocalInputs(succ));
            }

            return output;
        }
    }
}
