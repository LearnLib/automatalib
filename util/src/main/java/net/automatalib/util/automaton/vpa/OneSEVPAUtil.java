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
package net.automatalib.util.automaton.vpa;

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
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.common.smartcollection.ArrayStorage;
import net.automatalib.common.util.IntDisjointSets;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.UnionFindRemSP;
import net.automatalib.util.automaton.vpa.SPAConverter.ConversionResult;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;

/**
 * Utility class revolving around 1-SEVPAs.
 */
public final class OneSEVPAUtil {

    private OneSEVPAUtil() {}

    public static <L, I> @Nullable Word<I> computeAccessSequence(OneSEVPA<L, I> sevpa,
                                                                 VPAlphabet<I> alphabet,
                                                                 Predicate<? super L> predicate) {
        final ReachResult<L, I> result = computeAccessSequences(sevpa, alphabet, true, predicate);
        L resultLoc = result.terminateLoc;
        if (resultLoc != null) {
            return result.accessSequences.get(sevpa.getLocationId(resultLoc));
        }
        return null;
    }

    public static <L, I> ArrayStorage<Word<I>> computeAccessSequences(OneSEVPA<L, I> sevpa, VPAlphabet<I> alphabet) {
        return computeAccessSequences(sevpa, alphabet, true, l -> false).accessSequences;
    }

