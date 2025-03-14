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
package net.automatalib.graph.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.graph.BidirectionalGraph;
import net.automatalib.graph.MutableUniversalBidirectionalGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractCompactUniversalBidiGraph<NP, EP>
        extends AbstractCompactUniversalGraph<CompactBidiEdge<EP>, NP, EP>
        implements MutableUniversalBidirectionalGraph<Integer, CompactBidiEdge<EP>, NP, EP>,
                   BidirectionalGraph.IntAbstraction<CompactBidiEdge<EP>> {

    private final ArrayStorage<List<CompactBidiEdge<EP>>> inEdges;

    public AbstractCompactUniversalBidiGraph() {
        this.inEdges = new ArrayStorage<>();
    }

    public AbstractCompactUniversalBidiGraph(int initialCapacity) {
        super(initialCapacity);
        this.inEdges = new ArrayStorage<>(initialCapacity);
    }

    @Override
    public Collection<CompactBidiEdge<EP>> getIncomingEdges(Integer node) {
        return getIncomingEdges(node.intValue());
    }

    @Override
    public Collection<CompactBidiEdge<EP>> getIncomingEdges(int node) {
        return Collections.unmodifiableCollection(this.inEdges.get(node));
    }

    @Override
    public int addIntNode(@Nullable NP property) {
        int node = super.addIntNode(property);
        inEdges.ensureCapacity(node + 1);
        inEdges.set(node, new ArrayList<>());
        return node;
    }

    @Override
    public Integer getSource(CompactBidiEdge<EP> edge) {
        return getIntSource(edge);
    }

    @Override
    public int getIntSource(CompactBidiEdge<EP> edge) {
        return edge.getSource();
    }

    @Override
    public CompactBidiEdge<EP> connect(int source, int target, EP property) {
        CompactBidiEdge<EP> edge = super.connect(source, target, property);
        List<CompactBidiEdge<EP>> inEdges = this.inEdges.get(target);
        inEdges.add(edge);
        return edge;
    }

    @Override
    protected CompactBidiEdge<EP> createEdge(int source, int target, EP property) {
        return new CompactBidiEdge<>(source, target, property);
    }

}
