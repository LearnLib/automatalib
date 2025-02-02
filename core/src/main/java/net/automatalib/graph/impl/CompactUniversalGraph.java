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
package net.automatalib.graph.impl;

import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.graph.base.AbstractCompactUniversalGraph;
import net.automatalib.graph.base.CompactEdge;

/**
 * A compact graph representation that supports arbitrary node properties and edge properties.
 *
 * @param <NP>
 *         node property type
 * @param <EP>
 *         edge property type
 */
public class CompactUniversalGraph<NP, EP> extends AbstractCompactUniversalGraph<CompactEdge<EP>, NP, EP> {

    private final ArrayStorage<NP> nodeProperties;

    public CompactUniversalGraph() {
        this.nodeProperties = new ArrayStorage<>();
    }

    public CompactUniversalGraph(int initialCapacity) {
        super(initialCapacity);
        this.nodeProperties = new ArrayStorage<>(initialCapacity);
    }

    @Override
    public void setNodeProperty(int node, NP property) {
        nodeProperties.ensureCapacity(node + 1);
        nodeProperties.set(node, property);
    }

    @Override
    public NP getNodeProperty(int node) {
        return nodeProperties.get(node);
    }

    @Override
    protected CompactEdge<EP> createEdge(int source, int target, EP property) {
        return new CompactEdge<>(target, property);
    }

}
