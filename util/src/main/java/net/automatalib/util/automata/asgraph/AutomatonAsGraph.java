/* Copyright (C) 2013-2017 TU Dortmund
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
package net.automatalib.util.automata.asgraph;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.NodeIDs;

public class AutomatonAsGraph<S, I, T, A extends Automaton<S, I, T>> implements Graph<S, TransitionEdge<I, T>> {

    protected final A automaton;
    protected final Collection<? extends I> inputAlphabet;

    public AutomatonAsGraph(A automaton, Collection<? extends I> inputAlphabet) {
        this.automaton = automaton;
        this.inputAlphabet = inputAlphabet;
    }

    @Override
    public Collection<TransitionEdge<I, T>> getOutgoingEdges(S node) {
        return AGHelper.outgoingEdges(automaton, node, inputAlphabet);
    }

    @Override
    public S getTarget(TransitionEdge<I, T> edge) {
        return automaton.getSuccessor(edge.getTransition());
    }

    @Override
    public int size() {
        return automaton.size();
    }

    @Override
    public Collection<S> getNodes() {
        return automaton.getStates();
    }

    @Override
    public Iterator<S> iterator() {
        return automaton.iterator();
    }

    @Override
    public NodeIDs<S> nodeIDs() {
        final StateIDs<S> stateIds = automaton.stateIDs();
        return new NodeIDs<S>() {

            @Override
            public int getNodeId(S node) {
                return stateIds.getStateId(node);
            }

            @Override
            public S getNode(int id) {
                return stateIds.getState(id);
            }
        };
    }

    @Override
    public <V> MutableMapping<S, V> createStaticNodeMapping() {
        return automaton.createStaticStateMapping();
    }

    @Override
    public <V> MutableMapping<S, V> createDynamicNodeMapping() {
        return automaton.createDynamicStateMapping();
    }

}
