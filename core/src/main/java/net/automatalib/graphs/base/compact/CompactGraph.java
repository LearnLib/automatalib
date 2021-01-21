/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.graphs.base.compact;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactGraph<@Nullable NP, @Nullable EP> extends AbstractCompactGraph<CompactEdge<EP>, NP, EP> {

    private final ResizingArrayStorage<NP> nodeProperties;

    public CompactGraph() {
        super();
        this.nodeProperties = new ResizingArrayStorage<>(Object.class);
    }

    public CompactGraph(int initialCapacity) {
        super(initialCapacity);
        this.nodeProperties = new ResizingArrayStorage<>(Object.class, initialCapacity);
    }

    @Override
    public void setNodeProperty(int node, @Nullable NP property) {
        if (node >= nodeProperties.array.length) {
            nodeProperties.ensureCapacity(size);
        }
        nodeProperties.array[node] = property;
    }

    @Override
    protected CompactEdge<EP> createEdge(int source, int target, @Nullable EP property) {
        return new CompactEdge<>(target, property);
    }

    @Override
    public NP getNodeProperties(int node) {
        if (node < nodeProperties.array.length) {
            return nodeProperties.array[node];
        }
        return null;
    }

}
