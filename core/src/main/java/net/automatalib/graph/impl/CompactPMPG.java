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

import java.util.Collections;
import java.util.Set;

import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.graph.MutableProceduralModalProcessGraph;
import net.automatalib.graph.base.AbstractCompactUniversalGraph;
import net.automatalib.ts.modal.transition.MutableProceduralModalEdgeProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactPMPG<L, AP>
        extends AbstractCompactUniversalGraph<CompactPMPGEdge<L, MutableProceduralModalEdgeProperty>, Set<AP>, MutableProceduralModalEdgeProperty>
        implements MutableProceduralModalProcessGraph<Integer, L, CompactPMPGEdge<L, MutableProceduralModalEdgeProperty>, AP, MutableProceduralModalEdgeProperty> {

    private final ArrayStorage<Set<AP>> nodeProperties;
    private final L defaultLabel;
    private int initialNode;
    private int finalNode;

    public CompactPMPG(L defaultLabel) {
        this.nodeProperties = new ArrayStorage<>();
        this.defaultLabel = defaultLabel;
        this.initialNode = -1;
        this.finalNode = -1;
    }

    @Override
    public void setInitialNode(@Nullable Integer initialNode) {
        if (initialNode == null) {
            this.initialNode = -1;
        } else {
            this.initialNode = initialNode;
        }
    }

    @Override
    public void setFinalNode(@Nullable Integer finalNode) {
        if (finalNode == null) {
            this.finalNode = -1;
        } else {
            this.finalNode = finalNode;
        }
    }

    @Override
    public L getEdgeLabel(CompactPMPGEdge<L, MutableProceduralModalEdgeProperty> edge) {
        return edge.getLabel();
    }

    @Override
    public void setEdgeLabel(CompactPMPGEdge<L, MutableProceduralModalEdgeProperty> edge, L label) {
        edge.setLabel(label);
    }

    @Override
    public @Nullable Integer getFinalNode() {
        return this.finalNode < 0 ? null : this.finalNode;
    }

    @Override
    public @Nullable Integer getInitialNode() {
        return this.initialNode < 0 ? null : this.initialNode;
    }

    @Override
    public void setNodeProperty(int node, Set<AP> property) {
        nodeProperties.ensureCapacity(node + 1);
        nodeProperties.set(node, property);
    }

    @Override
    protected CompactPMPGEdge<L, MutableProceduralModalEdgeProperty> createEdge(int source,
                                                                                int target,
                                                                                MutableProceduralModalEdgeProperty property) {
        return new CompactPMPGEdge<>(target, property, this.defaultLabel);
    }

    @Override
    public Set<AP> getNodeProperty(int node) {
        if (node > nodeProperties.size()) {
            return Collections.emptySet();
        }

        final Set<AP> props = nodeProperties.get(node);
        return props == null ? Collections.emptySet() : props;
    }
}
