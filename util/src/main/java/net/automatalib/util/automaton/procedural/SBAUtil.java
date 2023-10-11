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
package net.automatalib.util.automaton.procedural;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.impl.compact.CompactDFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.procedural.StackSPA;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.util.automaton.copy.AutomatonCopyMethod;
import net.automatalib.util.automaton.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automaton.fsa.DFAs;
import net.automatalib.util.automaton.fsa.MutableDFAs;
import net.automatalib.util.automaton.predicate.TransitionPredicates;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility methods for {@link SBA}s.
 */
public final class SBAUtil {

    private SBAUtil() {
        // prevent instantiation
    }

    /**
     * Computes a set of access sequences and terminating sequences for a given {@link SBA}. This is a convenience
     * method for {@link #computeATSequences(SBA, ProceduralInputAlphabet)} that automatically uses the
     * {@link SBA#getInputAlphabet() input alphabet} of the given {@code sba}.
     *
     * @param sba
     *         the {@link SBA} for which the sequences should be computed
     * @param <I>
     *         input symbol type
     *
     * @return an {@link ATSequences} object which contains the respective sequences.
     *
     * @see #computeATSequences(SBA, ProceduralInputAlphabet)
     */
    public static <I> ATSequences<I> computeATSequences(SBA<?, I> sba) {
        return computeATSequences(sba, sba.getInputAlphabet());
    }

    /**
     * Computes a set of access sequences and terminating sequences for a given {@link SBA} limited to the symbols of
     * the given {@link ProceduralInputAlphabet}.
     *
     * @param sba
     *         the {@link SBA} for which the sequences should be computed
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for computing the respective sequences
     * @param <I>
     *         input symbol type
     *
     * @return an {@link ATSequences} object which contains the respective sequences.
     *
     * @see #computeAccessSequences(SBA, ProceduralInputAlphabet, Map)
     * @see #computeTerminatingSequences(SBA, ProceduralInputAlphabet)
     */
    public static <I> ATSequences<I> computeATSequences(SBA<?, I> sba, ProceduralInputAlphabet<I> alphabet) {

        assert isValid(sba, alphabet);

        final Map<I, Word<I>> terminatingSequences = computeTerminatingSequences(sba, alphabet);
        final Map<I, Word<I>> accessSequences = computeAccessSequences(sba, alphabet, terminatingSequences);

        return new ATSequences<>(accessSequences, terminatingSequences);
    }

    /**
     * Computes for a given {@link SBA} a set of terminating sequences using the given
     * {@link ProceduralInputAlphabet alphabet}. Terminating sequences transfer a procedure from its initial state to a
     * returnable state. This method furthermore checks that the hierarchy of calls is well-defined, i.e. it only
     * includes procedural invocations <i>p</i> for determining a terminating sequence if <i>p</i> has a valid
     * terminating sequence itself.
     *
     * @param sba
     *         the {@link SBA} to analyze
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for determining the terminating
     *         sequences
     * @param <I>
     *         input symbol type
     *
     * @return A map from procedures (restricted to the call symbols of the given alphabet) to the terminating
     * sequences. This map may be partial as some procedures may not have a well-defined terminating sequence for the
     * given alphabet.
     */
    public static <I> Map<I, Word<I>> computeTerminatingSequences(SBA<?, I> sba, ProceduralInputAlphabet<I> alphabet) {
        final Word<I> returnWord = Word.fromLetter(alphabet.getReturnSymbol());
        return ProceduralUtil.computeTerminatingSequences(sba.getProcedures(),
                                                          alphabet,
                                                          (dfa, trace) -> dfa.computeSuffixOutput(trace, returnWord));
    }

    /**
     * Computes for a given {@link SBA} a set of access sequences using the given
     * {@link ProceduralInputAlphabet alphabet}. An access sequence (for procedure <i>p</i>) transfers an {@link SBA}
     * from its initial state to a state that is able to successfully execute a run of <i>p</i>. This method furthermore
     * checks that potentially nested calls are well-defined, i.e. it only includes procedural invocations <i>p</i> for
     * determining access sequences if <i>p</i> has a valid terminating sequence and therefore can be expanded
     * correctly.
     *
     * @param sba
     *         the {@link SBA} to analyze
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for determining the access and return
     *         sequences
     * @param terminatingSequences
     *         a {@link Map} of call symbols to terminating sequences used to expand nested invocations in access
     *         sequences
     * @param <I>
     *         input symbol type
     *
     * @return A pair of maps from procedures (restricted to the call symbols of the given alphabet) to the
     * access/return sequences. These maps may be partial as some procedures may not have well-defined
     * access/terminating sequences for the given alphabet.
     */
    public static <I> Map<I, Word<I>> computeAccessSequences(SBA<?, I> sba,
                                                             ProceduralInputAlphabet<I> alphabet,
                                                             Map<I, Word<I>> terminatingSequences) {

        return ProceduralUtil.computeAccessSequences(sba.getProcedures(),
                                                     alphabet,
                                                     sba.getProceduralInputs(alphabet),
                                                     sba.getInitialProcedure(),
                                                     terminatingSequences,
                                                     DFA::accepts);
    }

