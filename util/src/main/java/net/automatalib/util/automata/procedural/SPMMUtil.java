/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.util.automata.procedural;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.automatalib.automata.procedural.SPMM;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.ProceduralOutputAlphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author frohme
 */
public final class SPMMUtil {

    private SPMMUtil() {
        // prevent instantiation
    }

    public static <I, O> ATSequences<I> computeATSequences(SPMM<?, I, ?, O> spmm) {
        return computeATSequences(spmm, spmm.getInputAlphabet());
    }

    public static <I, O> ATSequences<I> computeATSequences(SPMM<?, I, ?, O> spmm,
                                                           ProceduralInputAlphabet<I> inputAlphabet) {

        assert isValid(spmm, inputAlphabet);

        final Map<I, Word<I>> terminatingSequences = computeTerminatingSequences(spmm, inputAlphabet);
        final Map<I, Word<I>> accessSequences = computeAccessSequences(spmm, inputAlphabet, terminatingSequences);

        return new ATSequences<>(accessSequences, terminatingSequences);
    }

    public static <I, O> Map<I, Word<I>> computeTerminatingSequences(SPMM<?, I, ?, O> spmm,
                                                                     ProceduralInputAlphabet<I> alphabet) {
        final Word<I> returnWord = Word.fromLetter(alphabet.getReturnSymbol());
        final ProceduralOutputAlphabet<O> outputAlphabet = spmm.getOutputAlphabet();

        return ProceduralUtil.computeTerminatingSequences(spmm.getProcedures(), alphabet, (mealy, trace) -> {
            final Word<O> output = mealy.computeSuffixOutput(trace, returnWord);
            return !output.isEmpty() && !outputAlphabet.isErrorSymbol(output.lastSymbol());
        });
    }

    public static <I, O> Map<I, Word<I>> computeAccessSequences(SPMM<?, I, ?, O> spmm,
                                                                ProceduralInputAlphabet<I> alphabet,
                                                                Map<I, Word<I>> terminatingSequences) {

        final ProceduralOutputAlphabet<O> outputAlphabet = spmm.getOutputAlphabet();

        return ProceduralUtil.computeAccessSequences(spmm.getProcedures(),
                                                     alphabet,
                                                     spmm.getProceduralInputs(alphabet),
                                                     spmm.getInitialProcedure(),
                                                     terminatingSequences,
                                                     (mealy, trace) -> !outputAlphabet.isErrorSymbol(mealy.computeOutput(
                                                             trace).lastSymbol()));
    }

    public static <I, O> boolean isValid(SPMM<?, I, ?, O> spmm) {
        return isValid(spmm, spmm.getInputAlphabet());
    }

    public static <I, O> boolean isValid(SPMM<?, I, ?, O> spmm, ProceduralInputAlphabet<I> alphabet) {

        final Map<I, Word<I>> ts = computeTerminatingSequences(spmm, alphabet);
        final Collection<I> proceduralInputs = spmm.getProceduralInputs(alphabet);
        final Set<I> nonContinuableSymbols = new HashSet<>(alphabet.getCallAlphabet());
        nonContinuableSymbols.removeAll(ts.keySet());
        nonContinuableSymbols.retainAll(proceduralInputs);
        nonContinuableSymbols.add(alphabet.getReturnSymbol());

        final ProceduralOutputAlphabet<O> outputAlphabet = spmm.getOutputAlphabet();

        for (MealyMachine<?, I, ?, O> p : spmm.getProcedures().values()) {
            if (!isErrorReturnAndCallClosed(p,
                                            proceduralInputs,
                                            nonContinuableSymbols,
                                            outputAlphabet.getErrorSymbol())) {
                return false;
            }
        }

        return true;
    }

    private static <S, I, O> boolean isErrorReturnAndCallClosed(MealyMachine<S, I, ?, O> procedure,
                                                                Collection<I> inputs,
                                                                Collection<I> nonContinuableInputs,
                                                                O errorOutput) {

        for (I i : inputs) {

            boolean isNonContinuable = nonContinuableInputs.contains(i);

            for (S s : procedure) {

                final O output = procedure.getOutput(s, i);
                final S succ = procedure.getSuccessor(s, i);

                if (succ != null && (isNonContinuable || Objects.equals(output, errorOutput)) &&
                    !isSink(procedure, inputs, succ, errorOutput)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static <S, I, T, O> boolean isSink(MealyMachine<S, I, T, O> m,
                                               Collection<? extends I> inputs,
                                               S state,
                                               O output) {
        for (I i : inputs) {
            final T t = m.getTransition(state, i);
            if (t != null &&
                (!Objects.equals(m.getSuccessor(t), state) || !Objects.equals(m.getTransitionOutput(t), output))) {
                return false;
            }
        }
        return true;
    }

    public static <I, O> boolean testEquivalence(SPMM<?, I, ?, O> spmm1,
                                                 SPMM<?, I, ?, O> spmm2,
                                                 ProceduralInputAlphabet<I> alphabet) {
        return findSeparatingWord(spmm1, spmm2, alphabet) == null;
    }

    public static <I, O> @Nullable Word<I> findSeparatingWord(SPMM<?, I, ?, O> spmm1,
                                                              SPMM<?, I, ?, O> spmm2,
                                                              ProceduralInputAlphabet<I> alphabet) {

        final ATSequences<I> at1 = computeATSequences(spmm1, alphabet);
        final ATSequences<I> at2 = computeATSequences(spmm2, alphabet);

        return ProceduralUtil.findSeparatingWord(spmm1.getProcedures(), at1, spmm2.getProcedures(), at2, alphabet);
    }
}
