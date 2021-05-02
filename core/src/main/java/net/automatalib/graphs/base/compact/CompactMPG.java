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

import java.util.Collections;
import java.util.Set;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import net.automatalib.graphs.MutableModalProcessGraph;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.MutableProceduralModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty.ProceduralType;
import net.automatalib.ts.modal.transition.ProceduralModalEdgePropertyImpl;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactMPG<L, AP>
        extends AbstractCompactGraph<CompactMPGEdge<L, MutableProceduralModalEdgeProperty>, Set<AP>, MutableProceduralModalEdgeProperty>
        implements MutableModalProcessGraph<Integer, L, CompactMPGEdge<L, MutableProceduralModalEdgeProperty>, AP, MutableProceduralModalEdgeProperty> {

    private final ResizingArrayStorage<@Nullable Set<AP>> nodeProperties;
    private int initialNode;
    private int finalNode;

    public CompactMPG() {
        super();
        this.nodeProperties = new ResizingArrayStorage<>(Set.class);
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
    public L getEdgeLabel(CompactMPGEdge<L, MutableProceduralModalEdgeProperty> edge) {
        return edge.getLabel();
    }

    @Override
    public void setEdgeLabel(CompactMPGEdge<L, MutableProceduralModalEdgeProperty> edge, L label) {
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
    public void setNodeProperty(int node, @Nullable Set<AP> property) {
        nodeProperties.ensureCapacity(node);
        nodeProperties.array[node] = property;
    }

    @Override
    protected CompactMPGEdge<L, MutableProceduralModalEdgeProperty> createEdge(int source,
                                                                               int target,
                                                                               @Nullable MutableProceduralModalEdgeProperty property) {
        final MutableProceduralModalEdgeProperty prop;

        if (property == null) {
            prop = new ProceduralModalEdgePropertyImpl(ProceduralType.INTERNAL, ModalType.MUST);
        } else {
            prop = property;
        }

        return new CompactMPGEdge<>(target, prop, null);
    }

    @Override
    public Set<AP> getNodeProperties(int node) {
        if (node > nodeProperties.array.length) {
            return Collections.emptySet();
        }

        final Set<AP> props = nodeProperties.array[node];
        return props == null ? Collections.emptySet() : props;
    }
}
