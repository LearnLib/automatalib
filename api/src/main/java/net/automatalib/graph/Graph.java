/* Copyright (C) 2013-2024 TU Dortmund University
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

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.common.util.collection.CollectionsUtil;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;

/**
 * Graph interface. Like an {@link IndefiniteGraph}, but with the additional requirement that the set of nodes be
 * finite.
 *
 * @param <N>
 *         node type
 * @param <E>
 *         edge type
 */
public interface Graph<N, E> extends IndefiniteGraph<N, E>, SimpleGraph<N> {

    /**
     * Retrieves, for a given node, the (finite) collection of all outgoing edges.
     *
     * @param node
     *         the node
     *
     * @return a collection containing the outgoing edges
     */
    Collection<E> getOutgoingEdges(N node);

    /**
     * Retrieves, for a given node, the (finite) collection of all adjacent nodes.
     *
     * @param node
     *         the node
     *
     * @return a collection containing the outgoing edges
     */
    default Collection<N> getAdjacentNodes(N node) {
        return CollectionsUtil.map(getOutgoingEdges(node), this::getTarget);
    }

    @Override
    default Iterator<E> getOutgoingEdgesIterator(N node) {
        return getOutgoingEdges(node).iterator();
    }

    /*
     * Refinement of the super method, that no longer makes the edge type variable a wildcard.
     */
    @Override
    default VisualizationHelper<N, E> getVisualizationHelper() {
        return new DefaultVisualizationHelper<>();
    }

    /**
     * Interface for {@link SimpleGraph.IntAbstraction node integer abstractions} of a {@link Graph}.
     *
     * @param <E>
     *         edge type
     */
    interface IntAbstraction<E> extends SimpleGraph.IntAbstraction {

        /**
         * Int-abstracted version of {@link #getOutgoingEdges(Object)}.
         */
        Collection<E> getOutgoingEdges(int node);

        /**
         * Int-abstracted version of {@link #getTarget(Object)}.
         */
        int getIntTarget(E edge);

        /**
         * Int-abstracted version of {@link #getOutgoingEdgesIterator(Object)}.
         */
        default Iterator<E> getOutgoingEdgesIterator(int node) {
            return getOutgoingEdges(node).iterator();
        }

        /**
         * (Finite) int-abstracted version of {@link #getEdgesBetween(Object, Object)}.
         */
        default Collection<E> getEdgesBetween(int from, int to) {
            return IteratorUtil.list(IteratorUtil.filter(getOutgoingEdgesIterator(from),
                                                                 e -> getIntTarget(e) == to));
        }

        /**
         * Int-abstracted version of {@link #isConnected(Object, Object)}.
         */
        @Override
        default boolean isConnected(int source, int target) {
            return IteratorUtil.any(getOutgoingEdgesIterator(source), e -> getIntTarget(e) == target);
        }
    }
}
