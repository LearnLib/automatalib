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
package net.automatalib.util.automata.sba;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.sba.SBA;
import net.automatalib.automata.spa.SPA;
import net.automatalib.automata.spa.StackSPA;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.automata.fsa.MutableDFAs;
import net.automatalib.util.automata.predicates.TransitionPredicates;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
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

    public static <I> ATSequences<I> computeATSequences(SBA<?, I> sba, SPAAlphabet<I> alphabet) {

        assert isValid(sba, alphabet);

        final Map<I, Word<I>> terminatingSequences = computeTerminatingSequences(sba, alphabet);
        final Map<I, Word<I>> accessSequences = computeAccessSequences(sba, alphabet, terminatingSequences);

        return new ATSequences<>(accessSequences, terminatingSequences);
    }

    public static <I> Map<I, Word<I>> computeTerminatingSequences(SBA<?, I> sba, SPAAlphabet<I> alphabet) {

        final Map<I, DFA<?, I>> procedures = sba.getProcedures();
        final Map<I, Word<I>> terminatingSequences = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());

        // initial internal sequences
        for (I procedure : alphabet.getCallAlphabet()) {
            final DFA<?, I> dfa = procedures.get(procedure);

            if (dfa != null) {
                final Iterator<Word<I>> iter = Covers.stateCoverIterator(dfa, alphabet.getInternalAlphabet());
                while (iter.hasNext()) {
                    final Word<I> trace = iter.next();
                    if (dfa.accepts(trace.append(alphabet.getReturnSymbol()))) {
                        terminatingSequences.put(procedure, trace);
                        break;
                    }
                }
            }
        }

        final Set<I> remainingProcedures = new HashSet<>(alphabet.getCallAlphabet());
        remainingProcedures.removeAll(terminatingSequences.keySet());

        final Set<I> eligibleInputs = new HashSet<>(alphabet.getInternalAlphabet());
        eligibleInputs.addAll(terminatingSequences.keySet());

        boolean stable = false;

        while (!stable) {
            stable = true;

            for (final I i : new ArrayList<>(remainingProcedures)) {

                final DFA<?, I> dfa = procedures.get(i);

                if (dfa == null) {
                    // for non-existing procedures we cannot compute any terminating sequences
                    remainingProcedures.remove(i);
                } else {
                    final Iterator<Word<I>> iter = Covers.stateCoverIterator(dfa, eligibleInputs);

                    while (iter.hasNext()) {
                        final Word<I> trace = iter.next();

                        if (dfa.accepts(trace.append(alphabet.getReturnSymbol()))) {
                            terminatingSequences.put(i, alphabet.expand(trace, terminatingSequences::get));
                            remainingProcedures.remove(i);
                            eligibleInputs.add(i);
                            stable = false;
                            break;
                        }
                    }
                }
            }
        }

        return terminatingSequences;
    }

    public static <I> Map<I, Word<I>> computeAccessSequences(SBA<?, I> sba,
                                                             SPAAlphabet<I> alphabet,
                                                             Map<I, Word<I>> terminatingSequences) {
        final I initialProcedure = sba.getInitialProcedure();

        if (initialProcedure == null) {
            return Collections.emptyMap();
        }

        final Map<I, DFA<?, I>> procedures = sba.getProcedures();
        final Map<I, Word<I>> accessSequences = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
        final Set<I> finishedProcedures = Sets.newHashSetWithExpectedSize(alphabet.getNumCalls());

        // initial value
        accessSequences.put(initialProcedure, Word.fromLetter(initialProcedure));
        finishedProcedures.add(initialProcedure);

        final Deque<I> pendingProcedures = new ArrayDeque<>();
        pendingProcedures.add(initialProcedure);

        while (!pendingProcedures.isEmpty()) {
            final I i = pendingProcedures.pop();
            final DFA<?, I> dfa = procedures.get(i);

            if (dfa != null) {
                final Collection<I> newProcedures = discoverAccessSequences(alphabet,
                                                                            i,
                                                                            dfa,
                                                                            finishedProcedures,
                                                                            accessSequences,
                                                                            terminatingSequences);
                pendingProcedures.addAll(newProcedures);
            }
        }

        return accessSequences;
    }

    private static <S, I> Collection<I> discoverAccessSequences(SPAAlphabet<I> alphabet,
                                                                I procedure,
                                                                DFA<S, I> dfa,
                                                                Set<I> finishedProcedures,
                                                                Map<I, Word<I>> accessSequences,
                                                                Map<I, Word<I>> terminatingSequences) {

        final List<I> newAS = new ArrayList<>();
        final Iterator<Word<I>> transitionCoverIterator = Covers.transitionCoverIterator(dfa, alphabet);

        tc:
        while (transitionCoverIterator.hasNext()) {
            final Word<I> trace = transitionCoverIterator.next();

            S iter = dfa.getInitialState();

            for (int i = 0; i < trace.length(); i++) {
                final I input = trace.getSymbol(i);

                if (alphabet.isCallSymbol(input)) {
                    if (!finishedProcedures.contains(input)) {

                        final S succ = dfa.getSuccessor(iter, input);

                        if (dfa.isAccepting(succ)) {

                            final WordBuilder<I> accessBuilder = new WordBuilder<>();
                            final Word<I> as = accessSequences.get(procedure);
                            accessBuilder.append(as);
                            accessBuilder.append(alphabet.expand(trace.subWord(0, i), terminatingSequences::get));
                            accessBuilder.append(input);

                            accessSequences.put(input, accessBuilder.toWord());

                            finishedProcedures.add(input);
                            newAS.add(input);
                        } else {
                            // If we encounter a failing call we land in a sink state and don't need to analyse further
                            // transitions. Therefore, skip the remaining trace.
                            continue tc;
                        }
                    } else if (!terminatingSequences.containsKey(input)) {
                        // If we encounter a call symbol for which we do not have a terminating sequence,
                        // all local access sequences of future call symbols cannot be expanded properly.
                        // Therefore, skip the remaining trace.
                        continue tc;
                    }
                }

                iter = dfa.getSuccessor(iter, input);
            }

            if (finishedProcedures.containsAll(alphabet.getCallAlphabet())) {
                return newAS;
            }
        }
        return newAS;
    }

    public static <I> boolean isValid(SBA<?, I> sba) {
        return isValid(sba, sba.getInputAlphabet());
    }

    public static <I> boolean isValid(SBA<?, I> sba, SPAAlphabet<I> alphabet) {

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
                                                        SPAAlphabet<I> alphabet,
                                                        Set<I> nonContinuableSymbols) {

        for (S s : procedure) {
            for (I i : nonContinuableSymbols) {
                final S succ = procedure.getSuccessor(s, i);
                final S toAnalyze;

                if (procedure.isAccepting(succ)) {
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

                if (!isSink(procedure, alphabet, toAnalyze)) {
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
            if (!Objects.equals(succ, state)) {
                return false;
            }
        }

        return true;
    }

    public static <I> boolean testEquivalence(SBA<?, I> sba1, SBA<?, I> sba2, SPAAlphabet<I> alphabet) {
        return findSeparatingWord(sba1, sba2, alphabet) == null;
    }

    public static <I> @Nullable Word<I> findSeparatingWord(SBA<?, I> sba1, SBA<?, I> sba2, SPAAlphabet<I> alphabet) {

        final ATSequences<I> at1 = computeATSequences(sba1, alphabet);
        final ATSequences<I> at2 = computeATSequences(sba2, alphabet);

        for (final I procedure : alphabet.getCallAlphabet()) {
            final DFA<?, I> p1 = sba1.getProcedures().get(procedure);
            final DFA<?, I> p2 = sba2.getProcedures().get(procedure);

            if (p1 != null && p2 != null) {
                final Word<I> as1 = at1.accessSequences.get(procedure);
                final Word<I> ts1 = at1.terminatingSequences.get(procedure);

                final Word<I> as2 = at2.accessSequences.get(procedure);
                final Word<I> ts2 = at2.terminatingSequences.get(procedure);

                if (as1 != null && as2 != null) {
                    // we can access both procedures

                    if (ts1 == null && ts2 != null) {
                        return Word.fromWords(as2, ts2, Word.fromLetter(alphabet.getReturnSymbol()));
                    } else if (ts1 != null && ts2 == null) {
                        return Word.fromWords(as1, ts1, Word.fromLetter(alphabet.getReturnSymbol()));
                    }

                    final Word<I> sepWord = Automata.findSeparatingWord(p1, p2, alphabet);

                    if (sepWord != null) {
                        // deterministically select at1 because any mismatch will suffice for a counterexample
                        final Word<I> as = at1.accessSequences.get(procedure);

                        if (sepWord.isEmpty()) {
                            return as;
                        } else {
                            // do not expand the last symbol, because it may be an open call
                            final Word<I> expandedSepWord =
                                    alphabet.expand(sepWord.prefix(-1), at1.terminatingSequences::get);
                            return Word.fromWords(as, expandedSepWord, Word.fromLetter(sepWord.lastSymbol()));
                        }
                    }
                } else if (as1 == null && as2 != null) {
                    return as2;
                } else if (as1 != null) {
                    return as1;
                } // else no procedures are accessible
            } else if (p1 != null) {
                final Word<I> as = at1.accessSequences.get(procedure);

                if (as != null) {
                    return as;
                }
            } else if (p2 != null) {
                final Word<I> as = at2.accessSequences.get(procedure);

                if (as != null) {
                    return as;
                }
            } // else both procedures are null and therefore skip this call symbol
        }

        return null;
    }

    public static <I> SPA<?, I> reduce(SBA<?, I> sba) {
        final SPAAlphabet<I> alphabet = sba.getInputAlphabet();

        final Map<I, DFA<?, I>> procedures = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
        for (Entry<I, DFA<?, I>> e : sba.getProcedures().entrySet()) {
            procedures.put(e.getKey(), reduce(e.getValue(), alphabet));
        }

        return new StackSPA<>(alphabet, sba.getInitialProcedure(), procedures);
    }

    private static <S, I> DFA<?, I> reduce(DFA<S, I> dfa, SPAAlphabet<I> alphabet) {
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

    public static <I> List<Word<I>> characterizingSet(SBA<?, I> sul, SPAAlphabet<I> alphabet) {

        final List<Word<I>> tests = new ArrayList<>();
        final ATSequences<I> ats = SBAUtil.computeATSequences(sul, alphabet);

        final Set<I> eligibleInputs = new HashSet<>(ats.terminatingSequences.keySet());
        eligibleInputs.addAll(alphabet.getInternalAlphabet());

        for (Entry<I, DFA<?, I>> e : sul.getProcedures().entrySet()) {
            final Word<I> as = ats.accessSequences.get(e.getKey());
            final DFA<?, I> dfa = e.getValue();

            final List<Word<I>> sCov = Automata.stateCover(dfa, eligibleInputs);
            final List<Word<I>> cSet = Automata.characterizingSet(dfa, alphabet);

            for (Word<I> c : sCov) {
                for (I i : alphabet) {
                    if (!alphabet.isCallSymbol(i) || ats.terminatingSequences.containsKey(i)) {
                        final Word<I> ts = c.append(i);
                        for (Word<I> cs : cSet) {
                            tests.add(as.concat(alphabet.expand(ts.concat(cs), ats.terminatingSequences::get)));
                        }
                    } else {
                        tests.add(as.concat(alphabet.expand(c, ats.terminatingSequences::get)).append(i));
                    }
                }
            }
        }

        return tests;
    }
}