    /**
     * Checks whether the given {@link SBA} is valid, This is a convenience method for
     * {@link #isValid(SBA, ProceduralInputAlphabet)} that uses the {@link SBA#getInputAlphabet() input alphabet} of the
     * given {@link SBA}.
     *
     * @param sba
     *         the {@link SBA} to analyze
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if {@code sba} is valid, {@code false} otherwise.
     *
     * @see #isValid(SBA, ProceduralInputAlphabet)
     */
    public static <I> boolean isValid(SBA<?, I> sba) {
        return isValid(sba, sba.getInputAlphabet());
    }

    /**
     * Checks whether the given {@link SBA} is valid with respect to the given {@link ProceduralInputAlphabet}, i.e.,
     * whether its {@link SBA#getProcedures() procedures} are prefix-closed, return-closed, and call-closed.
     * <p>
     * A procedure is considered prefix-closed iff any continuation of a rejected word is rejected as well.
     * <p>
     * A procedure is considered return-closed iff any continuation beyond the
     * {@link ProceduralInputAlphabet#getReturnSymbol() return symbol} is rejected.
     * <p>
     * A procedure is considered call-closed iff any continuation beyond a non-terminating
     * {@link ProceduralInputAlphabet#getCallAlphabet() call symbol} is rejected.
     *
     * @param sba
     *         the {@link SBA} to analyze
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for checking validity
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if {@code sba} is valid, {@code false} otherwise.
     */
    public static <I> boolean isValid(SBA<?, I> sba, ProceduralInputAlphabet<I> alphabet) {

        final Map<I, Word<I>> ts = computeTerminatingSequences(sba, alphabet);
        final Collection<I> proceduralInputs = sba.getProceduralInputs(alphabet);
        final Set<I> nonContinuableSymbols = new HashSet<>(alphabet.getCallAlphabet());
        nonContinuableSymbols.removeAll(ts.keySet());
        nonContinuableSymbols.retainAll(proceduralInputs);
        nonContinuableSymbols.add(alphabet.getReturnSymbol());

        for (DFA<?, I> p : sba.getProcedures().values()) {
            if (!DFAs.isPrefixClosed(p, proceduralInputs) ||
                !isCallAndReturnClosed(p, proceduralInputs, nonContinuableSymbols)) {
                return false;
            }
        }

        return true;
    }

