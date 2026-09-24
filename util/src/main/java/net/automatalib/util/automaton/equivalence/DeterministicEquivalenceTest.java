/* Copyright (C) 2013-2026 TU Dortmund University
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

import net.automatalib.automaton.UniversalDeterministicAutomaton.RegularAutomaton;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Implements an automaton equivalence test via a breadth-first traversal of the product automaton.
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // not a traditional test class
public final class DeterministicEquivalenceTest {

    private static final int MAP_THRESHOLD = 10_000;

    private DeterministicEquivalenceTest() {
        // prevent instantiation
    }

    /**
     * Find a separating word for two states in a given automaton. Note that undefined transitions are considered a
     * separating feature as well.
     *
     * @param reference
     *         the automaton
     * @param init1
     *         the first state
     * @param init2
     *         the second state
     * @param inputs
     *         the inputs to consider for a separating word
     * @param <S>
     *         automaton state type
     * @param <I>
     *         input symbol type
     *
     * @return a word separating the two states, {@code null} if no such word can be found
     */
    public static <S, I> @Nullable Word<I> findSeparatingWord(RegularAutomaton<S, I, ?, ?, ?> reference,
                                                              S init1,
                                                              S init2,
                                                              Collection<? extends I> inputs) {
        return findSeparatingWord(reference, init1, init2, inputs, false);
    }

    /**
     * Find a separating word for two states in a given automaton. Note that unless undefined transitions are ignored,
     * they are considered a separating feature as well.
     *
     * @param reference
     *         the automaton
     * @param init1
     *         the first state
     * @param init2
     *         the second state
     * @param inputs
     *         the inputs to consider for a separating word
     * @param ignoreUndefinedTransitions
     *         whether to ignore undefined transitions in the automaton
     * @param <S>
     *         automaton state type
     * @param <I>
     *         input symbol type
     *
     * @return a word separating the two states, {@code null} if no such word can be found
     */
    public static <S, I> @Nullable Word<I> findSeparatingWord(RegularAutomaton<S, I, ?, ?, ?> reference,
                                                              S init1,
                                                              S init2,
                                                              Collection<? extends I> inputs,
                                                              boolean ignoreUndefinedTransitions) {
        return doFindSeparatingWord(reference, init1, init2, inputs, ignoreUndefinedTransitions);
    }

    /**
     * Find a separating word for two given automata. Note that undefined transitions are considered a separating
     * feature as well.
     *
     * @param reference
     *         the first automaton
     * @param other
     *         the second automaton
     * @param inputs
     *         the inputs to consider for a separating word
     * @param <I>
     *         input symbol type
     *
     * @return a word separating the two automata, {@code null} if no such word can be found
     */
    public static <I> @Nullable Word<I> findSeparatingWord(RegularAutomaton<?, I, ?, ?, ?> reference,
                                                           RegularAutomaton<?, I, ?, ?, ?> other,
                                                           Collection<? extends I> inputs) {
        return findSeparatingWord(reference, other, inputs, false);
    }

    /**
     * Find a separating word for two given automata. Note that unless undefined transitions are ignored, they are
     * considered a separating feature as well.
     *
     * @param reference
     *         the first automaton
     * @param other
     *         the second automaton
     * @param inputs
     *         the inputs to consider for a separating word
     * @param ignoreUndefinedTransitions
     *         whether to ignore undefined transitions in both automata
     * @param <I>
     *         input symbol type
     *
     * @return a word separating the two automata, {@code null} if no such word can be found
     */
    public static <I> @Nullable Word<I> findSeparatingWord(RegularAutomaton<?, I, ?, ?, ?> reference,
                                                           RegularAutomaton<?, I, ?, ?, ?> other,
                                                           Collection<? extends I> inputs,
                                                           boolean ignoreUndefinedTransitions) {
        return doFindSeparatingWord(reference, other, inputs, ignoreUndefinedTransitions);
    }

    private static <S, I, T, SP, TP> @Nullable Word<I> doFindSeparatingWord(RegularAutomaton<S, I, T, SP, TP> reference,
                                                                            S init1,
                                                                            S init2,
                                                                            Collection<? extends I> inputs,
                                                                            boolean ignoreUndefinedTransitions) {

        if (Objects.equals(init1, init2)) {
            return null;
        }

        SP refStateProp = reference.getStateProperty(init1);
        SP otherStateProp = reference.getStateProperty(init2);

        if (!Objects.equals(refStateProp, otherStateProp)) {
            return Word.epsilon();
        }

        StateIDs<S> stateIds = reference.stateIDs();

        Pred<I> lastPred = new Pred<>();
        int size = reference.size();
        Registry<I> reg = buildRegistry(size, size);

        reg.putPred(stateIds.getStateId(init1), stateIds.getStateId(init2), lastPred);

        Queue<StatePair<S, S>> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(new StatePair<>(init1, init2));
        StatePair<S, S> currPair;

        int currDepth = 0;
        int inCurrDepth = 1;
        int inNextDepth = 0;
        I lastSym = null;

        bfs:
        while ((currPair = bfsQueue.poll()) != null) {
            S refState = currPair.ref;
            S otherState = currPair.other;

            @SuppressWarnings("nullness") // in a breadth-first traversal the predecessors are always defined
            @NonNull
            Pred<I> pred = reg.getPred(stateIds.getStateId(refState), stateIds.getStateId(otherState));
            lastPred = pred;

            for (I in : inputs) {
                lastSym = in;
                T refTrans = reference.getTransition(refState, in);
                T otherTrans = reference.getTransition(otherState, in);

                if (ignoreUndefinedTransitions && (refTrans == null || otherTrans == null)) {
                    continue;
                } else if (refTrans == null) {
                    if (otherTrans == null) {
                        continue;
                    }
                    break bfs;
                } else if (otherTrans == null) {
                    break bfs;
                }

                TP refProp = reference.getTransitionProperty(refTrans);
                TP otherProp = reference.getTransitionProperty(otherTrans);
                if (!Objects.equals(refProp, otherProp)) {
                    break bfs;
                }

                S refSucc = reference.getSuccessor(refTrans);
                S otherSucc = reference.getSuccessor(otherTrans);

                int refId = stateIds.getStateId(refSucc);
                int otherId = stateIds.getStateId(otherSucc);

                if (refId != otherId && reg.getPred(refId, otherId) == null && reg.getPred(otherId, refId) == null) {
                    refStateProp = reference.getStateProperty(refSucc);
                    otherStateProp = reference.getStateProperty(otherSucc);

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

        return extractSepWord(lastPred, lastSym, currDepth);
    }

    private static <S1, S2, I, T1, T2, SP1, SP2, TP1, TP2> @Nullable Word<I> doFindSeparatingWord(RegularAutomaton<S1, I, T1, SP1, TP1> reference,
                                                                                                  RegularAutomaton<S2, I, T2, SP2, TP2> other,
                                                                                                  Collection<? extends I> inputs,
                                                                                                  boolean ignoreUndefinedTransitions) {

        S1 refInit = reference.getInitialState();
        S2 otherInit = other.getInitialState();

        if (refInit == null && otherInit == null) {
            return null;
        } else if (refInit == null || otherInit == null) {
            return ignoreUndefinedTransitions ? null : Word.epsilon();
        }

        SP1 refStateProp = reference.getStateProperty(refInit);
        SP2 otherStateProp = other.getStateProperty(otherInit);

        if (!Objects.equals(refStateProp, otherStateProp)) {
            return Word.epsilon();
        }

        StateIDs<S1> refStateIds = reference.stateIDs();
        StateIDs<S2> otherStateIds = other.stateIDs();

        Pred<I> lastPred = new Pred<>();
        Registry<I> reg = buildRegistry(reference.size(), other.size());

        reg.putPred(refStateIds.getStateId(refInit), otherStateIds.getStateId(otherInit), lastPred);

        Queue<StatePair<S1, S2>> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(new StatePair<>(refInit, otherInit));
        StatePair<S1, S2> currPair;

        int currDepth = 0;
        int inCurrDepth = 1;
        int inNextDepth = 0;
        I lastSym = null;

        bfs:
        while ((currPair = bfsQueue.poll()) != null) {
            S1 refState = currPair.ref;
            S2 otherState = currPair.other;

            @SuppressWarnings("nullness") // in a breadth-first traversal the predecessors are always defined
            @NonNull
            Pred<I> pred = reg.getPred(refStateIds.getStateId(refState), otherStateIds.getStateId(otherState));
            lastPred = pred;

            for (I in : inputs) {
                lastSym = in;
                T1 refTrans = reference.getTransition(refState, in);
                T2 otherTrans = other.getTransition(otherState, in);

                if (ignoreUndefinedTransitions && (refTrans == null || otherTrans == null)) {
                    continue;
                } else if (refTrans == null) {
                    if (otherTrans == null) {
                        continue;
                    }
                    break bfs;
                } else if (otherTrans == null) {
                    break bfs;
                }

                TP1 refProp = reference.getTransitionProperty(refTrans);
                TP2 otherProp = other.getTransitionProperty(otherTrans);
                if (!Objects.equals(refProp, otherProp)) {
                    break bfs;
                }

                S1 refSucc = reference.getSuccessor(refTrans);
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

        return extractSepWord(lastPred, lastSym, currDepth);
    }

    private static <I> Registry<I> buildRegistry(int refSize, int otherSize) {
        if (Math.multiplyFull(refSize, otherSize) > MAP_THRESHOLD) {
            return new MapRegistry<>(refSize);
        } else {
            return new ArrayRegistry<>(refSize, otherSize);
        }
    }

    private static <I> @Nullable Word<I> extractSepWord(Pred<I> lastPred, @Nullable I lastSym, int currDepth) {
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

    private static final class StatePair<S1, S2> {

        final S1 ref;
        final S2 other;

        StatePair(S1 ref, S2 other) {
            this.ref = ref;
            this.other = other;
        }
    }

    static final class Pred<I> {

        final @Nullable Pred<I> prev;
        final @Nullable I symbol;

        Pred() {
            this.prev = null;
            this.symbol = null;
        }

        Pred(Pred<I> prev, I input) {
            this.prev = prev;
            this.symbol = input;
        }
    }

    interface Registry<I> {

        @Nullable Pred<I> getPred(int id1, int id2);

        void putPred(int id1, int id2, Pred<I> pred);
    }

    static class ArrayRegistry<I> implements Registry<I> {

        final Pred<I>[] preds;
        final int size1;

        @SuppressWarnings("unchecked")
        ArrayRegistry(int size1, int size2) {
            this.preds = new Pred[size1 * size2];
            this.size1 = size1;
        }

        @Override
        public @Nullable Pred<I> getPred(int id1, int id2) {
            return preds[computeIndex(id1, id2)];
        }

        @Override
        public void putPred(int id1, int id2, Pred<I> pred) {
            preds[computeIndex(id1, id2)] = pred;
        }

        private int computeIndex(int id1, int id2) {
            return id2 * size1 + id1;
        }
    }

    static class MapRegistry<I> implements Registry<I> {

        final Map<Long, Pred<I>> preds;
        final int size1;

        MapRegistry(int size1) {
            this.preds = new HashMap<>();
            this.size1 = size1;
        }

        @Override
        public @Nullable Pred<I> getPred(int id1, int id2) {
            return preds.get(computeIndex(id1, id2));
        }

        @Override
        public void putPred(int id1, int id2, Pred<I> pred) {
            preds.put(computeIndex(id1, id2), pred);
        }

        private long computeIndex(long id1, long id2) {
            return id2 * size1 + id1;
        }
    }
}
