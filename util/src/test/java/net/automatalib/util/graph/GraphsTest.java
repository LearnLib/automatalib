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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.common.util.mapping.Mappings;
import net.automatalib.graph.CompactBidiGraph;
import net.automatalib.graph.CompactGraph;
import net.automatalib.graph.CompactSimpleBidiGraph;
import net.automatalib.graph.CompactSimpleGraph;
import net.automatalib.graph.MutableGraph;
import net.automatalib.graph.SimpleMapGraph;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.util.graph.copy.GraphCopy;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GraphsTest {

    @Test
    public void testIncomingEdges() {

        final Alphabet<Integer> alphabet = Alphabets.integers(1, 3);
        final DFA<?, Integer> dfa = RandomAutomata.randomDFA(new Random(42), 10, alphabet);

        checkIncomingEdges(dfa, alphabet, new CompactGraph<>(dfa.size()));
        checkIncomingEdges(dfa, alphabet, new CompactBidiGraph<>(dfa.size()));
        checkIncomingEdges(dfa, alphabet, new CompactSimpleGraph<>(dfa.size()));
        checkIncomingEdges(dfa, alphabet, new CompactSimpleBidiGraph<>(dfa.size()));
        checkIncomingEdges(dfa, alphabet, new SimpleMapGraph<>());
    }

    private <S, I, N, E, NP, EP> void checkIncomingEdges(DFA<S, I> dfa,
                                                         Alphabet<I> alphabet,
                                                         MutableGraph<N, E, NP, EP> graph) {

        final Mapping<S, N> nodeMapping = GraphCopy.copyUniversalPlain(dfa.transitionGraphView(alphabet),
                                                                       graph,
                                                                       Mappings.nullMapping(),
                                                                       Mappings.nullMapping());

        final Mapping<N, Collection<E>> incomingEdges = Graphs.incomingEdges(graph);

        for (S tgt : dfa) {
            final Set<S> incomingStates = new HashSet<>();

            for (S src : dfa) {
                for (I i : alphabet) {
                    if (Objects.equals(tgt, dfa.getSuccessor(src, i))) {
                        incomingStates.add(src);
                    }
                }
            }

            final N mappedTgt = nodeMapping.get(tgt);
            final Set<E> edges = new HashSet<>(incomingEdges.get(mappedTgt));
            final Set<E> checkEdges = Sets.newHashSetWithExpectedSize(edges.size());

            for (S src : incomingStates) {
                final N mappedSrc = nodeMapping.get(src);
                graph.getEdgesBetween(mappedSrc, mappedTgt).forEachRemaining(checkEdges::add);
            }

            Assert.assertEquals(checkEdges, edges);
        }
    }
}
