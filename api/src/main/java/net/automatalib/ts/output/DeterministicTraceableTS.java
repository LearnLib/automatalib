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
package net.automatalib.ts.output;

import java.util.List;

import net.automatalib.automaton.concept.Output;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.concept.DeterministicSuffixOutputTS;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A deterministic traceable transition system is a
 * {@link DeterministicSuffixOutputTS deterministic suffix output transition system} whose outputs are collected while
 * traversing the transition system. Compared to just computing an output, its
 * {@link #trace(Iterable, List) trace methods} additionally allow one the track whether undefined transition have been
 * encountered while constructing the output.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <O>
 *         output symbol type
 */
public interface DeterministicTraceableTS<S, I, T, O>
        extends DeterministicTransitionSystem<S, I, T>, DeterministicSuffixOutputTS<S, I, T, Word<O>> {

    /**
     * Convenience method for {@link #trace(Object, Iterable, List)} which uses this transition system's
     * {@link #getInitialState() initial state} as {@code state}.
     *
     * @param input
     *         the input sequence to apply
     * @param output
     *         the output list to which the observed outputs should be written to
     *
     * @return {@code true} if the complete input sequence could be applied, {@code false} if undefined transitions have
     * been encountered
     */
    default boolean trace(Iterable<? extends I> input, List<? super O> output) {
        final S init = getInitialState();

        return init != null && trace(init, input, output);
    }

    /**
     * Traces for a given state and input sequence, the observed outputs and writes them to the provided list.
     *
     * @param state
     *         the state from which to start the traversal
     * @param input
     *         the input sequence to apply
     * @param output
     *         the output list to which the observed outputs should be written to
     *
     * @return {@code true} if the complete input sequence could be applied, {@code false} if undefined transitions have
     * been encountered
     */
    boolean trace(S state, Iterable<? extends I> input, List<? super O> output);

    @Override
    default Word<O> computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
        return computeStateOutput(getState(prefix), suffix);
    }

    @Override
    default Word<O> computeStateOutput(@Nullable S state, Iterable<? extends I> input) {
        if (state == null) {
            return Word.epsilon();
        }
        // increase by one because state outputs systems may need them
        final WordBuilder<O> wb = Output.getBuilderFor(input, 1);
        trace(state, input, wb);
        return wb.toWord();
    }

}
