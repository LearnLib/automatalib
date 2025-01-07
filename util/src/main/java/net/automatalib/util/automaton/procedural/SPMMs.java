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
package net.automatalib.util.automaton.procedural;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Operations on {@link SPMM}s.
 */
public final class SPMMs {

    private SPMMs() {
        // prevent instantiation
    }

    /**
     * Computes a set of access sequences and terminating sequences for a given {@link SPMM}.  This is a convenience
     * method for {@link #computeATSequences(SPMM, ProceduralInputAlphabet)} that automatically uses the
     * {@link SPMM#getInputAlphabet() input alphabet} of the given {@code spmm}.
     *
     * @param spmm
     *         the {@link SPMM} for which the sequences should be computed
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return an {@link ATSequences} object which contains the respective sequences.
     *
     * @see #computeATSequences(SPMM, ProceduralInputAlphabet)
     */
    public static <I, O> ATSequences<I> computeATSequences(SPMM<?, I, ?, O> spmm) {
        return computeATSequences(spmm, spmm.getInputAlphabet());
    }

    /**
     * Computes a set of access sequences and return sequences for a given {@link SPMM} limited to the symbols of the
     * given {@link ProceduralInputAlphabet}.
     *
     * @param spmm
     *         the {@link SPMM} for which the sequences should be computed
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for computing the respective sequences
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return an {@link ATSequences} object which contains the respective sequences.
     *
     * @see #computeAccessSequences(SPMM, ProceduralInputAlphabet, Map)
     * @see #computeTerminatingSequences(SPMM, ProceduralInputAlphabet)
     */
    public static <I, O> ATSequences<I> computeATSequences(SPMM<?, I, ?, O> spmm, ProceduralInputAlphabet<I> alphabet) {

        assert isValid(spmm, alphabet);

        final Map<I, Word<I>> terminatingSequences = computeTerminatingSequences(spmm, alphabet);
        final Map<I, Word<I>> accessSequences = computeAccessSequences(spmm, alphabet, terminatingSequences);

        return new ATSequences<>(accessSequences, terminatingSequences);
    }

    /**
     * Computes for a given {@link SPMM} the set of terminating sequences using the given
     * {@link ProceduralInputAlphabet alphabet}. Terminating sequences transfer a procedure from its initial state to a
     * returnable state. This method furthermore checks that the hierarchy of calls is well-defined, i.e. it only
     * includes procedural invocations <i>p</i> for determining a terminating sequence if <i>p</i> has a valid
     * terminating sequence itself.
     *
     * @param spmm
     *         the {@link SPMM} to analyze
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for computing the terminating sequences
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return A map from procedures (restricted to the call symbols of the given alphabet) to the terminating
     * sequences. This map may be partial as some procedures may not have a well-defined terminating sequence for the
     * given alphabet.
     */
    public static <I, O> Map<I, Word<I>> computeTerminatingSequences(SPMM<?, I, ?, O> spmm,
                                                                     ProceduralInputAlphabet<I> alphabet) {
        final Word<I> returnWord = Word.fromLetter(alphabet.getReturnSymbol());

        return ProceduralUtil.computeTerminatingSequences(spmm.getProcedures(), alphabet, (mealy, trace) -> {
            final Word<O> output = mealy.computeSuffixOutput(trace, returnWord);
            return !output.isEmpty() && !spmm.isErrorOutput(output.lastSymbol());
        });
    }

    /**
     * Computes for a given {@link SPMM} a set of access sequences using the SPMM
     * {@link ProceduralInputAlphabet alphabet}. An access sequence (for procedure <i>p</i>) transfers an {@link SPMM}
     * from its initial state to a state that is able to successfully execute a run of <i>p</i>. This method furthermore
     * checks that potentially nested calls are well-defined, i.e. it only includes procedural invocations
     * <i>p</i> for determining access sequences if <i>p</i> has a valid terminating sequence and therefore can
     * be expanded correctly.
     *
     * @param spmm
     *         the {@link SPMM} to analyze
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for computing the access sequences
     * @param terminatingSequences
     *         a {@link Map} of call symbols to terminating sequences used to expand nested invocations in access
     *         sequences
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return A map from procedures (restricted to the call symbols of the given alphabet) to the access sequences.
     * This map may be partial as some procedures may not have well-defined access sequences for the given alphabet.
     */
    public static <I, O> Map<I, Word<I>> computeAccessSequences(SPMM<?, I, ?, O> spmm,
                                                                ProceduralInputAlphabet<I> alphabet,
                                                                Map<I, Word<I>> terminatingSequences) {
        return ProceduralUtil.computeAccessSequences(spmm.getProcedures(),
                                                     alphabet,
                                                     spmm.getProceduralInputs(alphabet),
                                                     spmm.getInitialProcedure(),
                                                     terminatingSequences,
                                                     (mealy, trace) -> !spmm.isErrorOutput(mealy.computeOutput(trace)
                                                                                                .lastSymbol()));
    }

