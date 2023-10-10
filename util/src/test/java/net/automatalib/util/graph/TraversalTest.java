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
package net.automatalib.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.compact.CompactDFA;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.common.util.Holder;
import net.automatalib.graph.MutableGraph;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.graph.base.compact.CompactEdge;
import net.automatalib.graph.base.compact.CompactSimpleGraph;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.util.graph.traversal.DefaultGraphTraversalVisitor;
import net.automatalib.util.graph.traversal.GraphTraversal;
import net.automatalib.util.graph.traversal.GraphTraversalAction;
import net.automatalib.util.traversal.TraversalOrder;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TraversalTest {

    private int size;
    private CompactDFA<Integer> automaton;
    private UniversalGraph<Integer, TransitionEdge<Integer, Integer>, ?, ?> graph;

    private CompactSimpleGraph<Character> tree;

    @BeforeClass
    public void setUp() {
        final Random random = new Random(0);
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 4);

        size = 20;
        automaton = RandomAutomata.randomDFA(random, size, alphabet);
        graph = automaton.transitionGraphView();

        tree = new CompactSimpleGraph<>();
        final Integer init = tree.addNode();
        addTreeTrace(tree, init, '1', '2', '3', '4', '5');
        addTreeTrace(tree, init, 'a', 'b', 'c', 'd', 'e');
    }

    @SafeVarargs
    private static <N, EP> void addTreeTrace(MutableGraph<N, ?, ?, EP> tree, N init, EP... traceElements) {
        N iter = init;

        for (EP ep : traceElements) {
            final N next = tree.addNode();
            tree.connect(iter, next, ep);
            iter = next;
        }
    }

    @Test
    public void testSingleToSingleShortestPath() {
        for (Integer s : automaton.getStates()) {
            final Path<Integer, TransitionEdge<Integer, Integer>> path =
                    ShortestPaths.shortestPath(graph, automaton.getInitialState(), size - 1, s);

            Assert.assertNotNull(path);

            final WordBuilder<Integer> asBuilder = new WordBuilder<>(path.size());
            path.edgeList().forEach(el -> asBuilder.append(el.getInput()));

            Assert.assertEquals(s, automaton.getState(asBuilder.toWord()));
        }
    }

    @Test
    public void testMultiToMultiShortestPaths() {
        final Iterable<Path<Integer, TransitionEdge<Integer, Integer>>> paths =
                ShortestPaths.shortestPaths(graph, automaton.getStates(), 0, automaton.getStates());

        final List<Word<Integer>> accessSequences = collectPathInputs(paths);

        Assert.assertEquals(size, accessSequences.size());
        accessSequences.forEach(as -> Assert.assertEquals(Word.epsilon(), as));
    }

    @Test
    public void testMultiToSingleShortestPaths() {
        final Integer target = new Random(0).nextInt(size);
        final Iterable<Path<Integer, TransitionEdge<Integer, Integer>>> paths =
                ShortestPaths.shortestPaths(graph, automaton.getStates(), 0, target);

        final List<Word<Integer>> accessSequences = collectPathInputs(paths);

        Assert.assertEquals(1, accessSequences.size());
        accessSequences.forEach(as -> Assert.assertEquals(Word.epsilon(), as));
    }

    @Test
    public void testDFTraversal() {
        checkVisitedNodesOrder(TraversalOrder.DEPTH_FIRST,
                               Arrays.asList('1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e'));
    }

    @Test
    public void testBFTraversal() {
        checkVisitedNodesOrder(TraversalOrder.BREADTH_FIRST,
                               Arrays.asList('1', 'a', '2', 'b', '3', 'c', '4', 'd', '5', 'e'));
    }

    private void checkVisitedNodesOrder(TraversalOrder order, List<Character> expectedOrder) {
        final List<Character> visitedNodes = new ArrayList<>();

        GraphTraversal.traverse(order,
                                tree,
                                tree.getNode(0),
                                new DefaultGraphTraversalVisitor<Integer, CompactEdge<Character>, Object>() {

                                    @Override
                                    public GraphTraversalAction processEdge(Integer srcNode,
                                                                            Object srcData,
                                                                            CompactEdge<Character> edge,
                                                                            Integer tgtNode,
                                                                            Holder<Object> outData) {
                                        visitedNodes.add(edge.getProperty());
                                        return super.processEdge(srcNode, srcData, edge, tgtNode, outData);
                                    }
                                });

        Assert.assertEquals(expectedOrder, visitedNodes);
    }

    private <S, I, T> List<Word<I>> collectPathInputs(Iterable<Path<S, TransitionEdge<I, T>>> paths) {
        return Streams.stream(paths)
                      .map(Path::edgeList)
                      .map(el -> el.stream().map(TransitionEdge::getInput).collect(Word.collector()))
                      .collect(Collectors.toList());
    }

}
