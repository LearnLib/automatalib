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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import net.automatalib.graphs.BidirectionalGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactSimpleBidiGraph<@Nullable EP> extends AbstractCompactSimpleGraph<CompactBidiEdge<EP>, EP>
        implements BidirectionalGraph<Integer, CompactBidiEdge<EP>> {

    private final ResizingArrayStorage<List<CompactBidiEdge<EP>>> inEdges;

    public CompactSimpleBidiGraph() {
        this.inEdges = new ResizingArrayStorage<>(List.class);
    }

    public CompactSimpleBidiGraph(int initialCapacity) {
        super(initialCapacity);
        this.inEdges = new ResizingArrayStorage<>(List.class, initialCapacity);
    }

    @Override
    public Collection<CompactBidiEdge<EP>> getIncomingEdges(Integer node) {
        return getIncomingEdges(node.intValue());
    }

    public Collection<CompactBidiEdge<EP>> getIncomingEdges(int node) {
        return Collections.unmodifiableCollection(getInEdgeList(node));
    }

    protected List<CompactBidiEdge<EP>> getInEdgeList(int node) {
        return inEdges.array[node];
    }

    @Override
    public Integer getSource(CompactBidiEdge<EP> edge) {
        return Integer.valueOf(getIntSource(edge));
    }

    public int getIntSource(CompactBidiEdge<EP> edge) {
        return edge.getSource();
    }

    @Override
    public int addIntNode(Void properties) {
        inEdges.ensureCapacity(size + 1);
        int node = super.addIntNode(properties);
        inEdges.array[node] = new ArrayList<>();
        return node;
    }

    @Override
    public CompactBidiEdge<EP> connect(int source, int target, @Nullable EP property) {
        CompactBidiEdge<EP> edge = super.connect(source, target, property);
        List<CompactBidiEdge<EP>> inEdges = getInEdgeList(target);
        edge.inIndex = inEdges.size();
        inEdges.add(edge);
        return edge;
    }

    @Override
    protected CompactBidiEdge<EP> createEdge(int source, int target, @Nullable EP property) {
        return new CompactBidiEdge<>(source, target, property);
    }

}
