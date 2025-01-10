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
package net.automatalib.util.automaton.equivalence;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("PMD.TestClassWithoutTestCases") // not a traditional test class
public final class DeterministicEquivalenceTest {

    private static final int MAP_THRESHOLD = 10_000;

    private DeterministicEquivalenceTest() {
        // prevent instantiation
    }

    @SuppressWarnings("PMD.UnnecessaryCast") // we want to cast to long, to prevent overflows
    public static <I, S, T, SP, TP, S2, T2, SP2, TP2> @Nullable Word<I> findSeparatingWord(
            UniversalDeterministicAutomaton<S, I, T, SP, TP> reference,
            UniversalDeterministicAutomaton<S2, I, T2, SP2, TP2> other,
            Collection<? extends I> inputs) {
        int refSize = reference.size();
        int otherSize = other.size();

        S refInit = reference.getInitialState();
        S2 otherInit = other.getInitialState();

        if (refInit == null || otherInit == null) {
            return refInit == null && otherInit == null ? null : Word.epsilon();
        }

        SP refStateProp = reference.getStateProperty(refInit);
        SP2 otherStateProp = other.getStateProperty(otherInit);

        if (!Objects.equals(refStateProp, otherStateProp)) {
            return Word.epsilon();
        }

        StateIDs<S> refStateIds = reference.stateIDs();
        StateIDs<S2> otherStateIds = other.stateIDs();

        int currDepth = 0;
        int inCurrDepth = 1;
        int inNextDepth = 0;

        I lastSym = null;
        Pred<I> lastPred = new Pred<>();
        Registry<I> reg;

        if ((long) refSize * (long) otherSize > MAP_THRESHOLD) {
            reg = new MapRegistry<>(refSize);
        } else {
            reg = new ArrayRegistry<>(refSize, otherSize);
        }

        reg.putPred(refStateIds.getStateId(refInit), otherStateIds.getStateId(otherInit), lastPred);

        Queue<StatePair<S, S2>> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(new StatePair<>(refInit, otherInit));
        StatePair<S, S2> currPair;

        bfs:
        while ((currPair = bfsQueue.poll()) != null) {
            S refState = currPair.ref;
            S2 otherState = currPair.other;

            lastPred = reg.getPred(refStateIds.getStateId(refState), otherStateIds.getStateId(otherState));
            assert lastPred != null;

            for (I in : inputs) {
                lastSym = in;
                T refTrans = reference.getTransition(refState, in);
                T2 otherTrans = other.getTransition(otherState, in);

                if (refTrans == null || otherTrans == null) {
                    if (refTrans == null && otherTrans == null) {
                        continue;
                    } else {
                        break bfs;
                    }
                }

                TP refProp = reference.getTransitionProperty(refTrans);
                TP2 otherProp = other.getTransitionProperty(otherTrans);
                if (!Objects.equals(refProp, otherProp)) {
                    break bfs;
                }

                S refSucc = reference.getSuccessor(refTrans);
                S2 otherSucc = other.getSuccessor(otherTrans);

                int refId = refStateIds.getStateId(refSucc);
                int otherId = otherStateIds.getStateId(otherSucc);

                if (reg.getPred(refId, otherId) == null) {
                    refStateProp = reference.getStateProperty(refSucc);
                    otherStateProp = other.getStateProperty(otherSucc);

                    if (!Objects.equals(refStateProp, otherStateProp)) {
                        break bfs;
                    }

                    reg.putPred(refId, otherId, new Pred<>(lastPred, in));
                    bfsQueue.add(new StatePair<>(refSucc, otherSucc));
                    inNextDepth++;
                }
            }

            lastSym = null;

            // Next level in BFS reached
            if (--inCurrDepth == 0) {
                inCurrDepth = inNextDepth;
                inNextDepth = 0;
                currDepth++;
            }
        }

        if (lastSym == null) {
            return null;
        }

        @SuppressWarnings("nullness") // we make sure to set each index to a value of type I
        WordBuilder<I> sep = new WordBuilder<>(null, currDepth + 1);
        int index = currDepth;
        sep.setSymbol(index--, lastSym);

        Pred<I> pred = lastPred;
        while (pred.prev != null) {
            sep.setSymbol(index--, pred.symbol);
            pred = pred.prev;
        }

        return sep.toWord();
    }

    private static final class StatePair<S, S2> {

        public final S ref;
        public final S2 other;

        StatePair(S ref, S2 other) {
            this.ref = ref;
            this.other = other;
        }
    }

    private static final class Pred<I> {

        public final @Nullable Pred<I> prev;
        public final @Nullable I symbol;

        Pred() {
            this.prev = null;
            this.symbol = null;
        }

        Pred(Pred<I> prev, I input) {
            this.prev = prev;
            this.symbol = input;
        }
    }

    private interface Registry<I> {

        @Nullable Pred<I> getPred(int id1, int id2);

        void putPred(int id1, int id2, Pred<I> pred);
    }

    private static class ArrayRegistry<I> implements Registry<I> {

        final Pred<I>[] preds;
        final int size1;

        @SuppressWarnings("unchecked")
        ArrayRegistry(int size1, int size2) {
            this.preds = new Pred[size1 * size2];
            this.size1 = size1;
        }

        @Override
        public @Nullable Pred<I> getPred(int id1, int id2) {
            return preds[computeIndex(id1, id2, size1)];
        }

        @Override
        public void putPred(int id1, int id2, Pred<I> pred) {
            preds[computeIndex(id1, id2, size1)] = pred;
        }

        private int computeIndex(int id1, int id2, int size1) {
            return id1 * size1 + id2;
        }
    }

    private static class MapRegistry<I> implements Registry<I> {

        final Map<Long, Pred<I>> preds;
        final int size1;

        MapRegistry(int size1) {
            this.preds = new HashMap<>();
            this.size1 = size1;
        }

        @Override
        public @Nullable Pred<I> getPred(int id1, int id2) {
            return preds.get(computeIndex(id1, id2, size1));
        }

        @Override
        public void putPred(int id1, int id2, Pred<I> pred) {
            preds.put(computeIndex(id1, id2, size1), pred);
        }

        private long computeIndex(long id1, long id2, long size1) {
            return id1 * size1 + id2;
        }
    }
}
