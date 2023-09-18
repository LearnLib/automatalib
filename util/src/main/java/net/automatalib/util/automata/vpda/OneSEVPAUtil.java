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
package net.automatalib.util.automata.vpda;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.commons.smartcollections.ArrayStorage;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class revolving around 1-SEVPAs.
 *
 * @author Malte Isberner
 */
public final class OneSEVPAUtil {

    private OneSEVPAUtil() {}

    public static <L, I> @Nullable Word<I> computeAccessSequence(OneSEVPA<L, I> sevpa,
                                                                 VPDAlphabet<I> alphabet,
                                                                 Predicate<? super L> predicate) {
        final ReachResult<L, I> result = computeAccessSequences(sevpa, alphabet, true, predicate);
        L resultLoc = result.terminateLoc;
        if (resultLoc != null) {
            return result.accessSequences.get(sevpa.getLocationId(resultLoc));
        }
        return null;
    }

    public static <L, I> ArrayStorage<Word<I>> computeAccessSequences(OneSEVPA<L, I> sevpa, VPDAlphabet<I> alphabet) {
        return computeAccessSequences(sevpa, alphabet, true, l -> false).accessSequences;
    }

    public static <L, I> ReachResult<L, I> computeAccessSequences(OneSEVPA<L, I> sevpa,
                                                                  VPDAlphabet<I> alphabet,
                                                                  boolean computeAs,
                                                                  Predicate<? super L> terminatePred) {
        final ArrayStorage<Word<I>> result = new ArrayStorage<>(sevpa.size());

        final L initLoc = sevpa.getInitialLocation();
        final List<L> reachable = new ArrayList<>();
        reachable.add(initLoc);
        result.set(sevpa.getLocationId(initLoc), Word.epsilon());

        if (terminatePred.test(initLoc)) {
            return new ReachResult<>(initLoc, reachable, result);
        }

        int queuePtr = 0;
        while (queuePtr < reachable.size()) {
            final L curr = reachable.get(queuePtr++);
            final Word<I> currAs = result.get(sevpa.getLocationId(curr));

            for (I intSym : alphabet.getInternalAlphabet()) {
                final L succ = sevpa.getInternalSuccessor(curr, intSym);
                if (succ == null) {
                    continue;
                }
                final int succIdx = sevpa.getLocationId(succ);
                if (result.get(succIdx) != null) {
                    continue;
                }
                final Word<I> succAs = computeAs ? currAs.append(intSym) : Word.epsilon();
                result.set(succIdx, succAs);
                if (terminatePred.test(succ)) {
                    return new ReachResult<>(succ, reachable, result);
                }
                reachable.add(succ);
            }

            for (I callSym : alphabet.getCallAlphabet()) {
                for (I returnSym : alphabet.getReturnAlphabet()) {
                    for (int i = 0; i < queuePtr; i++) {
                        final L src = reachable.get(i);
                        int stackSym = sevpa.encodeStackSym(src, callSym);
                        L succ = sevpa.getReturnSuccessor(curr, returnSym, stackSym);
                        if (succ == null) {
                            continue;
                        }
                        int succIdx = sevpa.getLocationId(succ);
                        if (result.get(succIdx) == null) {
                            Word<I> succAs = computeAs ?
                                    result.get(sevpa.getLocationId(src))
                                          .append(callSym)
                                          .concat(currAs.append(returnSym)) :
                                    Word.epsilon();
                            result.set(succIdx, succAs);
                            if (terminatePred.test(succ)) {
                                return new ReachResult<>(succ, reachable, result);
                            }
                            reachable.add(succ);
                        }

                        if (src != curr) {
                            stackSym = sevpa.encodeStackSym(curr, callSym);
                            succ = sevpa.getReturnSuccessor(src, returnSym, stackSym);
                            if (succ == null) {
                                continue;
                            }
                            succIdx = sevpa.getLocationId(succ);
                            if (result.get(succIdx) == null) {
                                final Word<I> succAs = computeAs ?
                                        currAs.append(callSym)
                                              .concat(result.get(sevpa.getLocationId(src)).append(returnSym)) :
                                        Word.epsilon();
                                result.set(succIdx, succAs);
                                if (terminatePred.test(succ)) {
                                    return new ReachResult<>(succ, reachable, result);
                                }
                                reachable.add(succ);
                            }
                        }
                    }
                }
            }
        }

        return new ReachResult<>(null, reachable, result);
    }

