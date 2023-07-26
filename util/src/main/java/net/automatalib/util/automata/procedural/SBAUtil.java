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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.procedural.SBA;
import net.automatalib.automata.procedural.SPA;
import net.automatalib.automata.procedural.StackSPA;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.automata.fsa.MutableDFAs;
import net.automatalib.util.automata.predicates.TransitionPredicates;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author frohme
 */
public final class SBAUtil {

    private SBAUtil() {
        // prevent instantiation
    }

    public static <I> ATSequences<I> computeATSequences(SBA<?, I> sba) {
        return computeATSequences(sba, sba.getInputAlphabet());
    }

    public static <I> ATSequences<I> computeATSequences(SBA<?, I> sba, ProceduralInputAlphabet<I> alphabet) {

        assert isValid(sba, alphabet);

        final Map<I, Word<I>> terminatingSequences = computeTerminatingSequences(sba, alphabet);
        final Map<I, Word<I>> accessSequences = computeAccessSequences(sba, alphabet, terminatingSequences);

        return new ATSequences<>(accessSequences, terminatingSequences);
    }

    public static <I> Map<I, Word<I>> computeTerminatingSequences(SBA<?, I> sba, ProceduralInputAlphabet<I> alphabet) {
        final Word<I> returnWord = Word.fromLetter(alphabet.getReturnSymbol());
        return ProceduralUtil.computeTerminatingSequences(sba.getProcedures(),
                                                          alphabet,
                                                          (dfa, trace) -> dfa.computeSuffixOutput(trace, returnWord));
    }

    public static <I> Map<I, Word<I>> computeAccessSequences(SBA<?, I> sba,
                                                             ProceduralInputAlphabet<I> alphabet,
                                                             Map<I, Word<I>> terminatingSequences) {

        return ProceduralUtil.computeAccessSequences(sba.getProcedures(),
                                                     alphabet,
                                                     sba.getInitialProcedure(),
                                                     terminatingSequences,
                                                     DFA::accepts);
    }

    public static <I> boolean isValid(SBA<?, I> sba) {
        return isValid(sba, sba.getInputAlphabet());
    }

    public static <I> boolean isValid(SBA<?, I> sba, ProceduralInputAlphabet<I> alphabet) {

        final Map<I, Word<I>> ts = computeTerminatingSequences(sba, alphabet);
        final Set<I> nonContinuableSymbols = new HashSet<>(alphabet.getCallAlphabet());
        nonContinuableSymbols.removeAll(ts.keySet());
        nonContinuableSymbols.add(alphabet.getReturnSymbol());

        for (DFA<?, I> p : sba.getProcedures().values()) {
            if (!DFAs.isPrefixClosed(p, alphabet) || !isCallAndReturnClosed(p, alphabet, nonContinuableSymbols)) {
                return false;
            }
        }

        return true;
    }

    private static <S, I> boolean isCallAndReturnClosed(DFA<S, I> procedure,
                                                        ProceduralInputAlphabet<I> alphabet,
                                                        Set<I> nonContinuableSymbols) {

        for (S s : procedure) {
            for (I i : nonContinuableSymbols) {
                final S succ = procedure.getSuccessor(s, i);
                final S toAnalyze;

                if (succ != null && procedure.isAccepting(succ)) {
                    toAnalyze = procedure.getSuccessor(succ, i);

                    // ensure that toAnalyze is effectively a "success sink"
                    for (I i2 : alphabet) {
                        if (!Objects.equals(procedure.getSuccessor(succ, i2), toAnalyze)) {
                            return false;
                        }
                    }
                } else {
                    toAnalyze = succ;
                }

                if (toAnalyze != null && !isSink(procedure, alphabet, toAnalyze)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static <S, I> boolean isSink(DFA<S, I> dfa, Collection<? extends I> inputs, S state) {

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

    public static <I> boolean testEquivalence(SBA<?, I> sba1, SBA<?, I> sba2, ProceduralInputAlphabet<I> alphabet) {
        return findSeparatingWord(sba1, sba2, alphabet) == null;
    }

    public static <I> @Nullable Word<I> findSeparatingWord(SBA<?, I> sba1,
                                                           SBA<?, I> sba2,
                                                           ProceduralInputAlphabet<I> alphabet) {

        final ATSequences<I> at1 = computeATSequences(sba1, alphabet);
        final ATSequences<I> at2 = computeATSequences(sba2, alphabet);

        return ProceduralUtil.findSeparatingWord(sba1.getProcedures(), at1, sba2.getProcedures(), at2, alphabet);
    }

    public static <I> SPA<?, I> reduce(SBA<?, I> sba) {
        final ProceduralInputAlphabet<I> alphabet = sba.getInputAlphabet();

        final Map<I, DFA<?, I>> procedures = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
        for (Entry<I, DFA<?, I>> e : sba.getProcedures().entrySet()) {
            procedures.put(e.getKey(), reduce(e.getValue(), alphabet));
        }

        return new StackSPA<>(alphabet, sba.getInitialProcedure(), procedures);
    }

    private static <S, I> DFA<?, I> reduce(DFA<S, I> dfa, ProceduralInputAlphabet<I> alphabet) {
        final MutableDFA<Integer, I> result = new CompactDFA<>(alphabet.getProceduralAlphabet());

        final Function<S, Boolean> spMapping = s -> dfa.isAccepting(dfa.getSuccessor(s, alphabet.getReturnSymbol()));
        final TransitionPredicate<S, I, S> transFilter = TransitionPredicates.inputIsNot(alphabet.getReturnSymbol());

        AutomatonLowLevelCopy.rawCopy(AutomatonCopyMethod.BFS,
                                      dfa,
                                      alphabet,
                                      result,
                                      spMapping,
                                      o -> null,
                                      o -> true,
                                      transFilter);

        MutableDFAs.complete(result, alphabet.getProceduralAlphabet(), true);
        return result;
    }

}
