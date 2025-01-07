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

import java.util.Iterator;
import java.util.Objects;

import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.mapping.MapMapping;
import net.automatalib.common.util.mapping.MutableMapping;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simplified interface for indefinite graphs, exposing only adjacency information, but no further information about
 * edge objects.
 *
 * @param <N>
 *         node type
 */
public interface IndefiniteSimpleGraph<N> extends Iterable<N> {

    /**
     * Retrieves, for a given node, all adjacent nodes.
     *
     * @param node
     *         the node
     *
     * @return an iterator over the adjacent nodes
     */
    Iterator<N> getAdjacentNodesIterator(N node);

    /**
     * Checks, for two given nodes, whether they are connected.
     *
     * @param source
     *         the source node
     * @param target
     *         the target node
     *
     * @return {@code true} if the nodes are connect, {@code false} otherwise
     */
    default boolean isConnected(N source, N target) {
        return IteratorUtil.any(getAdjacentNodesIterator(source), n -> Objects.equals(n, target));
    }

    /**
     * Creates a {@link MutableMapping} allowing to associate arbitrary data with this graph's nodes. The returned
     * mapping is however only guaranteed to work correctly if the transition system is not modified.
     *
     * @param <V>
     *         the value type of the mapping
     *
     * @return the mutable mapping
     */
    default <@Nullable V> MutableMapping<N, V> createStaticNodeMapping() {
        return new MapMapping<>();
    }

    /**
     * Creates a {@link MutableMapping} allowing to associate arbitrary data with this graph's nodes. The returned
     * mapping maintains the association even when the transition system is modified.
     *
     * @param <V>
     *         the value type of the mapping
     *
     * @return the mutable mapping
     */
    default <@Nullable V> MutableMapping<N, V> createDynamicNodeMapping() {
        return new MapMapping<>();
    }

}
