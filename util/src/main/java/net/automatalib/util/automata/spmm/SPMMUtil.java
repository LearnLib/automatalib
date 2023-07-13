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
package net.automatalib.util.automata.spmm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.automatalib.automata.spmm.SPMM;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.util.automata.sba.ATSequences;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.SPAOutputAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
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

    public static <I, O> ATSequences<I> computeATSequences(SPMM<?, I, ?, O> spmm, SPAAlphabet<I> inputAlphabet) {

        assert isValid(spmm, inputAlphabet);

        final Map<I, Word<I>> terminatingSequences = computeTerminatingSequences(spmm, inputAlphabet);
        final Map<I, Word<I>> accessSequences = computeAccessSequences(spmm, inputAlphabet, terminatingSequences);

        return new ATSequences<>(accessSequences, terminatingSequences);
    }

    public static <I, O> Map<I, Word<I>> computeTerminatingSequences(SPMM<?, I, ?, O> spmm,
                                                                     SPAAlphabet<I> inputAlphabet) {

        final SPAOutputAlphabet<O> outputAlphabet = spmm.getOutputAlphabet();
        final Map<I, MealyMachine<?, I, ?, O>> procedures = spmm.getProcedures();
        final Map<I, Word<I>> terminatingSequences = Maps.newHashMapWithExpectedSize(inputAlphabet.getNumCalls());

        // initial internal sequences
        for (I procedure : inputAlphabet.getCallAlphabet()) {
            final MealyMachine<?, I, ?, O> mealy = procedures.get(procedure);

            if (mealy != null) {
                final Iterator<Word<I>> iter = Covers.stateCoverIterator(mealy, inputAlphabet.getInternalAlphabet());
                while (iter.hasNext()) {
                    final Word<I> trace = iter.next();
                    final Word<O> output =
                            mealy.computeSuffixOutput(trace, Word.fromLetter(inputAlphabet.getReturnSymbol()));
                    assert output.size() == 1;

                    if (!outputAlphabet.isErrorSymbol(output.firstSymbol())) {
                        terminatingSequences.put(procedure, trace);
                        break;
                    }
                }
            }
        }

        final Set<I> remainingProcedures = new HashSet<>(inputAlphabet.getCallAlphabet());
        remainingProcedures.removeAll(terminatingSequences.keySet());

        final Set<I> eligibleInputs = new HashSet<>(inputAlphabet.getInternalAlphabet());
        eligibleInputs.addAll(terminatingSequences.keySet());

        boolean stable = false;

        while (!stable) {
            stable = true;

            for (final I i : new ArrayList<>(remainingProcedures)) {

                final MealyMachine<?, I, ?, O> mealy = procedures.get(i);

                if (mealy == null) {
                    // for non-existing procedures we cannot compute any terminating sequences
                    remainingProcedures.remove(i);
                } else {
                    final Iterator<Word<I>> iter = Covers.stateCoverIterator(mealy, eligibleInputs);

                    while (iter.hasNext()) {
                        final Word<I> trace = iter.next();
                        final Word<O> output =
                                mealy.computeSuffixOutput(trace, Word.fromLetter(inputAlphabet.getReturnSymbol()));
                        assert output.size() == 1;

                        if (!outputAlphabet.isErrorSymbol(output.firstSymbol())) {
                            terminatingSequences.put(i, inputAlphabet.expand(trace, terminatingSequences::get));
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

    public static <I, O> Map<I, Word<I>> computeAccessSequences(SPMM<?, I, ?, O> spmm,
                                                                SPAAlphabet<I> inputAlphabet,
                                                                Map<I, Word<I>> terminatingSequences) {
        final I initialProcedure = spmm.getInitialProcedure();

        if (initialProcedure == null) {
            return Collections.emptyMap();
        }

        final Map<I, MealyMachine<?, I, ?, O>> procedures = spmm.getProcedures();
        final Map<I, Word<I>> accessSequences = Maps.newHashMapWithExpectedSize(inputAlphabet.getNumCalls());
        final Set<I> finishedProcedures = Sets.newHashSetWithExpectedSize(inputAlphabet.getNumCalls());

        // initial value
        accessSequences.put(initialProcedure, Word.fromLetter(initialProcedure));
        finishedProcedures.add(initialProcedure);

        final Deque<I> pendingProcedures = new ArrayDeque<>();
        pendingProcedures.add(initialProcedure);

        while (!pendingProcedures.isEmpty()) {
            final I i = pendingProcedures.pop();
            final MealyMachine<?, I, ?, O> mealy = procedures.get(i);

            if (mealy != null) {
                final Collection<I> newProcedures = discoverAccessSequences(inputAlphabet,
                                                                            spmm.getOutputAlphabet(),
                                                                            i,
                                                                            mealy,
                                                                            finishedProcedures,
                                                                            accessSequences,
                                                                            terminatingSequences);
                pendingProcedures.addAll(newProcedures);
            }
        }

        return accessSequences;
    }

    private static <S, I, O> Collection<I> discoverAccessSequences(SPAAlphabet<I> inputAlphabet,
                                                                   SPAOutputAlphabet<O> outputAlphabet,
                                                                   I procedure,
                                                                   MealyMachine<S, I, ?, O> mealy,
                                                                   Set<I> finishedProcedures,
                                                                   Map<I, Word<I>> accessSequences,
                                                                   Map<I, Word<I>> terminatingSequences) {

        final List<I> newAS = new ArrayList<>();
        final Iterator<Word<I>> transitionCoverIterator = Covers.transitionCoverIterator(mealy, inputAlphabet);

        tc:
        while (transitionCoverIterator.hasNext()) {
            final Word<I> trace = transitionCoverIterator.next();

            S iter = mealy.getInitialState();

            for (int i = 0; i < trace.length(); i++) {
                final I input = trace.getSymbol(i);

                if (inputAlphabet.isCallSymbol(input)) {
                    if (!finishedProcedures.contains(input)) {

                        final O output = mealy.getOutput(iter, input);

                        if (outputAlphabet.isErrorSymbol(output)) {
                            // If we encounter a failing call we land in a sink state and don't need to analyse further
                            // transitions. Therefore skip the remaining trace.
                            continue tc;
                        } else {

                            final WordBuilder<I> accessBuilder = new WordBuilder<>();
                            final Word<I> as = accessSequences.get(procedure);
                            accessBuilder.append(as);
                            accessBuilder.append(inputAlphabet.expand(trace.subWord(0, i), terminatingSequences::get));
                            accessBuilder.append(input);

                            accessSequences.put(input, accessBuilder.toWord());

                            finishedProcedures.add(input);
                            newAS.add(input);
                        }
                    } else if (!terminatingSequences.containsKey(input)) {
                        // If we encounter a call symbol for which we do not have a terminating sequence,
                        // all local access sequences of future call symbols cannot be expanded properly.
                        // Therefore skip the remaining trace.
                        continue tc;
                    }
                }

                iter = mealy.getSuccessor(iter, input);
            }

            if (finishedProcedures.containsAll(inputAlphabet.getCallAlphabet())) {
                return newAS;
            }
        }
        return newAS;
    }

    public static <I, O> boolean isValid(SPMM<?, I, ?, O> spmm) {
        return isValid(spmm, spmm.getInputAlphabet());
    }

    public static <I, O> boolean isValid(SPMM<?, I, ?, O> spmm, SPAAlphabet<I> inputAlphabet) {

        final Map<I, Word<I>> ts = computeTerminatingSequences(spmm, inputAlphabet);
        final Set<I> nonContinuableSymbols = new HashSet<>(inputAlphabet.getCallAlphabet());
        nonContinuableSymbols.removeAll(ts.keySet());
        nonContinuableSymbols.add(inputAlphabet.getReturnSymbol());

        final SPAOutputAlphabet<O> outputAlphabet = spmm.getOutputAlphabet();

        for (MealyMachine<?, I, ?, O> p : spmm.getProcedures().values()) {
            if (!isErrorReturnAndCallClosed(p, inputAlphabet, nonContinuableSymbols, outputAlphabet.getErrorSymbol())) {
                return false;
            }
        }

        return true;
    }

    private static <S, I, O> boolean isErrorReturnAndCallClosed(MealyMachine<S, I, ?, O> procedure,
                                                                SPAAlphabet<I> inputAlphabet,
                                                                Set<I> nonContinuableSymbols,
                                                                O errorOutput) {

        for (I i : inputAlphabet) {

            boolean isNonContinuable = nonContinuableSymbols.contains(i);

            for (S s : procedure) {

                final O output = procedure.getOutput(s, i);
                final S succ = procedure.getSuccessor(s, i);

                if ((isNonContinuable || Objects.equals(output, errorOutput)) &&
                    !isSink(procedure, inputAlphabet, succ, errorOutput)) {
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
            if (t == null || !Objects.equals(m.getSuccessor(t), state) ||
                !Objects.equals(m.getTransitionOutput(t), output)) {
                return false;
            }
        }
        return true;
    }

    public static <I, O> boolean testEquivalence(SPMM<?, I, ?, O> spmm1,
                                                 SPMM<?, I, ?, O> spmm2,
                                                 SPAAlphabet<I> alphabet) {
        return findSeparatingWord(spmm1, spmm2, alphabet) == null;
    }

    public static <I, O> @Nullable Word<I> findSeparatingWord(SPMM<?, I, ?, O> spmm1,
                                                              SPMM<?, I, ?, O> spmm2,
                                                              SPAAlphabet<I> alphabet) {

        final ATSequences<I> at1 = computeATSequences(spmm1, alphabet);
        final ATSequences<I> at2 = computeATSequences(spmm2, alphabet);

        for (final I procedure : alphabet.getCallAlphabet()) {
            final MealyMachine<?, I, ?, O> p1 = spmm1.getProcedures().get(procedure);
            final MealyMachine<?, I, ?, O> p2 = spmm2.getProcedures().get(procedure);

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
                } // else no procedures can be embedded
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
