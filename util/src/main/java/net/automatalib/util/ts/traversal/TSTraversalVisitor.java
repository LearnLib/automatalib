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
package net.automatalib.util.ts.traversal;

import net.automatalib.common.util.Holder;

/**
 * Visitor interface for transition system traversals.
 * <p>
 * This interface declares methods that are called upon basic transition system traversal actions.
 *
 * @param <S>
 *         state class
 * @param <I>
 *         input symbol class
 * @param <T>
 *         transition class
 * @param <D>
 *         user data class
 */
public interface TSTraversalVisitor<S, I, T, D> {

    /**
     * Called when the initial states of the transition system are processed.
     *
     * @param initialState
     *         the state that is processed
     * @param holder
     *         a writable reference whose (node-specific) data is passed to the corresponding methods during traversal
     *
     * @return the action to perform
     */
    default TSTraversalAction processInitial(S initialState, Holder<D> holder) {
        return TSTraversalAction.EXPLORE;
    }

    /**
     * Called when the exploration of a state is started.
     *
     * @param state
     *         the state whose exploration is about to be started
     * @param data
     *         the user data associated with this state
     *
     * @return {@code true}, if the state should be explored, {@code false} otherwise
     */
    default boolean startExploration(S state, D data) {
        return true;
    }

    /**
     * Called when an edge is processed.
     *
     * @param srcState
     *         the source state
     * @param srcData
     *         the user data associated with the source state
     * @param input
     *         the input that is being processed
     * @param transition
     *         the transition that is being processed
     * @param tgtState
     *         the target state
     * @param tgtHolder
     *         a writable reference to provide user data that should be associated with the target state
     *
     * @return the action to perform
     */
    default TSTraversalAction processTransition(S srcState,
                                                D srcData,
                                                I input,
                                                T transition,
                                                S tgtState,
                                                Holder<D> tgtHolder) {
        return TSTraversalAction.EXPLORE;
    }

    /**
     * Called when an edge is backtracked. This typically happens only in depth-first style traversals.
     *
     * @param srcState
     *         the source state
     * @param srcData
     *         the user data associated with the source state
     * @param input
     *         the input that is being processed
     * @param transition
     *         the transition that is being processed
     * @param tgtState
     *         the target state
     * @param tgtData
     *         the user data associated with the target state
     */
    default void backtrackTransition(S srcState, D srcData, I input, T transition, S tgtState, D tgtData) {}

    /**
     * Called when the exploration of a state is finished.
     *
     * @param state
     *         the state whose exploration is being finished
     * @param data
     *         the user data associated with this state
     */
    default void finishExploration(S state, D data) {}
}
