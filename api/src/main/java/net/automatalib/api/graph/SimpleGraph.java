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
package net.automatalib.api.graph;

import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.Iterators;
import net.automatalib.api.automaton.concept.FiniteRepresentation;
import net.automatalib.api.graph.concept.NodeIDs;
import net.automatalib.api.graph.helper.SimpleNodeIDs;
import net.automatalib.api.visualization.DefaultVisualizationHelper;
import net.automatalib.api.visualization.VisualizationHelper;

/**
 * The finite version of a {@link IndefiniteSimpleGraph}.
 *
 * @param <N>
 *         node type
 */
public interface SimpleGraph<N> extends IndefiniteSimpleGraph<N>, FiniteRepresentation {

    /**
     * Retrieves an (unmodifiable) collection of the nodes in this graph.
     *
     * @return the nodes in this graph
     */
    Collection<N> getNodes();

    default NodeIDs<N> nodeIDs() {
        return new SimpleNodeIDs<>(this);
    }

    /**
     * Returns the {@link VisualizationHelper} that contains information for displaying this graph.
     *
     * @return the visualization helper
     */
    default VisualizationHelper<N, ?> getVisualizationHelper() {
        return new DefaultVisualizationHelper<>();
    }

    @Override
    default Iterator<N> iterator() {
        return Iterators.unmodifiableIterator(getNodes().iterator());
    }

    /**
     * Retrieves the size (number of nodes) of this graph.
     *
     * @return the number of nodes of this graph
     */
    @Override
    default int size() {
        return getNodes().size();
    }

    /**
     * Basic interface for integer abstractions of graphs. In an integer abstraction, each node of a graph is identified
     * with an integer in the range {@code [0, size() - 1]}.
     */
    interface IntAbstraction extends FiniteRepresentation {

        boolean isConnected(int source, int target);
    }
}
