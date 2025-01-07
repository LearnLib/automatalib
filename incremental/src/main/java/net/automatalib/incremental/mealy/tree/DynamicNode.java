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

import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A map-based node in the tree internally used by {@link DynamicIncrementalMealyTreeBuilder}.
 *
 * @param <O>
 *         output symbol type
 */
final class DynamicNode<I, O> {

    private final Map<I, Edge<DynamicNode<I, O>, O>> outEdges;

    DynamicNode() {
        this.outEdges = new HashMap<>();
    }

    @Nullable Edge<DynamicNode<I, O>, O> getEdge(I input) {
        return outEdges.get(input);
    }

    void setEdge(I symbol, Edge<DynamicNode<I, O>, O> edge) {
        outEdges.put(symbol, edge);
    }

    Map<I, Edge<DynamicNode<I, O>, O>> getOutEdges() {
        return outEdges;
    }
}
