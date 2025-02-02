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
package net.automatalib.util.graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.FastDFAState;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.graph.TransitionEdge.Property;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.graph.Graph;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.graph.impl.CompactUniversalGraph;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.util.graph.copy.GraphCopy;
import net.automatalib.util.traversal.TraversalOrder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GraphCopyTest {

    private FastDFA<Character> source;
    private UniversalGraph<FastDFAState, TransitionEdge<Character, FastDFAState>, Boolean, TransitionEdge.Property<Character, Void>>
            sourceAsGraph;

    @BeforeClass
    public void setUp() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final Random random = new Random(42);
        source = new FastDFA<>(alphabet);

        RandomAutomata.randomDeterministic(random,
                                           20,
                                           alphabet,
                                           Arrays.asList(Boolean.FALSE, Boolean.TRUE),
                                           Collections.emptyList(),
                                           source);

        sourceAsGraph = this.source.transitionGraphView();
    }

    @Test
    public void testPlainCopy() {
        final CompactUniversalGraph<Boolean, Property<Character, Void>> target = new CompactUniversalGraph<>();
        final Mapping<FastDFAState, Integer> mapping = GraphCopy.copyPlain(sourceAsGraph, target);
        checkEquality(sourceAsGraph, target, mapping);
    }

    @Test
    public void testTraversalCopy() {
        final CompactUniversalGraph<Boolean, Property<Character, Void>> target = new CompactUniversalGraph<>();
        final Mapping<FastDFAState, Integer> mapping = GraphCopy.copyTraversal(source.transitionGraphView(),
                                                                               target,
                                                                               TraversalOrder.BREADTH_FIRST,
                                                                               -1,
                                                                               source.getInitialStates());
        checkEquality(sourceAsGraph, target, mapping);
    }

    private static <N1, N2, E1, E2> void checkEquality(Graph<N1, E1> source,
                                                       Graph<N2, E2> target,
                                                       Mapping<N1, N2> mapping) {

        final Set<N2> mappedSourceNodes = source.getNodes().stream().map(mapping::get).collect(Collectors.toSet());
        final Set<N2> targetNodes = new HashSet<>(target.getNodes());

        Assert.assertEquals(targetNodes, mappedSourceNodes);

        for (N1 node : source.getNodes()) {
            final N2 mappedNode = mapping.get(node);

            final Set<N2> sourceSuccsMapped =
                    source.getAdjacentNodes(node).stream().map(mapping::get).collect(Collectors.toSet());
            final Set<N2> targetSuccs = new HashSet<>(target.getAdjacentNodes(mappedNode));

            Assert.assertEquals(targetSuccs, sourceSuccsMapped);

        }

    }
}