    /**
     * Checks whether the given {@link SPMM} is valid, This is a convenience method for
     * {@link #isValid(SPMM, ProceduralInputAlphabet)} that uses the {@link SPMM#getInputAlphabet() input alphabet} of
     * the given {@link SPMM}.
     *
     * @param spmm
     *         the {@link SPMM} to analyze
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return {@code true} if {@code spmm} is valid, {@code false} otherwise.
     *
     * @see #isValid(SPMM, ProceduralInputAlphabet)
     */
    public static <I, O> boolean isValid(SPMM<?, I, ?, O> spmm) {
        return isValid(spmm, spmm.getInputAlphabet());
    }

    /**
     * Checks whether the given {@link SPMM} is valid with respect to the given {@link ProceduralInputAlphabet}, i.e.,
     * whether its {@link SPMM#getProcedures() procedures} are error-closed, return-closed, and call-closed.
     * <p>
     * A procedure is considered error-closed iff any transition that emits an
     * {@link SPMM#getErrorOutput() error output} transitions the procedure into a sink state that continues to output
     * the {@link SPMM#getErrorOutput() error output}.
     * <p>
     * A procedure is considered return-closed iff the {@link ProceduralInputAlphabet#getReturnSymbol() return symbol}
     * transitions the procedure into a sink state that continues to output the
     * {@link SPMM#getErrorOutput() error output}.
     * <p>
     * A procedure is considered call-closed iff any transition labeled with a non-terminating
     * {@link ProceduralInputAlphabet#getCallAlphabet() call symbol} transitions the procedure into a sink state that
     * continues to output the {@link SPMM#getErrorOutput() error output}.
     *
     * @param spmm
     *         the {@link SPMM} to analyze
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for checking validity
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return {@code true} if {@code spmm} is valid, {@code false} otherwise.
     */
    public static <I, O> boolean isValid(SPMM<?, I, ?, O> spmm, ProceduralInputAlphabet<I> alphabet) {

        final Map<I, Word<I>> ts = computeTerminatingSequences(spmm, alphabet);
        final Collection<I> proceduralInputs = spmm.getProceduralInputs(alphabet);
        final Set<I> nonContinuableSymbols = new HashSet<>(alphabet.getCallAlphabet());
        nonContinuableSymbols.removeAll(ts.keySet());
        nonContinuableSymbols.retainAll(proceduralInputs);
        nonContinuableSymbols.add(alphabet.getReturnSymbol());

        for (MealyMachine<?, I, ?, O> p : spmm.getProcedures().values()) {
            if (!isErrorReturnAndCallClosed(p, proceduralInputs, nonContinuableSymbols, spmm.getErrorOutput())) {
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

    /**
     * Checks if the two given {@link SPMM}s are equivalent, i.e. whether there exists a
     * {@link #findSeparatingWord(SPMM, SPMM, ProceduralInputAlphabet) separating word} for them.
     *
     * @param spmm1
     *         the first {@link SPMM}
     * @param spmm2
     *         the second {@link SPMM}
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for checking equivalence
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return {@code true} if the two {@link SPMM}s are equivalent, {@code false} otherwise.
     *
     * @see #findSeparatingWord(SPMM, SPMM, ProceduralInputAlphabet)
     */
    public static <I, O> boolean testEquivalence(SPMM<?, I, ?, O> spmm1,
                                                 SPMM<?, I, ?, O> spmm2,
                                                 ProceduralInputAlphabet<I> alphabet) {
        return findSeparatingWord(spmm1, spmm2, alphabet) == null;
    }

    /**
     * Computes a separating word for the two given {@link SPMM}s, if existent. A separating word is a {@link Word} such
     * that one {@link SPMM} {@link SPMM#computeOutput(Iterable) behaves} different from the other.
     *
     * @param spmm1
     *         the first {@link SPMM}
     * @param spmm2
     *         the second {@link SPMM}
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for computing the separating word
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a separating word, if existent, {@code null} otherwise.
     */
    public static <I, O> @Nullable Word<I> findSeparatingWord(SPMM<?, I, ?, O> spmm1,
                                                              SPMM<?, I, ?, O> spmm2,
                                                              ProceduralInputAlphabet<I> alphabet) {

        final ATSequences<I> at1 = computeATSequences(spmm1, alphabet);
        final ATSequences<I> at2 = computeATSequences(spmm2, alphabet);

        return ProceduralUtil.findSeparatingWord(spmm1.getProcedures(), at1, spmm2.getProcedures(), at2, alphabet);
    }
}
