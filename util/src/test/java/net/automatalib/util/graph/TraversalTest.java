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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
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
import net.automatalib.util.graph.traversal.GraphTraversal;
import net.automatalib.util.graph.traversal.GraphTraversalAction;
import net.automatalib.util.graph.traversal.GraphTraversalVisitor;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TraversalTest {

    private final CompactDFA<Integer> automaton;
    private final UniversalGraph<Integer, TransitionEdge<Integer, Integer>, ?, ?> graph;

    private final CompactSimpleGraph<Character> tree;
    private final CompactSimpleGraph<Character> circular;

    public TraversalTest() {
        final Random random = new Random(0);
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 4);

        automaton = RandomAutomata.randomDFA(random, 20, alphabet);
        graph = automaton.transitionGraphView();

        tree = new CompactSimpleGraph<>();
        final Integer init = tree.addNode();
        final Integer last1 = addTreeTrace(tree, init, '1', '2', '3', '4', '5');
        final Integer last2 = addTreeTrace(tree, init, 'a', 'b', 'c', 'd', 'e');

        tree.connect(last1, init);
        tree.connect(last2, init);

        this.circular = new CompactSimpleGraph<>();
        Integer s0 = circular.addNode();
        Integer s1 = circular.addNode();
        Integer s2 = circular.addNode();
        Integer s3 = circular.addNode();
        Integer s4 = circular.addNode();

        circular.connect(s0, s1, (Character) 'a');
        circular.connect(s1, s2, (Character) 'a');
        circular.connect(s1, s4, (Character) 'b');
        circular.connect(s2, s3, (Character) 'a');
        circular.connect(s3, s4, (Character) 'a');
        circular.connect(s4, s0, (Character) 'a');
        circular.connect(s0, s4, (Character) 'c');
        circular.connect(s4, s3, (Character) 'c');
        circular.connect(s4, s1, (Character) 'b');
        circular.connect(s3, s2, (Character) 'c');
        circular.connect(s2, s1, (Character) 'c');
        circular.connect(s1, s0, (Character) 'c');
    }

    @SafeVarargs
    private static <N, EP> N addTreeTrace(MutableGraph<N, ?, ?, EP> tree, N init, EP... traceElements) {
        N iter = init;

        for (EP ep : traceElements) {
            final N next = tree.addNode();
            tree.connect(iter, next, ep);
            iter = next;
        }

        return iter;
    }

    @Test
    public void testSingleToSingleShortestPath() {
        for (Integer s : automaton.getStates()) {
            final Path<Integer, TransitionEdge<Integer, Integer>> path =
                    ShortestPaths.shortestPath(graph, automaton.getInitialState(), automaton.size() - 1, s);

            Assert.assertNotNull(path);

            final List<Integer> nodes = path.getNodes();
            final Iterator<Integer> nodeIter = nodes.iterator();

            Assert.assertTrue(nodeIter.hasNext());

            Integer sIter = automaton.getInitialState();
            Integer nIter = nodeIter.next();

            Assert.assertEquals(sIter, nIter);

            for (TransitionEdge<Integer, Integer> e : path) {
                Assert.assertTrue(nodeIter.hasNext());

                final Integer i = e.getInput();
                sIter = automaton.getSuccessor(sIter, i);
                nIter = nodeIter.next();

                Assert.assertEquals(nIter, sIter);
            }
        }
    }

    @Test
    public void testMultiToMultiShortestPaths() {
        final Iterable<Path<Integer, TransitionEdge<Integer, Integer>>> paths =
                ShortestPaths.shortestPaths(graph, automaton.getStates(), 0, automaton.getStates());

        final List<Word<Integer>> accessSequences = collectPathInputs(paths);

        Assert.assertEquals(automaton.size(), accessSequences.size());
        accessSequences.forEach(as -> Assert.assertEquals(Word.epsilon(), as));
    }

    @Test
    public void testMultiToSingleShortestPaths() {
        final Integer target = new Random(0).nextInt(automaton.size());
        final Iterable<Path<Integer, TransitionEdge<Integer, Integer>>> paths =
                ShortestPaths.shortestPaths(graph, automaton.getStates(), 0, target);

        final List<Word<Integer>> accessSequences = collectPathInputs(paths);

        Assert.assertEquals(1, accessSequences.size());
        accessSequences.forEach(as -> Assert.assertEquals(Word.epsilon(), as));
    }

    @Test
    public void testBreadthFirstTraversal() {
        final Iterable<Integer> iter = GraphTraversal.breadthFirstOrder(tree, Collections.singleton(tree.getNode(0)));
        Assert.assertEquals(iter, Arrays.asList(0, 1, 6, 2, 7, 3, 8, 4, 9, 5, 10));
    }

    @Test
    public void testBreadthFirstDefault() {
        final DefaultVisitor<Integer, CompactEdge<Character>, Void> visitor = new DefaultVisitor<>(0);
        GraphTraversal.breadthFirst(circular, circular.getNodes(), visitor);

        Assert.assertEquals(visitor.getNodes(), Arrays.asList(0, 1, 4, 2, 3));
    }

    @Test
    public void testBreadthFirstInitialAbort() {
        final InitialAbortVisitor<Integer, CompactEdge<Character>, Void> abortVisitor = new InitialAbortVisitor<>();
        GraphTraversal.breadthFirst(circular, 0, abortVisitor);

        Assert.assertEquals(abortVisitor.getNodes(), Collections.emptyList());

        final DefaultVisitor<Integer, CompactEdge<Character>, Void> defaultVisitor = new DefaultVisitor<>(0);
        final boolean limit = GraphTraversal.breadthFirst(circular, 0, circular.getNodes(), defaultVisitor);

        Assert.assertFalse(limit);
        Assert.assertEquals(defaultVisitor.getNodes(), Collections.emptyList());
    }

    @Test
    public void testBreadthFirstAbortState() {
        final AbortStateVisitor<Integer, CompactEdge<Character>, Void> visitor =
                new AbortStateVisitor<>(circular.getNode(0), circular.getNode(1));
        GraphTraversal.breadthFirst(circular, circular.getNodes(), visitor);

        Assert.assertEquals(visitor.getNodes(), Arrays.asList(0, 1, 4, 3, 2));
    }

    @Test
    public void testDepthFirstTraversal() {
        final Iterable<Integer> iter = GraphTraversal.depthFirstOrder(tree, Collections.singleton(tree.getNode(0)));
        Assert.assertEquals(iter, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void testDepthFirstDefault() {
        final DefaultVisitor<Integer, CompactEdge<Character>, Void> visitor = new DefaultVisitor<>(0);
        GraphTraversal.depthFirst(circular, circular.getNodes(), visitor);

        Assert.assertEquals(visitor.getNodes(), Arrays.asList(0, 1, 2, 3, 4));
    }

    @Test
    public void testDepthFirstInitialAbort() {
        final InitialAbortVisitor<Integer, CompactEdge<Character>, Void> abortVisitor = new InitialAbortVisitor<>();
        GraphTraversal.depthFirst(circular, 0, abortVisitor);

        Assert.assertEquals(abortVisitor.getNodes(), Collections.emptyList());

        final DefaultVisitor<Integer, CompactEdge<Character>, Void> defaultVisitor = new DefaultVisitor<>(0);
        final boolean limit = GraphTraversal.depthFirst(circular, 0, circular.getNodes(), defaultVisitor);

        Assert.assertFalse(limit);
        Assert.assertEquals(defaultVisitor.getNodes(), Collections.emptyList());
    }

    @Test
    public void testDepthFirstAbortState() {
        final AbortStateVisitor<Integer, CompactEdge<Character>, Void> visitor =
                new AbortStateVisitor<>(circular.getNode(0), circular.getNode(1));
        GraphTraversal.depthFirst(circular, circular.getNodes(), visitor);

        Assert.assertEquals(visitor.getNodes(), Arrays.asList(0, 1, 4, 3, 2));
    }

    private <S, I, T> List<Word<I>> collectPathInputs(Iterable<Path<S, TransitionEdge<I, T>>> paths) {
        return Streams.stream(paths)
                      .map(p -> p.stream().map(TransitionEdge::getInput).collect(Word.collector()))
                      .collect(Collectors.toList());
    }

    private abstract static class AbstractVisitor<N, E, D> implements GraphTraversalVisitor<N, E, D> {

        private final Set<N> nodes;

        AbstractVisitor() {
            this.nodes = new LinkedHashSet<>();
        }

        @Override
        public boolean startExploration(N node, D data) {
            nodes.add(node);
            return true;
        }

        @Override
        public GraphTraversalAction processEdge(N srcNode, D srcData, E edge, N tgtNode, Holder<D> tgtHolder) {
            return nodes.contains(tgtNode) ? GraphTraversalAction.IGNORE : GraphTraversalAction.EXPLORE;
        }

        public Collection<N> getNodes() {
            return nodes;
        }
    }

    private static class InitialAbortVisitor<N, E, D> extends AbstractVisitor<N, E, D> {

        @Override
        public GraphTraversalAction processInitial(N initialNode, Holder<D> holder) {
            return GraphTraversalAction.ABORT_TRAVERSAL;
        }
    }

    private static class DefaultVisitor<N, E, D> extends AbstractVisitor<N, E, D> {

        private final N initial;

        DefaultVisitor(N initial) {
            this.initial = initial;
        }

        @Override
        public GraphTraversalAction processInitial(N initialNode, Holder<D> holder) {
            return Objects.equals(initialNode, initial) ? GraphTraversalAction.EXPLORE : GraphTraversalAction.IGNORE;
        }
    }

    private static class AbortStateVisitor<N, E, D> extends DefaultVisitor<N, E, D> {

        private final N ignored;

        AbortStateVisitor(N initial, N ignored) {
            super(initial);
            this.ignored = ignored;
        }

        @Override
        public GraphTraversalAction processEdge(N srcNode, D srcData, E edge, N tgtNode, Holder<D> tgtHolder) {
            if (Objects.equals(srcNode, ignored)) {
                return GraphTraversalAction.ABORT_NODE;
            }

            return super.processEdge(srcNode, srcData, edge, tgtNode, tgtHolder);
        }
    }
}