    private static <S, I> boolean isCallAndReturnClosed(DFA<S, I> procedure,
                                                        Collection<I> inputs,
                                                        Collection<I> nonContinuableInputs) {

        for (S s : procedure) {
            for (I i : nonContinuableInputs) {
                final S succ = procedure.getSuccessor(s, i);
                final S toAnalyze;

                if (succ != null && procedure.isAccepting(succ)) {
                    toAnalyze = procedure.getSuccessor(succ, i);

                    // ensure that toAnalyze is effectively a "success sink"
                    for (I i2 : inputs) {
                        if (!Objects.equals(procedure.getSuccessor(succ, i2), toAnalyze)) {
                            return false;
                        }
                    }
                } else {
                    toAnalyze = succ;
                }

                if (toAnalyze != null && !isSink(procedure, inputs, toAnalyze)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static <S, I> boolean isSink(DFA<S, I> dfa, Collection<I> inputs, S state) {

        if (dfa.isAccepting(state)) {
            return false;
        }

        for (I i : inputs) {
            final S succ = dfa.getSuccessor(state, i);
            if (succ != null && !Objects.equals(succ, state)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the two given {@link SBA}s are equivalent, i.e. whether there exists a
     * {@link #findSeparatingWord(SBA, SBA, ProceduralInputAlphabet) separating word} for them.
     *
     * @param sba1
     *         the first {@link SPMM}
     * @param sba2
     *         the second {@link SPMM}
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for checking equivalence
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if the two {@link SBA}s are equivalent, {@code false} otherwise.
     *
     * @see #findSeparatingWord(SBA, SBA, ProceduralInputAlphabet)
     */
    public static <I> boolean testEquivalence(SBA<?, I> sba1, SBA<?, I> sba2, ProceduralInputAlphabet<I> alphabet) {
        return findSeparatingWord(sba1, sba2, alphabet) == null;
    }

    /**
     * Computes a separating word for the two given {@link SBA}s, if existent. A separating word is a {@link Word} such
     * that one {@link SBA} {@link SBA#accepts(Iterable) behaves} different from the other.
     *
     * @param sba1
     *         the first {@link SBA}
     * @param sba2
     *         the second {@link SBA}
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be considered for computing the separating word
     * @param <I>
     *         input symbol type
     *
     * @return a separating word, if existent, {@code null} otherwise.
     */
    public static <I> @Nullable Word<I> findSeparatingWord(SBA<?, I> sba1,
                                                           SBA<?, I> sba2,
                                                           ProceduralInputAlphabet<I> alphabet) {

        final ATSequences<I> at1 = computeATSequences(sba1, alphabet);
        final ATSequences<I> at2 = computeATSequences(sba2, alphabet);

        return ProceduralUtil.findSeparatingWord(sba1.getProcedures(), at1, sba2.getProcedures(), at2, alphabet);
    }

    /**
     * Reduces a given {@link SBA} to its well-matched language. This is a convenience method for
     * {@link #reduce(SBA, ProceduralInputAlphabet)} that uses the {@link SBA#getInputAlphabet() input alphabet} of the
     * given {@link SBA}.
     *
     * @param sba
     *         the {@link SBA} to reduce
     * @param <I>
     *         input symbol type
     *
     * @return the reduced {@link SBA} in form of an {@link SPA}
     *
     * @see #reduce(SBA, ProceduralInputAlphabet)
     */
    public static <I> SPA<?, I> reduce(SBA<?, I> sba) {
        return reduce(sba, sba.getInputAlphabet());
    }

    /**
     * Reduces a given {@link SBA} to its well-matched language restricted to the symbols of the given
     * {@link ProceduralInputAlphabet}. The reduced {@link SBA} only accepts a {@link Word} iff it is (minimally)
     * well-matched and accepted by the original {@link SBA} as well.
     *
     * @param sba
     *         the {@link SBA} to reduce
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be considered for reduction
     * @param <I>
     *         input symbol type
     *
     * @return the reduced {@link SBA} in form of an {@link SPA}
     */
    public static <I> SPA<?, I> reduce(SBA<?, I> sba, ProceduralInputAlphabet<I> alphabet) {
        final Map<I, DFA<?, I>> procedures = sba.getProcedures();
        final Map<I, DFA<?, I>> spaProcedures = Maps.newHashMapWithExpectedSize(procedures.size());
        final Collection<I> proceduralInputs = sba.getProceduralInputs(alphabet);
        proceduralInputs.remove(alphabet.getReturnSymbol());

        for (Entry<I, DFA<?, I>> e : procedures.entrySet()) {
            spaProcedures.put(e.getKey(), reduce(e.getValue(), alphabet, proceduralInputs));
        }

        // explicit type specification is required by checker-framework
        return new StackSPA<@Nullable Object, I>(alphabet, sba.getInitialProcedure(), spaProcedures);
    }

    private static <S, I> DFA<?, I> reduce(DFA<S, I> dfa,
                                           ProceduralInputAlphabet<I> alphabet,
                                           Collection<I> sourceInputs) {

        final I returnSymbol = alphabet.getReturnSymbol();
        final Alphabet<I> proceduralAlphabet = alphabet.getProceduralAlphabet();

        final Function<S, Boolean> spMapping = s -> {
            final S succ = dfa.getSuccessor(s, returnSymbol);
            return succ != null && dfa.isAccepting(succ);
        };
        final TransitionPredicate<S, I, S> transFilter = TransitionPredicates.inputIsNot(returnSymbol);

        final MutableDFA<Integer, I> result = new CompactDFA<>(proceduralAlphabet);
        AutomatonLowLevelCopy.rawCopy(AutomatonCopyMethod.BFS,
                                      dfa,
                                      sourceInputs,
                                      result,
                                      spMapping,
                                      o -> null,
                                      o -> true,
                                      transFilter);

        MutableDFAs.complete(result, proceduralAlphabet, true);
        return result;
    }

    /**
     * Returns a {@link ContextFreeModalProcessSystem}-based view on the language of a given {@link SBA} such that there
     * exists a {@code w}-labeled path in the returned CFMPS if and only if {@code w} is accepted by the given
     * {@link SBA}. This allows one to model-check language properties of {@link SBA}s with tools such as M3C.
     *
     * @param sba
     *         the {@link SBA} to convert
     * @param <I>
     *         input symbol type
     *
     * @return the {@link ContextFreeModalProcessSystem}-based view on the given {@code sba}.
     */
    public static <I> ContextFreeModalProcessSystem<I, Void> toCFMPS(SBA<?, I> sba) {
        assert SBAUtil.isValid(sba);
        return new CFMPSViewSBA<>(sba);
    }

}