    public static <L, I> ReachResult<L, I> computeAccessSequences(OneSEVPA<L, I> sevpa,
                                                                  VPAlphabet<I> alphabet,
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

    public static <I> boolean testEquivalence(OneSEVPA<?, I> sevpa1, OneSEVPA<?, I> sevpa2, VPAlphabet<I> alphabet) {
        return findSeparatingWord(sevpa1, sevpa2, alphabet) == null;
    }

    public static <L, I> @Nullable Word<I> findAcceptedWord(OneSEVPA<L, I> sevpa, VPAlphabet<I> alphabet) {
        return computeAccessSequence(sevpa, alphabet, sevpa::isAcceptingLocation);
    }

    public static <L, I> @Nullable Word<I> findRejectedWord(OneSEVPA<L, I> sevpa, VPAlphabet<I> alphabet) {
        return computeAccessSequence(sevpa, alphabet, l -> !sevpa.isAcceptingLocation(l));
    }

    public static <L, I> List<L> findReachableLocations(OneSEVPA<L, I> sevpa, VPAlphabet<I> alphabet) {
        return computeAccessSequences(sevpa, alphabet, false, l -> false).reachableLocs;
    }

    public static <I> @Nullable Word<I> findSeparatingWord(OneSEVPA<?, I> sevpa1,
                                                           OneSEVPA<?, I> sevpa2,
                                                           VPAlphabet<I> alphabet) {
        final OneSEVPA<?, I> prod = OneSEVPAs.xor(sevpa1, sevpa2, alphabet);
        return findAcceptedWord(prod, alphabet);
    }

    public static <L, I> @Nullable Pair<Word<I>, Word<I>> findSeparatingWord(OneSEVPA<L, I> sevpa,
                                                                             L init1,
                                                                             L init2,
                                                                             VPAlphabet<I> alphabet) {
        if (sevpa.isAcceptingLocation(init1) != sevpa.isAcceptingLocation(init2)) {
            return Pair.of(Word.epsilon(), Word.epsilon());
        }

        final ArrayStorage<Word<I>> as = computeAccessSequences(sevpa, alphabet);
        final IntDisjointSets uf = new UnionFindRemSP(sevpa.size());
        uf.link(sevpa.getLocationId(init1), sevpa.getLocationId(init2));

        final Queue<Record<L, I>> queue = new ArrayDeque<>();
        queue.add(new Record<>(init1, init2));

        Pair<Word<I>, Word<I>> lastPair = null;
        Record<L, I> current;

        explore:
        while ((current = queue.poll()) != null) {
            final L l1 = current.l1;
            final L l2 = current.l2;

            for (I i : alphabet.getInternalAlphabet()) {
                final Pair<Word<I>, Word<I>> pair = Pair.of(Word.epsilon(), Word.fromLetter(i));

                final L succ1 = sevpa.getInternalSuccessor(l1, i);
                final L succ2 = sevpa.getInternalSuccessor(l2, i);

                assert succ1 != null && succ2 != null;

                if (sevpa.isAcceptingLocation(succ1) != sevpa.isAcceptingLocation(succ2)) {
                    lastPair = pair;
                    break explore;
                }

                final int r1 = uf.find(sevpa.getLocationId(succ1)), r2 = uf.find(sevpa.getLocationId(succ2));

                if (r1 == r2) {
                    continue;
                }

                uf.link(r1, r2);

                queue.add(new Record<>(succ1, succ2, pair, current));
            }

            for (I c : alphabet.getCallAlphabet()) {
                final Word<I> cWord = Word.fromLetter(c);

                for (I r : alphabet.getReturnAlphabet()) {
                    final Word<I> rWord = Word.fromLetter(r);

                    // check l as source location for l1/l2
                    for (L l : sevpa.getLocations()) {
                        final int sym = sevpa.encodeStackSym(l, c);
                        final L rSucc1 = sevpa.getReturnSuccessor(l1, r, sym);
                        final L rSucc2 = sevpa.getReturnSuccessor(l2, r, sym);

                        assert rSucc1 != null && rSucc2 != null;

                        final Pair<Word<I>, Word<I>> pair =
                                Pair.of(Word.fromWords(as.get(sevpa.getLocationId(l)), cWord), rWord);

                        if (sevpa.isAcceptingLocation(rSucc1) != sevpa.isAcceptingLocation(rSucc2)) {
                            lastPair = pair;
                            break explore;
                        }

                        final int r1 = uf.find(sevpa.getLocationId(rSucc1)), r2 = uf.find(sevpa.getLocationId(rSucc2));

                        if (r1 == r2) {
                            continue;
                        }

                        uf.link(r1, r2);

                        queue.add(new Record<>(rSucc1, rSucc2, pair, current));
                    }

                    // check l1/l2 as source location for l
                    for (L l : sevpa.getLocations()) {
                        final int sym1 = sevpa.encodeStackSym(l1, c);
                        final int sym2 = sevpa.encodeStackSym(l2, c);
                        final L rSucc1 = sevpa.getReturnSuccessor(l, r, sym1);
                        final L rSucc2 = sevpa.getReturnSuccessor(l, r, sym2);

                        assert rSucc1 != null && rSucc2 != null;

                        final Pair<Word<I>, Word<I>> pair =
                                Pair.of(Word.epsilon(), Word.fromWords(cWord, as.get(sevpa.getLocationId(l)), rWord));

                        if (sevpa.isAcceptingLocation(rSucc1) != sevpa.isAcceptingLocation(rSucc2)) {
                            lastPair = pair;
                            break explore;
                        }

                        final int r1 = uf.find(sevpa.getLocationId(rSucc1)), r2 = uf.find(sevpa.getLocationId(rSucc2));

                        if (r1 == r2) {
                            continue;
                        }

                        uf.link(r1, r2);

                        queue.add(new Record<>(rSucc1, rSucc2, pair, current));
                    }
                }
            }
        }

        if (current == null) {
            return null;
        }

        final Deque<Word<I>> prefixBuilder = new ArrayDeque<>();
        final Deque<Word<I>> suffixBuilder = new ArrayDeque<>();

        prefixBuilder.add(lastPair.getFirst());
        suffixBuilder.add(lastPair.getSecond());

        while (current.reachedFrom != null) {
            final Pair<Word<I>, Word<I>> reachedBy = current.reachedBy;
            prefixBuilder.offerLast(reachedBy.getFirst());
            suffixBuilder.offerFirst(reachedBy.getSecond());
            current = current.reachedFrom;
        }

        return Pair.of(Word.fromWords(prefixBuilder), Word.fromWords(suffixBuilder));
    }

    public static <L, I> Collection<Pair<Word<I>, Word<I>>> findCharacterizingSet(OneSEVPA<L, I> sevpa,
                                                                                  VPAlphabet<I> alphabet) {

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
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
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

    public static <AI, CI> ConversionResult<AI, CI> toSPA(OneSEVPA<?, AI> sevpa,
                                                          VPAlphabet<AI> alphabet,
                                                          CI mainProcedure,
                                                          SymbolMapper<AI, CI> symbolMapper,
                                                          boolean minimize) {
        return SPAConverter.convert(sevpa, alphabet, mainProcedure, symbolMapper, minimize);
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

    private static final class Record<L, I> {

        private final L l1;
        private final L l2;
        private final @PolyNull Pair<Word<I>, Word<I>> reachedBy;
        private final @PolyNull Record<L, I> reachedFrom;

        Record(L l1, L l2) {
            this(l1, l2, null, null);
        }

        Record(L l1, L l2, @PolyNull Pair<Word<I>, Word<I>> reachedBy, @PolyNull Record<L, I> reachedFrom) {
            this.l1 = l1;
            this.l2 = l2;
            this.reachedBy = reachedBy;
            this.reachedFrom = reachedFrom;
        }
    }
}
