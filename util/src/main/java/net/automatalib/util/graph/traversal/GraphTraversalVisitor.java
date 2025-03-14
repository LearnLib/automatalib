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
package net.automatalib.util.graph.traversal;

import net.automatalib.common.util.Holder;

/**
 * Visitor interface for graph traversals.
 * <p>
 * This interface declares methods that are called upon basic graph traversal actions.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 * @param <D>
 *         user data class
 */
public interface GraphTraversalVisitor<N, E, D> {

    /**
     * Called when the initial nodes (as passed to the traversal method) are processed.
     *
     * @param initialNode
     *         the node that is processed
     * @param holder
     *         a writable reference whose (node-specific) data is passed to the corresponding methods during traversal
     *
     * @return the action to perform
     */
    default GraphTraversalAction processInitial(N initialNode, Holder<D> holder) {
        return GraphTraversalAction.EXPLORE;
    }

    /**
     * Called when the exploration of a node is started.
     *
     * @param node
     *         the node whose exploration is about to be started
     * @param data
     *         the user data associated with this node
     *
     * @return {@code true}, if the node should be explored, {@code false} otherwise
     */
    default boolean startExploration(N node, D data) {
        return true;
    }

    /**
     * Called when an edge is processed.
     *
     * @param srcNode
     *         the source node
     * @param srcData
     *         the user data associated with the source node
     * @param edge
     *         the edge that is being processed
     * @param tgtNode
     *         the target node
     * @param tgtHolder
     *         a writable reference to provide user data that should be associated with the target node
     *
     * @return the action to perform
     */
    default GraphTraversalAction processEdge(N srcNode, D srcData, E edge, N tgtNode, Holder<D> tgtHolder) {
        return GraphTraversalAction.EXPLORE;
    }

    /**
     * Called when an edge is backtracked. This typically happens only in depth-first style traversals.
     *
     * @param srcNode
     *         the source node
     * @param srcData
     *         the user data associated with the source node
     * @param edge
     *         the edge that is being processed
     * @param tgtNode
     *         the target node
     * @param tgtData
     *         the user data associated with the target node
     */
    default void backtrackEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {}

    /**
     * Called when the exploration of a node is finished.
     *
     * @param node
     *         the node whose exploration is being finished
     * @param data
     *         the user data associated with this node
     */
    default void finishExploration(N node, D data) {}
}
