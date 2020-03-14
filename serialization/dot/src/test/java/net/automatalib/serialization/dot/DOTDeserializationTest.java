/* Copyright (C) 2013-2020 TU Dortmund
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.commons.util.io.UnclosableInputStream;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.serialization.FormatException;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.fsa.NFAs;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class DOTDeserializationTest {

    @Test
    public void testRegularDFADeserialization() throws IOException {

        final CompactDFA<String> dfa = DOTSerializationUtil.DFA;

        final DFA<?, String> parsed =
                DOTParsers.dfa().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.DFA_RESOURCE)).
                        model;

        Assert.assertTrue(Automata.testEquivalence(dfa, parsed, dfa.getInputAlphabet()));
    }

    @Test
    public void testRegularNFADeserialization() throws IOException {

        final CompactNFA<String> nfa = DOTSerializationUtil.NFA;

        final CompactNFA<String> parsed =
                DOTParsers.nfa().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.NFA_RESOURCE)).
                        model;

        Assert.assertTrue(Automata.testEquivalence(NFAs.determinize(nfa),
                                                   NFAs.determinize(parsed),
                                                   nfa.getInputAlphabet()));
    }

    @Test
    public void testRegularNFA2Deserialization() throws IOException {

        final CompactNFA<String> parsed = DOTParsers.fsa(new CompactNFA.Creator<>(),
                                                         DOTParsers.DEFAULT_FSA_NODE_PARSER,
                                                         DOTParsers.DEFAULT_EDGE_PARSER,
                                                         Arrays.asList("s0", "s1", "s2"),
                                                         false)
                                                    .readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.NFA2_RESOURCE)).
                model;

        Assert.assertEquals(3, parsed.size());
        Assert.assertEquals(3, parsed.getInitialStates().size());
        Assert.assertFalse(parsed.accepts(Word.fromSymbols("a", "a", "a")));
        Assert.assertFalse(parsed.accepts(Word.fromSymbols("b", "b")));
        Assert.assertFalse(parsed.accepts(Word.fromSymbols("c")));
        Assert.assertEquals(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("a", "a", "a")).size(), 1);
        Assert.assertEquals(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("b", "b")).size(), 1);
        Assert.assertEquals(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("c")).size(), 1);
        Assert.assertTrue(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("a", "b")).isEmpty());
        Assert.assertTrue(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("c", "a")).isEmpty());
        Assert.assertTrue(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("b", "c")).isEmpty());
    }

    @Test
    public void testRegularMealyDeserialization() throws IOException {

        final CompactMealy<String, String> mealy = DOTSerializationUtil.MEALY;

        final MealyMachine<?, String, ?, String> parsed =
                DOTParsers.mealy().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.MEALY_RESOURCE)).
                        model;

        Assert.assertTrue(Automata.testEquivalence(mealy, parsed, mealy.getInputAlphabet()));
    }

    @Test
    public void testRegularMooreDeserialization() throws IOException {
        final CompactMoore<String, String> moore = DOTSerializationUtil.MOORE;

        final MooreMachine<?, String, ?, String> parsed =
                DOTParsers.moore().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.MOORE_RESOURCE)).
                        model;

        Assert.assertTrue(Automata.testEquivalence(moore, parsed, moore.getInputAlphabet()));
    }

    @Test
    public void testRegularGraphDeserialization() throws IOException {
        final UniversalGraph<?, ?, String, String> graph = DOTSerializationUtil.GRAPH;

        final UniversalGraph<?, ?, String, String> parsed =
                DOTParsers.graph().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.GRAPH_RESOURCE));

        checkGraphEquivalence(graph, parsed);
    }

    @Test(expectedExceptions = FormatException.class)
    public void testFaultyAutomatonDeserialization() throws IOException {
        DOTParsers.dfa().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.FAULTY_AUTOMATON_RESOURCE));
    }

    @Test(expectedExceptions = FormatException.class)
    public void testFaultyGraphDeserialization() throws IOException {
        DOTParsers.graph().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.FAULTY_GRAPH_RESOURCE));
    }

    @Test
    public void doNotCloseInputStreamTest() throws IOException {
        try (InputStream dfa = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.DFA_RESOURCE);
             InputStream nfa = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.NFA_RESOURCE);
             InputStream graph = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.GRAPH_RESOURCE);
             InputStream mealy = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.MEALY_RESOURCE);
             InputStream moore = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.MOORE_RESOURCE)) {
            DOTParsers.dfa().readModel(new UnclosableInputStream(dfa));
            DOTParsers.nfa().readModel(new UnclosableInputStream(nfa));
            DOTParsers.graph().readModel(new UnclosableInputStream(graph));
            DOTParsers.mealy().readModel(new UnclosableInputStream(mealy));
            DOTParsers.moore().readModel(new UnclosableInputStream(moore));
        }
    }

    private static <N1, E1, NP extends Comparable<NP>, EP extends Comparable<EP>, N2, E2> void checkGraphEquivalence(
            UniversalGraph<N1, E1, NP, EP> source,
            UniversalGraph<N2, E2, NP, EP> target) {

        Assert.assertEquals(source.size(), target.size());

        final Queue<N1> sourceQueue = new ArrayDeque<>();
        final Queue<N2> targetQueue = new ArrayDeque<>();

        // since the node ids are parsed natural order, the first nodes are equal
        sourceQueue.add(source.nodeIDs().getNode(0));
        targetQueue.add(target.nodeIDs().getNode(0));

        while (!sourceQueue.isEmpty() && !targetQueue.isEmpty()) {
            N1 sourceNode = sourceQueue.remove();
            N2 targetNode = targetQueue.remove();

            Assert.assertEquals(source.getNodeProperty(sourceNode), target.getNodeProperty(targetNode));

            final List<E1> sourceEdges = new ArrayList<>(source.getOutgoingEdges(sourceNode));
            final List<E2> targetEdges = new ArrayList<>(target.getOutgoingEdges(targetNode));

            Assert.assertEquals(sourceEdges.size(), targetEdges.size());

            // since we have unique node properties, these uniquely identify states
            sourceEdges.sort(Comparator.comparing(e -> source.getNodeProperty(source.getTarget(e))));
            targetEdges.sort(Comparator.comparing(e -> target.getNodeProperty(target.getTarget(e))));

            for (int j = 0; j < sourceEdges.size(); j++) {
                final E1 sourceEdge = sourceEdges.get(j);
                final E2 targetEdge = targetEdges.get(j);

                Assert.assertEquals(source.getEdgeProperty(sourceEdge), target.getEdgeProperty(targetEdge));
                Assert.assertEquals(source.getNodeProperty(source.getTarget(sourceEdge)),
                                    target.getNodeProperty(target.getTarget(targetEdge)));
            }
        }

        Assert.assertEquals(sourceQueue.isEmpty(), targetQueue.isEmpty());
    }
}
