/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.util.automata.asgraph;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.NodeIDs;


public class AutomatonAsGraph<S, I, T,A extends Automaton<S,I,T>> implements Graph<S, Pair<I, T>> {
	
	
	protected final A automaton;
	protected final Collection<? extends I> inputAlphabet;

	
	public AutomatonAsGraph(A automaton, Collection<? extends I> inputAlphabet) {
		this.automaton = automaton;
		this.inputAlphabet = inputAlphabet;
	}

	@Override
	public Iterator<S> iterator() {
		return automaton.iterator();
	}

	@Override
	public Collection<S> getNodes() {
		return automaton.getStates();
	}

	@Override
	public Collection<Pair<I, T>> getOutgoingEdges(S node) {
		return AGHelper.outgoingEdges(automaton, node, inputAlphabet);
	}

	@Override
	public S getTarget(Pair<I, T> edge) {
		return automaton.getSuccessor(edge.getSecond());
	}

	@Override
	public int size() {
		return automaton.size();
	}

	@Override
	public <V> MutableMapping<S, V> createStaticNodeMapping() {
		return automaton.createStaticStateMapping();
	}

	@Override
	public <V> MutableMapping<S, V> createDynamicNodeMapping() {
		return automaton.createDynamicStateMapping();
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

}
