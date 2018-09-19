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
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.automatalib.commons.util.mappings.MapMapping;
import net.automatalib.commons.util.mappings.MutableMapping;

/**
 * A simplified interface for indefinite graphs, exposing only adjacency information, but no further information about
 * edge objects.
 *
 * @param <N>
 *         node type
 *
 * @author Malte Isberner
 */
public interface IndefiniteSimpleGraph<N> {

    @Nonnull
    default Iterable<N> adjacentTargets(N node) {
        return () -> adjacentTargetsIterator(node);
    }

    @Nonnull
    default Iterator<N> adjacentTargetsIterator(N node) {
        return getAdjacentTargets(node).iterator();
    }

    @Nonnull
    Collection<N> getAdjacentTargets(N node);

    default boolean isConnected(N source, N target) {
        return adjacentTargetsStream(source).anyMatch(n -> Objects.equals(n, target));
    }

    @Nonnull
    default Stream<N> adjacentTargetsStream(N node) {
        return getAdjacentTargets(node).stream();
    }

    @Nonnull
    default <V> MutableMapping<N, V> createStaticNodeMapping() {
        return new MapMapping<>();
    }

    @Nonnull
    default <V> MutableMapping<N, V> createDynamicNodeMapping() {
        return new MapMapping<>();
    }

    default IndefiniteGraph<N, ?> asNormalGraph() {
        return new NormalGraphView<>(this);
    }

    class NormalGraphView<N, G extends IndefiniteSimpleGraph<N>> implements IndefiniteGraph<N, N> {

        protected final G simpleGraph;

        public NormalGraphView(G simpleGraph) {
            this.simpleGraph = simpleGraph;
        }

        @Override
        public Iterator<N> adjacentTargetsIterator(N node) {
            return simpleGraph.adjacentTargetsIterator(node);
        }

        @Override
        public Collection<N> getAdjacentTargets(N node) {
            return simpleGraph.getAdjacentTargets(node);
        }

        @Override
        public Stream<N> adjacentTargetsStream(N node) {
            return simpleGraph.adjacentTargetsStream(node);
        }

        @Override
        public Collection<N> getOutgoingEdges(N node) {
            return simpleGraph.getAdjacentTargets(node);
        }

        @Override
        public N getTarget(N edge) {
            return edge;
        }

        @Override
        public Iterable<N> adjacentTargets(N node) {
            return simpleGraph.adjacentTargets(node);
        }

        @Override
        public boolean isConnected(N source, N target) {
            return simpleGraph.isConnected(source, target);
        }

        @Override
        public <V> MutableMapping<N, V> createStaticNodeMapping() {
            return simpleGraph.createStaticNodeMapping();
        }

        @Override
        public <V> MutableMapping<N, V> createDynamicNodeMapping() {
            return simpleGraph.createDynamicNodeMapping();
        }
    }
}
