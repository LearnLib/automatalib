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
package net.automatalib.graph;

import java.util.Iterator;
import java.util.Objects;

import com.google.common.collect.Iterators;

/**
 * Interface for an (indefinite) graph structure. A graph consists of nodes, each of which has outgoing edges connecting
 * to other nodes. In an indefinite graph, the node set is not required to be finite.
 *
 * @param <N>
 *         node type
 * @param <E>
 *         edge type
 */
public interface IndefiniteGraph<N, E> extends IndefiniteSimpleGraph<N> {

    /**
     * Retrieves, for a given node, all outgoing edges.
     *
     * @param node
     *         the node
     *
     * @return an iterator over the outgoing edges
     */
    Iterator<E> getOutgoingEdgesIterator(N node);

    /**
     * Retrieves, for a given edge, its target node.
     *
     * @param edge
     *         the edge.
     *
     * @return the target node of the given edge.
     */
    N getTarget(E edge);

    /**
     * Returns, for two given nodes, the edges between those nodes.
     *
     * @param from
     *         the source node
     * @param to
     *         the target node
     *
     * @return an iterator over the edges between the two nodes
     */
    default Iterator<E> getEdgesBetween(N from, N to) {
        return Iterators.filter(getOutgoingEdgesIterator(from), e -> Objects.equals(getTarget(e), to));
    }

    @Override
    default Iterator<N> getAdjacentNodesIterator(N node) {
        return Iterators.transform(getOutgoingEdgesIterator(node), this::getTarget);
    }

}
