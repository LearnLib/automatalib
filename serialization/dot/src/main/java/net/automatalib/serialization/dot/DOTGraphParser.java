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
package net.automatalib.serialization.dot;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.IOUtil;
import net.automatalib.exception.FormatException;
import net.automatalib.graph.Graph;
import net.automatalib.graph.MutableGraph;
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
 */
public class DOTGraphParser<NP, EP, G extends MutableGraph<?, ?, NP, EP>> implements ModelDeserializer<G> {

    private final Supplier<G> creator;
    private final Function<Map<String, String>, NP> nodeParser;
    private final Function<Map<String, String>, EP> edgeParser;

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
                          Function<Map<String, String>, NP> nodeParser,
                          Function<Map<String, String>, EP> edgeParser) {
        this.creator = creator;
        this.nodeParser = nodeParser;
        this.edgeParser = edgeParser;
    }

    @Override
    public G readModel(InputStream is) throws IOException, FormatException {

        try (Reader r = IOUtil.asNonClosingUTF8Reader(is)) {
            InternalDOTParser parser = new InternalDOTParser(r);
            parser.parse();

            final G graph = creator.get();

            parseNodesAndEdges(parser, (MutableGraph<?, ?, NP, EP>) graph);

            return graph;
        }
    }

    private <N> void parseNodesAndEdges(InternalDOTParser parser, MutableGraph<N, ?, NP, EP> graph) {
        final Collection<Node> nodes = parser.getNodes();
        final Collection<Edge> edges = parser.getEdges();

        final Map<String, N> stateMap = new HashMap<>(HashUtil.capacity(nodes.size()));

        for (Node node : nodes) {
            final N n = graph.addNode(nodeParser.apply(node.attributes));
            stateMap.put(node.id, n);
        }

        for (Edge edge : edges) {
            graph.connect(stateMap.get(edge.src), stateMap.get(edge.tgt), edgeParser.apply(edge.attributes));
        }
    }
}
