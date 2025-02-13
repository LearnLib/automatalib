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

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.graph.concept.NodeIDs;
import net.automatalib.graph.helper.NodeIDGrowingMapping;
import net.automatalib.graph.helper.NodeIDStaticMapping;
import net.automatalib.graph.helper.SimpleNodeIDs;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;

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

    @Override
    default Iterator<N> iterator() {
        return getNodes().iterator();
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

    @Override
    default <V> MutableMapping<N, V> createStaticNodeMapping() {
        return new NodeIDStaticMapping<>(nodeIDs(), size());
    }

    @Override
    default <V> MutableMapping<N, V> createDynamicNodeMapping() {
        return new NodeIDGrowingMapping<>(nodeIDs(), size());
    }

    /**
     * Returns the {@link VisualizationHelper} that contains information for displaying this graph.
     *
     * @return the visualization helper
     */
    default VisualizationHelper<N, ?> getVisualizationHelper() {
        return new DefaultVisualizationHelper<>();
    }

    /**
     * Basic interface for integer abstractions of graphs. In an integer abstraction, each node of a graph is identified
     * with an integer in the range {@code [0, size() - 1]}.
     */
    interface IntAbstraction extends FiniteRepresentation {

        boolean isConnected(int source, int target);
    }
}