    public static <I> boolean testEquivalence(OneSEVPA<?, I> sevpa1, OneSEVPA<?, I> sevpa2, VPDAlphabet<I> alphabet) {
        return findSeparatingWord(sevpa1, sevpa2, alphabet) == null;
    }

    public static <L, I> @Nullable Word<I> findAcceptedWord(OneSEVPA<L, I> sevpa, VPDAlphabet<I> alphabet) {
        return computeAccessSequence(sevpa, alphabet, sevpa::isAcceptingLocation);
    }

    public static <L, I> @Nullable Word<I> findRejectedWord(OneSEVPA<L, I> sevpa, VPDAlphabet<I> alphabet) {
        return computeAccessSequence(sevpa, alphabet, l -> !sevpa.isAcceptingLocation(l));
    }

    public static <L, I> List<L> findReachableLocations(OneSEVPA<L, I> sevpa, VPDAlphabet<I> alphabet) {
        return computeAccessSequences(sevpa, alphabet, false, l -> false).reachableLocs;
    }

    public static <I> @Nullable Word<I> findSeparatingWord(OneSEVPA<?, I> sevpa1,
                                                           OneSEVPA<?, I> sevpa2,
                                                           VPDAlphabet<I> alphabet) {
        final OneSEVPA<?, I> prod = OneSEVPAs.xor(sevpa1, sevpa2, alphabet);
        return findAcceptedWord(prod, alphabet);
    }

    public static <L, I> @Nullable Pair<Word<I>, Word<I>> findSeparatingWord(OneSEVPA<L, I> sevpa,
                                                                             L l1,
                                                                             L l2,
                                                                             VPDAlphabet<I> alphabet) {

        final ArrayStorage<Word<I>> as = computeAccessSequences(sevpa, alphabet);
        final Word<I> as1 = as.get(sevpa.getLocationId(l1));
        final Word<I> as2 = as.get(sevpa.getLocationId(l2));

        if (sevpa.accepts(as1) != sevpa.accepts(as2)) {
            return Pair.of(Word.epsilon(), Word.epsilon());
        }

        final Deque<Pair<Word<I>, Word<I>>> queue = new ArrayDeque<>();
        queue.add(Pair.of(Word.epsilon(), Word.epsilon()));

        int nestingDepth = 0;
        while (!queue.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            final @NonNull Pair<Word<I>, Word<I>> curr = queue.poll();
            final Word<I> pref = curr.getFirst();
            final Word<I> suff = curr.getSecond();

            for (I i : alphabet.getInternalAlphabet()) {
                final Pair<Word<I>, Word<I>> candidate = Pair.of(pref, suff.append(i));

                if (sevpa.accepts(Word.fromWords(candidate.getFirst(), as1, candidate.getSecond())) !=
                    sevpa.accepts(Word.fromWords(candidate.getFirst(), as2, candidate.getSecond()))) {
                    return candidate;
                } else if (nestingDepth < sevpa.size()) {
                    queue.add(candidate);
                }
            }

            for (I callSymbol : alphabet.getCallAlphabet()) {
                final Word<I> c = Word.fromLetter(callSymbol);

                for (I returnSymbol : alphabet.getReturnAlphabet()) {
                    final Word<I> r = Word.fromLetter(returnSymbol);

                    for (L l : sevpa.getLocations()) {
                        final Word<I> v = as.get(sevpa.getLocationId(l));

                        final Pair<Word<I>, Word<I>> candidate1 = Pair.of(pref, Word.fromWords(suff, c, v, r));
                        if (sevpa.accepts(Word.fromWords(candidate1.getFirst(), as1, candidate1.getSecond())) !=
                            sevpa.accepts(Word.fromWords(candidate1.getFirst(), as2, candidate1.getSecond()))) {
                            return candidate1;
                        } else if (nestingDepth < sevpa.size()) {
                            queue.add(candidate1);
                        }

                        final Pair<Word<I>, Word<I>> candidate2 = Pair.of(Word.fromWords(v, c, pref), suff.concat(r));
                        if (sevpa.accepts(Word.fromWords(candidate2.getFirst(), as1, candidate2.getSecond())) !=
                            sevpa.accepts(Word.fromWords(candidate2.getFirst(), as2, candidate2.getSecond()))) {
                            return candidate2;
                        } else if (nestingDepth < sevpa.size()) {
                            queue.add(candidate2);
                        }
                    }
                }
            }
            nestingDepth++;
        }

        return null;
    }

