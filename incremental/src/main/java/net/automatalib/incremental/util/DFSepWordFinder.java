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
package net.automatalib.incremental.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import net.automatalib.automaton.UniversalDeterministicAutomaton.RegularAutomaton;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class for finding separating words between incremental constructions and target automata. This class
 * traverses the incremental construction in a depth-first style. Thus callers must ensure that the incremental
 * construction is either acyclic or provide a proper
 * {@link Visitor#continueExploration(Object, Object) termination criterion}. Furthermore, undefined transitions of the
 * incremental construction are always ignored, since it may contain incomplete information.
 */
public final class DFSepWordFinder {

    private DFSepWordFinder() {
        // prevent instantiation
    }

    /**
     * Searches for a counterexample between the given incremental construction and target automaton.
     *
     * @param inc
     *         (automaton abstraction of) the incremental construction
     * @param target
     *         the target automaton
     * @param inputs
     *         the input symbol to consider while searching
     * @param omitUndefined
     *         a flag whether undefined transitions of the target automaton should be ignored
     * @param visitor
     *         the {@link Visitor} for guiding the traversal
     * @param <S1>
     *         state type of the incremental construction
     * @param <S2>
     *         state type of the target automaton
     * @param <I>
     *         input symbol type
     * @param <T1>
     *         transition type of the incremental construction
     * @param <T2>
     *         transition type of the target automaton
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     *
     * @return a separating word that unveils diverging behavior between the incremental construction and the target
     * automaton, {@code null} if no such word exists
     */
    public static <S1, S2, I, T1, T2, SP, TP> @Nullable Word<I> findSeparatingWord(RegularAutomaton<S1, I, T1, SP, TP> inc,
                                                                                   RegularAutomaton<S2, I, T2, SP, TP> target,
                                                                                   Collection<? extends I> inputs,
                                                                                   boolean omitUndefined,
                                                                                   Visitor<S1, S2, T1, T2> visitor) {

        final S1 treeInit = inc.getInitialState();
        final S2 targetInit = target.getInitialState();

        if (treeInit == null) {
            return null;
        } else if (targetInit == null) {
            return omitUndefined ? null : Word.epsilon();
        }

        if (visitor.initConflict(treeInit, targetInit)) {
            return Word.epsilon();
        }

        // incomingInput can be null here, because we will always skip the bottom stack element below
        @SuppressWarnings("nullness")
        final Record<S1, @Nullable S2, I> init = new Record<>(treeInit, targetInit, null, inputs.iterator());
        final Deque<Record<S1, @Nullable S2, I>> dfsStack = new ArrayDeque<>();

        dfsStack.push(init);

        while (!dfsStack.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull Record<S1, @Nullable S2, I> rec = dfsStack.peek();
            if (!rec.inputIt.hasNext()) {
                dfsStack.pop();
                continue;
            }

            final I input = rec.inputIt.next();
            final T1 treeTrans = inc.getTransition(rec.treeState, input);

            if (treeTrans == null) {
                continue;
            }

            final @Nullable S2 targetState = rec.targetState;
            final T2 targetTrans = targetState == null ? null : target.getTransition(targetState, input);

            if (targetTrans == null && omitUndefined) {
                continue;
            }

            if (visitor.succConflict(treeTrans, targetTrans)) {
                WordBuilder<I> wb = new WordBuilder<>(dfsStack.size());
                wb.append(input);

                dfsStack.pop();
                while (!dfsStack.isEmpty()) {
                    wb.append(rec.incomingInput);
                    rec = dfsStack.pop();
                }
                return wb.reverse().toWord();
            }

            final S1 treeSucc = inc.getSuccessor(treeTrans);
            final S2 targetSucc = targetTrans == null ? null : target.getSuccessor(targetTrans);

            if (visitor.continueExploration(treeSucc, targetSucc)) {
                dfsStack.push(new Record<>(treeSucc, targetSucc, input, inputs.iterator()));
            }
        }

        return null;
    }

    /**
     * A utility class for steering the exploration of the incremental construction and the target automaton.
     *
     * @param <S1>
     *         state type of the incremental construction
     * @param <S2>
     *         state type of the target automaton
     * @param <T1>
     *         transition type of the incremental construction
     * @param <T2>
     *         transition type of the target automaton
     */
    public interface Visitor<S1, S2, T1, T2> {

        /**
         * Checks whether the initial states of the incremental construction and the target automaton are in conflict.
         *
         * @param incInit
         *         initial state of the incremental construction
         * @param targetInit
         *         initial state of the target automaton
         *
         * @return {@code true} if the states are in conflict (and thus a separating word has been found), {@code false}
         * otherwise
         */
        default boolean initConflict(S1 incInit, S2 targetInit) {
            return false;
        }

        /**
         * Checks whether the transitions of the incremental construction and the target automaton are in conflict.
         *
         * @param incTrans
         *         transition of the incremental construction
         * @param targetTrans
         *         transition of the target automaton
         *
         * @return {@code true} if the transitions (i.e., their transition properties or their successors)  are in
         * conflict (and thus a separating word has been found), {@code false} otherwise
         */
        default boolean succConflict(T1 incTrans, @Nullable T2 targetTrans) {
            return false;
        }

        /**
         * Checks whether the successor states of the incremental construction and the target automaton should be
         * explored further.
         *
         * @param incSucc
         *         successor state of the incremental construction
         * @param targetSucc
         *         successor state of the target automaton
         *
         * @return {@code true} if the pair of states should be added to the DFS queue for further exploration,
         * {@code false} otherwise
         */
        default boolean continueExploration(S1 incSucc, @Nullable S2 targetSucc) {
            return true;
        }
    }

    private static final class Record<S1, S2, I> {

        final S1 treeState;
        final S2 targetState;
        final I incomingInput;
        final Iterator<? extends I> inputIt;

        Record(S1 treeState, S2 targetState, I incomingInput, Iterator<? extends I> inputIt) {
            this.targetState = targetState;
            this.treeState = treeState;
            this.incomingInput = incomingInput;
            this.inputIt = inputIt;
        }
    }
}
