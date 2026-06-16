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
package net.automatalib.automaton.procedural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.semantics.DeterministicSemantics.UniversalSemantics;
import net.automatalib.ts.concept.DeterministicSuffixOutputTS;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A system of procedural Mealy machines. {@link SPMM}s extend the idea of {@link SBA}s by supporting deterministic
 * bi-languages with lock-step semantics (typically known from {@link MealyMachine}s in the regular case). This makes
 * {@link SPMM}s a preferable model for (instrumented) dialog-systems in which the system responds to an external input
 * stimuli with an observable output symbol.
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
public interface SPMM<S, I, T, O> extends ProceduralSystem<I, MealyMachine<?, I, ?, O>>,
                                          MealyTransitionSystem<S, I, T, O>, UniversalSemantics<S, I, T, Void, O> {

    @Override
    default SPMM<S, I, T, O> getSemantics() {
        return this;
    }

    /**
     * Returns the output symbol that identifies erroneous transitions. Note that for the validity of this {@link SPMM},
     * each transition labeled with this output symbol must lead into a sink state that continues to output this
     * symbol.
     *
     * @return the output symbol that identifies erroneous transitions
     */
    O getErrorOutput();

    /**
     * Convenience method that compares the given {@code output} with this {@link SPMM}'s
     * {@link #getErrorOutput() error output}.
     *
     * @param output
     *         the symbol to check
     *
     * @return {@code true} if {@code output} equals this {@link SPMM}'s {@link #getErrorOutput() error output},
     * {@code false} otherwise.
     */
    default boolean isErrorOutput(O output) {
        return Objects.equals(this.getErrorOutput(), output);
    }

    @Override
    default Collection<I> getProceduralInputs(Collection<I> constraints) {
        final ProceduralInputAlphabet<I> alphabet = getInputAlphabet();
        final Map<I, MealyMachine<?, I, ?, O>> procedures = getProcedures();

        final List<I> result = new ArrayList<>(Math.min(alphabet.size(), constraints.size()));

        for (I i : constraints) {
            if (procedures.containsKey(i) || alphabet.isInternalSymbol(i) || alphabet.isReturnSymbol(i)) {
                result.add(i);
            }
        }

        return result;
    }

    /**
     * This implementation differs from the
     * {@link DeterministicSuffixOutputTS#computeStateOutput(Object, Iterable) default implementation} in that it always
     * computes the full output word  because the outputs of missing transitions can be replaced by this {@link SPMM}'s
     * {@link #getErrorOutput() error output}.
     *
     * @param state
     *         the start from which the output computation should start
     * @param input
     *         the sequence of input symbols
     *
     * @return the computed output
     */
    @Override
    default Word<O> computeStateOutput(@Nullable S state, Iterable<? extends I> input) {
        final WordBuilder<O> output = Output.getBuilderFor(input);

        S iter = state;
        for (I i : input) {
            if (iter == null) {
                output.add(getErrorOutput());
            } else {
                T trans = getTransition(iter, i);
                if (trans == null) {
                    output.add(getErrorOutput());
                    iter = null;
                } else {
                    O out = getTransitionOutput(trans);
                    output.add(out);
                    iter = getSuccessor(trans);
                }
            }
        }

        return output.toWord();
    }

}
