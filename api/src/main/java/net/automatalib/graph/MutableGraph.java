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
package net.automatalib.graph;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A graph that allows modification. Note that this interface only exposes methods for extending a graph. If also
 * destructive modifications should be performed, {@link ShrinkableGraph} is the adequate interface.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 * @param <NP>
 *         node property class
 * @param <EP>
 *         edge property class
 */
public interface MutableGraph<N, E, NP, EP> extends UniversalGraph<N, E, NP, EP> {

    /**
     * Adds a new node (with an empty property) to the graph.
     *
     * @return the newly inserted node
     */
    default N addNode() {
        return addNode(null);
    }

    /**
     * Adds a new node with the given property to the graph.
     *
     * @param property
     *         the property of the new node
     *
     * @return the newly inserted node
     */
    N addNode(@Nullable NP property);

    /**
     * Inserts an edge in the graph.
     *
     * @param source
     *         the source node of the edge
     * @param target
     *         the target node of the edge
     * @param property
     *         the property of the edge
     *
     * @return the newly inserted edge
     */
    E connect(N source, N target, EP property);

    /**
     * Sets the node property of the given node.
     *
     * @param node
     *         the node
     * @param property
     *         the property to set
     */
    void setNodeProperty(N node, NP property);

    /**
     * Sets the edge property of the given edge.
     *
     * @param edge
     *         the edge
     * @param property
     *         the property to set
     */
    void setEdgeProperty(E edge, EP property);

    /**
     * Interface for {@link UniversalGraph.IntAbstraction node integer abstractions} of a {@link MutableGraph}.
     *
     * @param <E>
     *         edge type
     * @param <NP>
     *         node property type
     * @param <EP>
     *         edge property type
     */
    interface IntAbstraction<E, NP, EP> extends UniversalGraph.IntAbstraction<E, NP, EP> {

        /**
         * Int-abstracted version of {@link #addNode()}.
         *
         * @return the (int-abstracted) id of the newly inserted node
         */
        default int addIntNode() {
            return addIntNode(null);
        }

        /**
         * Int-abstracted version of {@link #addNode(Object)}.
         *
         * @param property
         *         the property of the new node
         *
         * @return the (int-abstracted) id of the newly inserted node
         */
        int addIntNode(@Nullable NP property);

        /**
         * Int-abstracted version of {@link #connect(Object, Object, Object)}.
         *
         * @param source
         *         the (int-abstracted) id of the source node
         * @param target
         *         the (int-abstracted) id of the target node
         * @param property
         *         the property of the edge
         *
         * @return the newly created edge
         */
        E connect(int source, int target, EP property);

        /**
         * Int-abstracted version of {@link #setNodeProperty(Object, Object)}.
         *
         * @param node
         *         the (int-abstracted) id of the node
         * @param property
         *         the property of the node
         */
        void setNodeProperty(int node, NP property);

        /**
         * Int-abstracted version of {@link MutableGraph#setEdgeProperty(Object, Object)}.
         *
         * @param edge
         *         the edge
         * @param property
         *         the property of the edge
         */
        void setEdgeProperty(E edge, EP property);
    }
}
