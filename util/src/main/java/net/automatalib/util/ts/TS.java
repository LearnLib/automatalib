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
package net.automatalib.util.ts;

import java.util.Iterator;
import java.util.function.Function;

import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.UniversalTransitionSystem;
import net.automatalib.util.ts.iterator.AllDefinedInputsIterator;
import net.automatalib.util.ts.iterator.AllUndefinedInputsIterator;
import net.automatalib.util.ts.iterator.DefinedInputsIterator;
import net.automatalib.util.ts.iterator.UndefinedInputsIterator;

public final class TS {

    private TS() {
        // prevent initialization
    }

    public static <S, SP> Function<S, SP> stateProperties(UniversalTransitionSystem<S, ?, ?, SP, ?> uts) {
        return uts::getStateProperty;
    }

    public static <T, TP> Function<T, TP> transitionProperties(UniversalTransitionSystem<?, ?, T, ?, TP> uts) {
        return uts::getTransitionProperty;
    }

    public static <S, I> Iterable<I> definedInputs(DeterministicTransitionSystem<S, I, ?> dts,
                                                   S state,
                                                   Iterable<? extends I> inputs) {
        return () -> definedInputsIterator(dts, state, inputs.iterator());
    }

    public static <S, I> Iterator<I> definedInputsIterator(TransitionSystem<S, I, ?> ts,
                                                           S state,
                                                           Iterator<? extends I> inputsIt) {
        return new DefinedInputsIterator<>(ts, state, inputsIt);
    }

    public static <S, I> Iterable<TransRef<S, I, ?>> allDefinedInputs(TransitionSystem<S, I, ?> ts,
                                                                      Iterable<? extends S> states,
                                                                      Iterable<? extends I> inputs) {
        return () -> allDefinedInputsIterator(ts, states.iterator(), inputs);
    }

    public static <S, I> Iterator<TransRef<S, I, ?>> allDefinedInputsIterator(TransitionSystem<S, I, ?> ts,
                                                                              Iterator<? extends S> stateIt,
                                                                              Iterable<? extends I> inputs) {
        return new AllDefinedInputsIterator<>(stateIt, ts, inputs);
    }

    public static <S, I> Iterable<I> undefinedInputs(TransitionSystem<S, I, ?> ts,
                                                     S state,
                                                     Iterable<? extends I> inputs) {
        return () -> undefinedInputsIterator(ts, state, inputs.iterator());
    }

    public static <S, I> Iterator<I> undefinedInputsIterator(TransitionSystem<S, I, ?> ts,
                                                             S state,
                                                             Iterator<? extends I> inputsIt) {
        return new UndefinedInputsIterator<>(ts, state, inputsIt);
    }

    public static <S, I> Iterable<TransRef<S, I, ?>> allUndefinedTransitions(TransitionSystem<S, I, ?> ts,
                                                                             Iterable<? extends S> states,
                                                                             Iterable<? extends I> inputs) {
        return () -> allUndefinedTransitionsIterator(ts, states.iterator(), inputs);
    }

    public static <S, I> Iterator<TransRef<S, I, ?>> allUndefinedTransitionsIterator(TransitionSystem<S, I, ?> ts,
                                                                                     Iterator<? extends S> stateIt,
                                                                                     Iterable<? extends I> inputs) {
        return new AllUndefinedInputsIterator<>(stateIt, ts, inputs);
    }

    public static final class TransRef<S, I, T> {

        public final S state;
        public final I input;
        public final T transition;

        public TransRef(S state, I input, T transition) {
            this.state = state;
            this.input = input;
            this.transition = transition;
        }
    }

}
