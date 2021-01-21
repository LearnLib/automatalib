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
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.graphs.MutableGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractCompactGraph<E extends CompactEdge<EP>, NP, EP>
        implements MutableGraph<Integer, E, NP, EP>, NodeIDs<Integer> {

    protected final ResizingArrayStorage<List<E>> edges;
    protected int size;

    public AbstractCompactGraph() {
        this.edges = new ResizingArrayStorage<>(List.class);
    }

    public AbstractCompactGraph(int initialCapacity) {
        this.edges = new ResizingArrayStorage<>(List.class, initialCapacity);
    }

    @Override
    public Collection<Integer> getNodes() {
        return CollectionsUtil.intRange(0, size);
    }

    @Override
    public NodeIDs<Integer> nodeIDs() {
        return this;
    }

    @Override
    public Collection<E> getOutgoingEdges(Integer node) {
        return getOutgoingEdges(node.intValue());
    }

    public Collection<E> getOutgoingEdges(int node) {
        return Collections.unmodifiableCollection(getOutEdgeList(node));
    }

    protected List<E> getOutEdgeList(int node) {
        return edges.array[node];
    }

    @Override
    public Integer getTarget(E edge) {
        return Integer.valueOf(edge.getTarget());
    }

    @Override
    public Integer addNode(@Nullable NP properties) {
        return Integer.valueOf(addIntNode(properties));
    }

    public int addIntNode(@Nullable NP properties) {
        edges.ensureCapacity(size + 1);
        edges.array[size] = new ArrayList<>();
        int n = size++;
        setNodeProperty(n, properties);
        return n;
    }

    public int addIntNode() {
        return addIntNode(null);
    }

    @Override
    public void setNodeProperty(Integer node, NP property) {
        setNodeProperty(node.intValue(), property);
    }

    public abstract void setNodeProperty(int node, @Nullable NP property);

    @Override
    public E connect(Integer source, Integer target, @Nullable EP properties) {
        return connect(source.intValue(), target.intValue(), properties);
    }

    public E connect(int source, int target, @Nullable EP property) {
        E edge = createEdge(source, target, property);
        List<E> edges = getOutEdgeList(source);
        edge.outIndex = edges.size();
        edges.add(edge);
        return edge;
    }

    public CompactEdge<EP> connect(int source, int target) {
        return connect(source, target, null);
    }

    protected abstract E createEdge(int source, int target, @Nullable EP property);

    @Override
    public void setEdgeProperty(E edge, EP property) {
        edge.setProperty(property);
    }

    @Override
    public int getNodeId(Integer node) {
        return node.intValue();
    }

    @Override
    public Integer getNode(int id) {
        return Integer.valueOf(id);
    }

    @Override
    public NP getNodeProperty(Integer node) {
        return getNodeProperties(node.intValue());
    }

    public abstract NP getNodeProperties(int node);

    @Override
    public EP getEdgeProperty(E edge) {
        return edge.getProperty();
    }

}