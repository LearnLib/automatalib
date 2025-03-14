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
package net.automatalib.util.automaton.predicate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import net.automatalib.automaton.concept.TransitionOutput;
import net.automatalib.common.util.function.FunctionsUtil;
import net.automatalib.ts.TransitionPredicate;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TransitionPredicates {

    private TransitionPredicates() {}

    public static <S, I, T> TransitionPredicate<S, I, T> safePred(@Nullable TransitionPredicate<S, I, T> pred,
                                                                  boolean nullValue) {
        if (pred != null) {
            return pred;
        }
        return (s, i, t) -> nullValue;
    }

    public static <S, I, T> TransitionPredicate<S, I, T> compose(@Nullable Predicate<? super S> sourcePred,
                                                                 @Nullable Predicate<? super I> inputPred,
                                                                 @Nullable Predicate<? super T> transPred) {
        final Predicate<? super S> safeSource = FunctionsUtil.safeToTrue(sourcePred);
        final Predicate<? super I> safeInput = FunctionsUtil.safeToTrue(inputPred);
        final Predicate<? super T> safeTrans = FunctionsUtil.safeToTrue(transPred);
        return (s, i, t) -> safeSource.test(s) && safeInput.test(i) && safeTrans.test(t);
    }

    public static <S, I, T> TransitionPredicate<S, I, T> alwaysTrue() {
        return (s, i, t) -> true;
    }

    public static <S, I, T> TransitionPredicate<S, I, T> alwaysFalse() {
        return (s, i, t) -> false;
    }

    public static <S, I, T> TransitionPredicate<S, I, T> constantValue(boolean value) {
        return (s, i, t) -> value;
    }

    public static <S, I, T> TransitionPredicate<S, I, T> sourceSatisfying(Predicate<? super S> sourcePred) {
        return (s, i, t) -> sourcePred.test(s);
    }

    public static <S, I, T> TransitionPredicate<S, I, T> inputSatisfying(Predicate<? super I> inputPred) {
        return (s, i, t) -> inputPred.test(i);
    }

    public static <S, I, T> TransitionPredicate<S, I, T> transitionSatisfying(Predicate<? super T> transPred) {
        return (s, i, t) -> transPred.test(t);
    }

    public static <S, I, T> TransitionPredicate<S, I, T> inputIs(@Nullable Object input) {
        return (s, i, t) -> Objects.equals(i, input);
    }

    public static <S, I, T> TransitionPredicate<S, I, T> inputIsNot(@Nullable Object input) {
        return (s, i, t) -> !Objects.equals(i, input);
    }

    public static <S, I, T> TransitionPredicate<S, I, T> inputIn(@Nullable Object... inputs) {
        return inputIn(Arrays.asList(inputs));
    }

    public static <S, I, T> TransitionPredicate<S, I, T> inputIn(Collection<?> inputs) {
        return (s, i, t) -> inputs.contains(i);
    }

    public static <S, I, T> TransitionPredicate<S, I, T> inputNotIn(@Nullable Object... inputs) {
        return inputNotIn(Arrays.asList(inputs));
    }

    public static <S, I, T> TransitionPredicate<S, I, T> inputNotIn(Collection<?> inputs) {
        return (s, i, t) -> !inputs.contains(i);
    }

    public static <S, I, T> TransitionPredicate<S, I, T> outputIs(TransitionOutput<? super T, ?> transOut,
                                                                  @Nullable Object output) {
        return outputSatisfies(transOut, o -> Objects.equals(o, output));
    }

    public static <S, I, T, O> TransitionPredicate<S, I, T> outputSatisfies(TransitionOutput<? super T, ? extends O> transOut,
                                                                            Predicate<? super O> outputPred) {
        return (s, i, t) -> outputPred.test(transOut.getTransitionOutput(t));
    }

    public static <S, I, T> TransitionPredicate<S, I, T> outputIsNot(TransitionOutput<? super T, ?> transOut,
                                                                     @Nullable Object output) {
        return outputViolates(transOut, o -> Objects.equals(o, output));
    }

    public static <S, I, T, O> TransitionPredicate<S, I, T> outputViolates(TransitionOutput<? super T, ? extends O> transOut,
                                                                           Predicate<? super O> outputPred) {
        return (s, i, t) -> !outputPred.test(transOut.getTransitionOutput(t));
    }

    public static <S, I, T> TransitionPredicate<S, I, T> outputIn(TransitionOutput<? super T, ?> transOut,
                                                                  @Nullable Object... outputs) {
        return outputIn(transOut, Arrays.asList(outputs));
    }

    public static <S, I, T> TransitionPredicate<S, I, T> outputIn(TransitionOutput<? super T, ?> transOut,
                                                                  Collection<?> outputs) {
        return outputSatisfies(transOut, outputs::contains);
    }

    public static <S, I, T> TransitionPredicate<S, I, T> outputNotIn(TransitionOutput<? super T, ?> transOut,
                                                                     @Nullable Object... outputs) {
        return outputNotIn(transOut, Arrays.asList(outputs));
    }

    public static <S, I, T> TransitionPredicate<S, I, T> outputNotIn(TransitionOutput<? super T, ?> transOut,
                                                                     Collection<?> outputs) {
        return outputViolates(transOut, outputs::contains);
    }

}

