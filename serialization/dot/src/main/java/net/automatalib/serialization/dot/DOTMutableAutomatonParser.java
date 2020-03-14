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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.paypal.digraph.parser.GraphParserException;
import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * General-purpose DOT parser for {@link MutableAutomaton}s.
 *
 * @param <I>
 *         input symbol type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 * @param <A>
 *         automaton type
 *
 * @author frohme
 */
public class DOTMutableAutomatonParser<I, SP, TP, A extends MutableAutomaton<?, I, ?, SP, TP>>
        implements InputModelDeserializer<I, A> {

    private final AutomatonCreator<A, I> creator;
    private final Function<Map<String, Object>, SP> nodeParser;
    private final Function<Map<String, Object>, Pair<I, TP>> edgeParser;
    private final Collection<String> initialNodeIds;
    private final boolean fakeInitialNodeIds;

    /**
     * Parser for arbitrary {@link MutableAutomaton}s with a custom automaton instance, custom node and edge attributes
     * and custom labels for the initial nodes.
     *
     * @param creator
     *         a creator that is used to instantiate the returned automaton
     * @param nodeParser
     *         a node parser that extracts from a property map of a node the state property
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol and transition property
     * @param initialNodeIds
     *         the ids of the initial nodes
     * @param fakeInitialNodeIds
     *         a flag indicating whether or not the {@code initialNodeIds} are artificial or not. If {@code true}, the
     *         nodes matching the {@code initialNodeIds} will not be added to the automaton. Instead, their direct
     *         successors will be initial states instead. This may be useful for instances where there are artificial
     *         nodes used to display in incoming arrow for the actual initial states. If {@code false}, the nodes
     *         matching the {@code initialNodeIds} will be used as initial nodes.
     */
    public DOTMutableAutomatonParser(AutomatonCreator<A, I> creator,
                                     Function<Map<String, Object>, SP> nodeParser,
                                     Function<Map<String, Object>, Pair<I, TP>> edgeParser,
                                     Collection<String> initialNodeIds,
                                     boolean fakeInitialNodeIds) {
        this.creator = creator;
        this.nodeParser = nodeParser;
        this.edgeParser = edgeParser;
        this.initialNodeIds = initialNodeIds;
        this.fakeInitialNodeIds = fakeInitialNodeIds;
    }

    @Override
    public InputModelData<I, A> readModel(InputStream is) throws IOException {

        final GraphParser gp;

        try {
            gp = new GraphParser(IOUtil.asUncompressedBufferedNonClosingInputStream(is));
        } catch (GraphParserException gpe) {
            throw new FormatException(gpe);
        }

        final Set<I> inputs = new HashSet<>();

        for (GraphEdge edge : gp.getEdges().values()) {
            if (!fakeInitialNodeIds || !initialNodeIds.contains(edge.getNode1().getId())) {
                inputs.add(edgeParser.apply(edge.getAttributes()).getFirst());
            }
        }

        final Alphabet<I> alphabet = Alphabets.fromCollection(inputs);
        final A automaton = creator.createAutomaton(alphabet, gp.getNodes().size());

        parseNodesAndEdges(gp, (MutableAutomaton<?, I, ?, SP, TP>) automaton);

        return new InputModelData<>(automaton, alphabet);
    }

    private <S> void parseNodesAndEdges(GraphParser gp, MutableAutomaton<S, I, ?, SP, TP> automaton) {
        final Map<String, S> stateMap = Maps.newHashMapWithExpectedSize(gp.getNodes().size());

        for (GraphNode node : gp.getNodes().values()) {
            final S state;

            if (fakeInitialNodeIds && initialNodeIds.contains(node.getId())) {
                continue;
            } else if (!fakeInitialNodeIds && initialNodeIds.contains(node.getId())) {
                state = automaton.addInitialState(nodeParser.apply(node.getAttributes()));
            } else {
                state = automaton.addState(nodeParser.apply(node.getAttributes()));
            }

            stateMap.put(node.getId(), state);
        }

        for (GraphEdge edge : gp.getEdges().values()) {
            if (fakeInitialNodeIds && initialNodeIds.contains(edge.getNode1().getId())) {
                automaton.setInitial(stateMap.get(edge.getNode2().getId()), true);
            } else {
                final Pair<I, TP> property = edgeParser.apply(edge.getAttributes());
                automaton.addTransition(stateMap.get(edge.getNode1().getId()),
                                        property.getFirst(),
                                        stateMap.get(edge.getNode2().getId()),
                                        property.getSecond());
            }
        }
    }
}
