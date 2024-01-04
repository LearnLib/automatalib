/* Copyright (C) 2013-2024 TU Dortmund University
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.cover.Covers;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

final class ProceduralUtil {

    private ProceduralUtil() {
        // prevent instantiation
    }

    static <I, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>> Map<I, Word<I>> computeTerminatingSequences(Map<I, ? extends M> procedures,
                                                                                                                     ProceduralInputAlphabet<I> alphabet,
                                                                                                                     BiPredicate<M, Word<I>> tracePredicate) {

        final Map<I, Word<I>> terminatingSequences = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());

        // initial internal sequences
        for (I procedure : alphabet.getCallAlphabet()) {
            final M p = procedures.get(procedure);

            if (p != null) {
                final Iterator<Word<I>> iter = Covers.stateCoverIterator(p, alphabet.getInternalAlphabet());
                while (iter.hasNext()) {
                    final Word<I> trace = iter.next();
                    if (tracePredicate.test(p, trace)) {
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

            for (I i : new ArrayList<>(remainingProcedures)) {

                final M p = procedures.get(i);

                if (p == null) {
                    // for non-existing procedures we cannot compute any terminating sequences
                    remainingProcedures.remove(i);
                } else {
                    final Iterator<Word<I>> iter = Covers.stateCoverIterator(p, eligibleInputs);

                    while (iter.hasNext()) {
                        final Word<I> trace = iter.next();
                        if (tracePredicate.test(p, trace)) {
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

    static <I, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>> Map<I, Word<I>> computeAccessSequences(Map<I, ? extends M> procedures,
                                                                                                                ProceduralInputAlphabet<I> alphabet,
                                                                                                                Collection<I> proceduralInputs,
                                                                                                                @Nullable I initialProcedure,
                                                                                                                Map<I, Word<I>> terminatingSequences,
                                                                                                                BiPredicate<M, Word<I>> transitionPredicate) {

        if (initialProcedure == null) {
            return Collections.emptyMap();
        }

        final M initialP = procedures.get(initialProcedure);

        if (initialP == null || initialP.getInitialState() == null) {
            return Collections.emptyMap();
        }

        final Map<I, Word<I>> accessSequences = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
        final Set<I> finishedProcedures = Sets.newHashSetWithExpectedSize(alphabet.getNumCalls());

        // initial value
        accessSequences.put(initialProcedure, Word.fromLetter(initialProcedure));
        finishedProcedures.add(initialProcedure);

        final Deque<I> pendingProcedures = new ArrayDeque<>();
        pendingProcedures.add(initialProcedure);

        while (!pendingProcedures.isEmpty()) {
            final I i = pendingProcedures.pop();
            final M p = procedures.get(i);

            if (p != null) {
                final Collection<I> newProcedures = discoverAccessSequences(alphabet,
                                                                            proceduralInputs,
                                                                            i,
                                                                            p,
                                                                            finishedProcedures,
                                                                            accessSequences,
                                                                            terminatingSequences,
                                                                            transitionPredicate);
                pendingProcedures.addAll(newProcedures);
            }
        }

        return accessSequences;
    }

    private static <I, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>> Collection<I> discoverAccessSequences(
            ProceduralInputAlphabet<I> alphabet,
            Collection<I> proceduralInputs,
            I procedure,
            M p,
            Set<I> finishedProcedures,
            Map<I, Word<I>> accessSequences,
            Map<I, Word<I>> terminatingSequences,
            BiPredicate<M, Word<I>> predicate) {

        final List<I> newAS = new ArrayList<>();
        final Iterator<Word<I>> transitionCoverIterator = Covers.transitionCoverIterator(p, proceduralInputs);

        while (transitionCoverIterator.hasNext()) {
            final Word<I> trace = transitionCoverIterator.next();
            final I sym = trace.lastSymbol();

            if (alphabet.isCallSymbol(sym)) {
                if (!finishedProcedures.contains(sym) && predicate.test(p, trace)) {
                    final WordBuilder<I> accessBuilder = new WordBuilder<>();
                    final Word<I> as = accessSequences.get(procedure);
                    accessBuilder.append(as);
                    accessBuilder.append(alphabet.expand(trace.prefix(-1), terminatingSequences::get));
                    accessBuilder.append(sym);

                    accessSequences.put(sym, accessBuilder.toWord());

                    finishedProcedures.add(sym);
                    newAS.add(sym);
                } else {
                    // If we encounter a failing call we land in a sink state and don't need to analyse further
                    // transitions. Therefore, skip the remaining trace.
                    continue;
                }
            }

            if (finishedProcedures.containsAll(alphabet.getCallAlphabet())) {
                return newAS;
            }
        }
        return newAS;
    }

    static <I, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>> @Nullable Word<I> findSeparatingWord(Map<I, M> sys1,
                                                                                                              ATSequences<I> at1,
                                                                                                              Map<I, M> sys2,
                                                                                                              ATSequences<I> at2,
                                                                                                              ProceduralInputAlphabet<I> alphabet) {
        for (I procedure : alphabet.getCallAlphabet()) {
            final M p1 = sys1.get(procedure);
            final M p2 = sys2.get(procedure);

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

                        // do not expand the last symbol, because it may be an open call
                        final Word<I> expandedSepWord =
                                alphabet.expand(sepWord.prefix(-1), at1.terminatingSequences::get);
                        return Word.fromWords(as, expandedSepWord, Word.fromLetter(sepWord.lastSymbol()));
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
}
