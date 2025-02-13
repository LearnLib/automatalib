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
package net.automatalib.incremental.mealy.tree;

import net.automatalib.common.util.array.ArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A node in the tree internally used by {@link IncrementalMealyTreeBuilder}.
 *
 * @param <O>
 *         output symbol type
 */
final class Node<O> {

    private final ArrayStorage<@Nullable Edge<Node<O>, O>> outEdges;

    Node(int alphabetSize) {
        this.outEdges = new ArrayStorage<>(alphabetSize);
    }

    @Nullable Edge<Node<O>, O> getEdge(int idx) {
        return outEdges.get(idx);
    }

    void setEdge(int idx, @Nullable Edge<Node<O>, O> edge) {
        outEdges.set(idx, edge);
    }

    @Nullable Node<O> getSuccessor(int idx) {
        Edge<Node<O>, O> edge = outEdges.get(idx);
        if (edge != null) {
            return edge.getTarget();
        }
        return null;
    }

    void ensureInputCapacity(int capacity) {
        this.outEdges.ensureCapacity(capacity);
    }
}
