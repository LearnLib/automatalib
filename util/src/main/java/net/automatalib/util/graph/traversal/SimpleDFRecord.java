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
package net.automatalib.util.graph.traversal;

import java.util.Iterator;

import net.automatalib.graph.IndefiniteGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

class SimpleDFRecord<N, E> {

    final N node;
    private @Nullable Iterator<E> edgeIterator;

    SimpleDFRecord(N node) {
        this.node = node;
    }

    boolean wasStarted() {
        return edgeIterator != null;
    }

    boolean start(IndefiniteGraph<N, E> graph) {
        if (edgeIterator != null) {
            return false;
        }
        this.edgeIterator = graph.getOutgoingEdgesIterator(node);
        return true;
    }

    boolean hasNextEdge() {
        if (edgeIterator == null) {
            throw new IllegalStateException("Edge iteration not yet started");
        }
        return edgeIterator.hasNext();
    }

    E nextEdge() {
        if (edgeIterator == null) {
            throw new IllegalStateException("Edge iteration not yet started");
        }
        return edgeIterator.next();
    }

}