    public static <L, I> Collection<Pair<Word<I>, Word<I>>> findCharacterizingSet(OneSEVPA<L, I> sevpa,
                                                                                  VPDAlphabet<I> alphabet) {

        final ArrayStorage<Word<I>> as = computeAccessSequences(sevpa, alphabet);
        final List<L> acceptingLocations = new ArrayList<>(sevpa.size());
        final List<L> rejectionLocations = new ArrayList<>(sevpa.size());

        for (L l : sevpa.getLocations()) {
            if (sevpa.isAcceptingLocation(l)) {
                acceptingLocations.add(l);
            } else {
                rejectionLocations.add(l);
            }
        }

        final Set<Pair<Word<I>, Word<I>>> result = Sets.newHashSetWithExpectedSize(sevpa.size());
        result.add(Pair.of(Word.epsilon(), Word.epsilon()));

        final Queue<List<L>> blockQueue = new ArrayDeque<>();
        blockQueue.add(acceptingLocations);
        blockQueue.add(rejectionLocations);

        while (!blockQueue.isEmpty()) {
            final @NonNull List<L> block = blockQueue.poll();
            if (block.size() == 1) {
                continue;
            }

            final Iterator<L> blockIter = block.iterator();

            final L l1 = blockIter.next();
            final L l2 = blockIter.next();

            final Pair<Word<I>, Word<I>> sepWord = findSeparatingWord(sevpa, l1, l2, alphabet);
            assert sepWord != null;

            result.add(sepWord);

            final List<L> acceptingBucket = new ArrayList<>(block.size());
            final List<L> rejectingBucket = new ArrayList<>(block.size());

            if (sevpa.accepts(Word.fromWords(sepWord.getFirst(),
                                             as.get(sevpa.getLocationId(l1)),
                                             sepWord.getSecond()))) {
                acceptingBucket.add(l1);
                rejectingBucket.add(l2);
            } else {
                acceptingBucket.add(l2);
                rejectingBucket.add(l1);
            }

            while (blockIter.hasNext()) {
                final L next = blockIter.next();
                if (sevpa.accepts(Word.fromWords(sepWord.getFirst(),
                                                 as.get(sevpa.getLocationId(next)),
                                                 sepWord.getSecond()))) {
                    acceptingBucket.add(next);
                } else {
                    rejectingBucket.add(next);
                }
            }

            blockQueue.add(acceptingBucket);
            blockQueue.add(rejectingBucket);
        }

        return result;
    }

    public static class ReachResult<L, I> {

        public final @Nullable L terminateLoc;
        public final List<L> reachableLocs;
        public final ArrayStorage<Word<I>> accessSequences;

        public ReachResult(@Nullable L terminateLoc, List<L> reachableLocs, ArrayStorage<Word<I>> accessSequences) {
            this.terminateLoc = terminateLoc;
            this.reachableLocs = reachableLocs;
            this.accessSequences = accessSequences;
        }
    }

}
