/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.graphs;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.common.collect.Iterators;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.graphs.helpers.SimpleNodeIDs;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;

/**
 * The finite version of a {@link IndefiniteSimpleGraph}.
 *
 * @param <N>
 *         node type
 *
 * @author Malte Isberner
 */
public interface SimpleGraph<N> extends IndefiniteSimpleGraph<N>, Iterable<N> {

    /**
     * Retrieves the number of nodes of this graph.
     *
     * @return the number of nodes of this graph.
     */
    default int size() {
        return getNodes().size();
    }

    /**
     * Retrieves an (unmodifiable) collection of the nodes in this graph.
     *
     * @return the nodes in this graph
     */
    @Nonnull
    Collection<N> getNodes();

    @Override
    @Nonnull
    default Iterator<N> iterator() {
        return Iterators.unmodifiableIterator(getNodes().iterator());
    }

    default Stream<N> nodesStream() {
        return getNodes().stream();
    }

    @Nonnull
    default NodeIDs<N> nodeIDs() {
        return new SimpleNodeIDs<>(this);
    }

    default VisualizationHelper<N, ?> getVisualizationHelper() {
        return new DefaultVisualizationHelper<>();
    }

    @Override
    default Graph<N, ?> asNormalGraph() {
        return new NormalGraphView<>(this);
    }

    class NormalGraphView<N, G extends SimpleGraph<N>> extends IndefiniteSimpleGraph.NormalGraphView<N, G>
            implements Graph<N, N> {

        public NormalGraphView(G simpleGraph) {
            super(simpleGraph);
        }

        @Override
        public int size() {
            return simpleGraph.size();
        }

        @Override
        public Collection<N> getNodes() {
            return simpleGraph.getNodes();
        }

        @Override
        public Iterator<N> iterator() {
            return simpleGraph.iterator();
        }

        @Override
        public Stream<N> nodesStream() {
            return simpleGraph.nodesStream();
        }

    }

}
