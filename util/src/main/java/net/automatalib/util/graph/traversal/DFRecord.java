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

import org.checkerframework.checker.nullness.qual.Nullable;

final class DFRecord<N, E, D> extends SimpleDFRecord<N, E> {

    final D data;
    private @Nullable LastEdge<E, N, D> lastEdge;

    DFRecord(N node, D data) {
        super(node);
        this.data = data;
    }

    @Nullable LastEdge<E, N, D> getLastEdge() {
        LastEdge<E, N, D> result = lastEdge;
        lastEdge = null;
        return result;
    }

    void setLastEdge(E edge, N tgtNode, D tgtData) {
        assert lastEdge == null;
        lastEdge = new LastEdge<>(edge, tgtNode, tgtData);
    }

    static final class LastEdge<E, N, D> {

        public final E edge;
        public final N node;
        public final D data;

        LastEdge(E edge, N node, D data) {
            this.edge = edge;
            this.node = node;
            this.data = data;
        }
    }
}
