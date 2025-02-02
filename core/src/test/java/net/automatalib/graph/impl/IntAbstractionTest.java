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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.graph.BidirectionalGraph;
import net.automatalib.graph.MutableGraph;
import net.automatalib.graph.concept.NodeIDs;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntAbstractionTest {

    private static final int SIZE = 25;
    private static final List<Character> NPS = CollectionUtil.charRange('A', 'F');
    private static final List<Character> EPS = CollectionUtil.charRange('0', '9');

    @Test
    public void testCompactUniversalGraphs() {

        final CompactUniversalGraph<Character, Character> graph = new CompactUniversalGraph<>();
        final CompactUniversalGraph<Character, Character> abstraction = new CompactUniversalGraph<>();

        fillGraphs(new Random(42), graph, abstraction, SIZE, NPS, EPS);
        checkGraphs(graph, abstraction);
    }

    @Test
    public void testCompactSimpleGraphs() {

        final CompactSimpleGraph<Character> graph = new CompactSimpleGraph<>();
        final CompactSimpleGraph<Character> abstraction = new CompactSimpleGraph<>();

        fillGraphs(new Random(42), graph, abstraction, SIZE, Collections.singletonList(null), EPS);
        checkGraphs(graph, abstraction);
    }

    @Test
    public void testCompactUniversalBidiGraphs() {

        final CompactUniversalBidiGraph<Character, Character> graph = new CompactUniversalBidiGraph<>();
        final CompactUniversalBidiGraph<Character, Character> abstraction = new CompactUniversalBidiGraph<>();

        fillGraphs(new Random(42), graph, abstraction, SIZE, NPS, EPS);
        checkGraphs(graph, abstraction);
        checkIncomingNodes(graph, abstraction);
    }

    @Test
    public void testCompactSimpleBidiGraphs() {

        final CompactSimpleBidiGraph<Character> graph = new CompactSimpleBidiGraph<>();
        final CompactSimpleBidiGraph<Character> abstraction = new CompactSimpleBidiGraph<>();

        fillGraphs(new Random(42), graph, abstraction, SIZE, Collections.singletonList(null), EPS);
        checkGraphs(graph, abstraction);
        checkIncomingNodes(graph, abstraction);
    }

    @Test
    public void testCompactGraphs() {

        final CompactGraph graph = new CompactGraph();
        final CompactGraph abstraction = new CompactGraph();

        fillGraphs(new Random(42),
                   graph,
                   abstraction,
                   SIZE,
                   Collections.singletonList(null),
                   Collections.singletonList(null));
        checkGraphs(graph, abstraction);
    }

    private <N, E1, E2, NP, EP> void fillGraphs(Random random,
                                                MutableGraph<N, E1, NP, EP> graph,
                                                MutableGraph.IntAbstraction<E2, NP, EP> abstraction,
                                                int size,
                                                List<NP> nodeProperties,
                                                List<EP> edgeProperties) {

        for (int j = 0; j < size; j++) {
            final NP np = RandomUtil.choose(random, nodeProperties);

            graph.addNode(np);
            abstraction.addIntNode(np);
        }

        final NodeIDs<N> nodeIDs = graph.nodeIDs();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (random.nextBoolean()) { // connect
                    final EP ep = RandomUtil.choose(random, edgeProperties);

                    final E1 ge = graph.connect(nodeIDs.getNode(i), nodeIDs.getNode(j), ep);
                    final E2 ae = abstraction.connect(i, j, ep);

                    Assert.assertEquals(graph.getEdgeProperty(ge), ep);
                    Assert.assertEquals(abstraction.getEdgeProperty(ae), ep);

                    if (random.nextBoolean()) { // override
                        final EP ep2 = RandomUtil.choose(random, edgeProperties);
                        graph.setEdgeProperty(ge, ep2);
                        abstraction.setEdgeProperty(ae, ep2);
                    }
                }
            }
        }

    }

    private <N, E1, E2, NP, EP> void checkGraphs(MutableGraph<N, E1, NP, EP> graph,
                                                 MutableGraph.IntAbstraction<E2, NP, EP> abstraction) {

        Assert.assertEquals(graph.size(), abstraction.size());

        final NodeIDs<N> nodeIDs = graph.nodeIDs();
        for (N n : graph) {
            final int id = nodeIDs.getNodeId(n);

            Assert.assertEquals(graph.getNodeProperty(n), abstraction.getNodeProperty(id));

            final Collection<E1> gEdges = graph.getOutgoingEdges(n);
            final Collection<E2> aEdges = abstraction.getOutgoingEdges(id);
            Assert.assertEquals(gEdges.size(), aEdges.size());

            final Set<N> gSuccs = gEdges.stream().map(graph::getTarget).collect(Collectors.toSet());
            final Set<N> aSuccs = aEdges.stream()
                                        .mapToInt(abstraction::getIntTarget)
                                        .mapToObj(nodeIDs::getNode)
                                        .collect(Collectors.toSet());
            Assert.assertEquals(gSuccs, aSuccs);

            final Set<EP> gProps = IteratorUtil.stream(graph.getOutgoingEdgesIterator(n))
                                               .map(graph::getEdgeProperty)
                                               .collect(Collectors.toSet());
            final Set<EP> aProps = IteratorUtil.stream(abstraction.getOutgoingEdgesIterator(id))
                                               .map(abstraction::getEdgeProperty)
                                               .collect(Collectors.toSet());
            Assert.assertEquals(gProps, aProps);

            for (N n1 : graph) {
                final int id1 = nodeIDs.getNodeId(n1);
                Assert.assertEquals(graph.isConnected(n, n1), abstraction.isConnected(id, id1));

                final Collection<E1> gEdgesInBtwn = IteratorUtil.list(graph.getEdgesBetween(n, n1));
                final Collection<E2> aEdgesInBtwn = abstraction.getEdgesBetween(id, id1);
                Assert.assertEquals(gEdgesInBtwn.size(), aEdgesInBtwn.size());
            }
        }
    }

    private <N, E1, E2> void checkIncomingNodes(BidirectionalGraph<N, E1> graph,
                                                BidirectionalGraph.IntAbstraction<E2> abstraction) {

        final NodeIDs<N> nodeIDs = graph.nodeIDs();
        for (N n : graph) {
            final int id = nodeIDs.getNodeId(n);

            final Collection<E1> gEdges = graph.getIncomingEdges(n);
            final Collection<E2> aEdges = abstraction.getIncomingEdges(id);
            Assert.assertEquals(gEdges.size(), aEdges.size());

            final Set<N> gSuccs = gEdges.stream().map(graph::getSource).collect(Collectors.toSet());
            final Set<N> aSuccs = aEdges.stream()
                                        .mapToInt(abstraction::getIntSource)
                                        .mapToObj(nodeIDs::getNode)
                                        .collect(Collectors.toSet());
            Assert.assertEquals(gSuccs, aSuccs);
        }
    }

}
