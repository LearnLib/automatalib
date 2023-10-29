/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.example.graph;

import java.util.HashMap;
import java.util.Map;

import net.automatalib.common.util.Holder;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.graph.CompactSimpleGraph;
import net.automatalib.graph.IndefiniteGraph;
import net.automatalib.graph.base.CompactEdge;
import net.automatalib.util.graph.traversal.GraphTraversal;
import net.automatalib.util.graph.traversal.GraphTraversalAction;
import net.automatalib.util.graph.traversal.GraphTraversalVisitor;
import net.automatalib.visualization.Visualization;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.visualization.VisualizationHelper.EdgeStyles;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A small example of a {@link GraphTraversal graph traversal} that uses a custom {@link GraphTraversalVisitor} to
 * generate some visualization properties based on the order in which nodes and edges are discovered.
 */
public final class DFSExample {

    private DFSExample() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        CompactSimpleGraph<Void> graph = new CompactSimpleGraph<>();

        int n0 = graph.addIntNode(), n1 = graph.addIntNode(), n2 = graph.addIntNode(), n3 = graph.addIntNode(), n4 =
                graph.addIntNode();

        graph.connect(n0, n1);
        graph.connect(n0, n2);
        graph.connect(n1, n1);
        graph.connect(n1, n2);
        graph.connect(n2, n3);
        graph.connect(n3, n1);
        graph.connect(n3, n0);
        graph.connect(n0, n4);
        graph.connect(n4, n3);

        MyDFSVisitor<Integer, CompactEdge<Void>> vis = new MyDFSVisitor<>(graph);
        GraphTraversal.depthFirst(graph, n0, vis);
        DFSResultDOTHelper<Integer, CompactEdge<Void>> helper = new DFSResultDOTHelper<>(vis);

        Visualization.visualize(graph, helper);
    }

    enum EdgeType {
        TREE(EdgeStyles.BOLD),
        FORWARD(EdgeStyles.DOTTED),
        BACK(EdgeStyles.SOLID),
        CROSS(EdgeStyles.DASHED);

        private final String style;

        EdgeType(String style) {
            this.style = style;
        }

        String getStyle() {
            return style;
        }
    }

    static class MyDFSVisitor<N, E> implements GraphTraversalVisitor<N, E, DFSData> {

        private final MutableMapping<N, @Nullable DFSData> records;
        private final Map<E, EdgeType> edgeTypes;
        private int dfsNum;

        MyDFSVisitor(IndefiniteGraph<N, E> graph) {
            this.records = graph.createStaticNodeMapping();
            this.edgeTypes = new HashMap<>();
        }

        @Override
        public GraphTraversalAction processInitial(N initialNode, Holder<DFSData> holder) {
            final DFSData rec = new DFSData(dfsNum++);
            records.put(initialNode, rec);
            holder.value = rec;
            return GraphTraversalAction.EXPLORE;
        }

        @Override
        public void finishExploration(N node, DFSData data) {
            data.finished = true;
        }

        @Override
        public GraphTraversalAction processEdge(N srcNode,
                                                DFSData srcData,
                                                E edge,
                                                N tgtNode,
                                                Holder<DFSData> tgtHolder) {
            DFSData tgtRec = records.get(tgtNode);
            if (tgtRec == null) {
                edgeTypes.put(edge, EdgeType.TREE);
                tgtRec = new DFSData(dfsNum++);
                records.put(tgtNode, tgtRec);

                tgtHolder.value = tgtRec;
                return GraphTraversalAction.EXPLORE;
            }
            if (!tgtRec.finished) {
                edgeTypes.put(edge, EdgeType.BACK);
            } else if (tgtRec.dfsNumber > srcData.dfsNumber) {
                edgeTypes.put(edge, EdgeType.FORWARD);
            } else {
                edgeTypes.put(edge, EdgeType.CROSS);
            }

            return GraphTraversalAction.IGNORE;
        }

        Mapping<N, @Nullable DFSData> getRecords() {
            return records;
        }

        Map<E, EdgeType> getEdgeTypes() {
            return edgeTypes;
        }
    }

    static class DFSData {

        final int dfsNumber;
        boolean finished;

        DFSData(int dfsNumber) {
            this.dfsNumber = dfsNumber;
            this.finished = false;
        }
    }

    static class DFSResultDOTHelper<N, E> implements VisualizationHelper<N, E> {

        private final Mapping<N, @Nullable DFSData> records;
        private final Map<E, EdgeType> edgeTypes;

        DFSResultDOTHelper(MyDFSVisitor<N, E> vis) {
            this(vis.getRecords(), vis.getEdgeTypes());
        }

        DFSResultDOTHelper(Mapping<N, @Nullable DFSData> records, Map<E, EdgeType> edgeTypes) {
            this.records = records;
            this.edgeTypes = edgeTypes;
        }

        @Override
        public boolean getNodeProperties(N node, Map<String, String> properties) {
            String lbl = properties.get(NodeAttrs.LABEL);
            DFSData record = records.get(node);
            assert record != null;
            properties.put(NodeAttrs.LABEL, lbl + " [#" + record.dfsNumber + "]");
            return true;
        }

        @Override
        public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
            EdgeType et = edgeTypes.get(edge);
            assert et != null;
            properties.put(EdgeAttrs.STYLE, et.getStyle());
            properties.remove(EdgeAttrs.LABEL);
            return true;
        }
    }

}
