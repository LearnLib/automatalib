/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.serialization.dot;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Maps;
import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.paypal.digraph.parser.GraphParserException;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.MutableGraph;
import net.automatalib.serialization.FormatException;
import net.automatalib.serialization.ModelDeserializer;

/**
 * General-purpose DOT parser for {@link MutableGraph}s.
 *
 * @param <NP>
 *         the node property type
 * @param <EP>
 *         the edge property type
 * @param <G>
 *         the graph type
 *
 * @author frohme
 */
public class DOTGraphParser<NP, EP, G extends MutableGraph<?, ?, NP, EP>> implements ModelDeserializer<G> {

    private final Supplier<G> creator;
    private final Function<Map<String, Object>, NP> nodeParser;
    private final Function<Map<String, Object>, EP> edgeParser;

    /**
     * Parser for (directed) {@link Graph}s with a custom graph instance and custom node and edge attributes.
     *
     * @param creator
     *         a creator that is used to instantiate the returned graph
     * @param nodeParser
     *         a node parser that extracts from a property map of a node the node property
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the edge property
     */
    public DOTGraphParser(Supplier<G> creator,
                          Function<Map<String, Object>, NP> nodeParser,
                          Function<Map<String, Object>, EP> edgeParser) {
        this.creator = creator;
        this.nodeParser = nodeParser;
        this.edgeParser = edgeParser;
    }

    @Override
    public G readModel(InputStream is) {

        final GraphParser gp;

        try {
            gp = new GraphParser(is);
        } catch (GraphParserException gpe) {
            throw new FormatException(gpe);
        }

        final G graph = creator.get();

        parseNodesAndEdges(gp, (MutableGraph<?, ?, NP, EP>) graph);

        return graph;
    }

    private <N> void parseNodesAndEdges(GraphParser gp, MutableGraph<N, ?, NP, EP> graph) {
        final Map<String, N> stateMap = Maps.newHashMapWithExpectedSize(gp.getNodes().size());

        for (GraphNode node : gp.getNodes().values()) {
            final N n = graph.addNode(nodeParser.apply(node.getAttributes()));
            stateMap.put(node.getId(), n);
        }

        for (GraphEdge edge : gp.getEdges().values()) {
            graph.connect(stateMap.get(edge.getNode1().getId()),
                          stateMap.get(edge.getNode2().getId()),
                          edgeParser.apply(edge.getAttributes()));
        }
    }
}
