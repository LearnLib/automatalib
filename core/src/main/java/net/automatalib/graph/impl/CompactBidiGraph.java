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
import net.automatalib.graph.base.AbstractCompactBidiGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactBidiGraph<@Nullable NP, @Nullable EP> extends AbstractCompactBidiGraph<NP, EP> {

    private final ArrayStorage<NP> nodeProperties;

    public CompactBidiGraph() {
        this.nodeProperties = new ArrayStorage<>();
    }

    public CompactBidiGraph(int initialCapacity) {
        super(initialCapacity);
        this.nodeProperties = new ArrayStorage<>(initialCapacity);
    }

    @Override
    public void setNodeProperty(int node, @Nullable NP property) {
        nodeProperties.ensureCapacity(node + 1);
        nodeProperties.set(node, property);
    }

    @Override
    public NP getNodeProperty(int node) {
        return nodeProperties.get(node);
    }

}
