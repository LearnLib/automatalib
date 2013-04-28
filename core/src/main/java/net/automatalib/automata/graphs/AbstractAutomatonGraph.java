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
package net.automatalib.automata.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.FiniteAlphabetAutomaton;
import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.abstractimpl.AbstractAutomaton;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.concepts.NodeIDs;

public abstract class AbstractAutomatonGraph<S,I,T,SP,TP>
		extends AbstractAutomaton<S, I, T> implements FiniteAlphabetAutomaton<S,I,T>, UniversalAutomaton<S,I,T,SP,TP>, UniversalGraph<S,Pair<I,T>,SP,Pair<I,TP>> {
	
	
	public static <S,I,T> Collection<Pair<I,T>> createOutgoingEdges(Automaton<S,I,T> automaton, Collection<? extends I> inputs, S state) {
		List<Pair<I,T>> result
			= new ArrayList<Pair<I,T>>();
	
		
		for(I input : inputs) {
			Collection<T> transitions = automaton.getTransitions(state, input);
			if(transitions == null)
				continue;
			for(T t : transitions)
				result.add(Pair.make(input, t));
		}
		
		return result;
	}
	
	
	public static <S,I,T> Collection<S> getNodes(Automaton<S,I,T> $this) {
		return $this.getStates();
	}
	
	public static <S,I,T> NodeIDs<S> nodeIDs(Automaton<S,I,T> $this) {
		return new StateAsNodeIDs<>($this.stateIDs());
	}
	
	public static <S,I,T> Collection<Pair<I,T>> getOutgoingEdges(FiniteAlphabetAutomaton<S,I,T> $this, S node) {
		return createOutgoingEdges($this, $this.getInputAlphabet(), node);
	}
	
	public static <S,I,T> S getTarget(Automaton<S,I,T> $this, Pair<I,T> edge) {
		return $this.getSuccessor(edge.getSecond());
	}
	
	public static <S,I,T,V> MutableMapping<S, V> createStaticNodeMapping(Automaton<S,I,T> $this) {
		return $this.createStaticStateMapping();
	}

	public static <S,I,T,V> MutableMapping<S, V> createDynamicNodeMapping(Automaton<S,I,T> $this) {
		return $this.createDynamicStateMapping();
	}
	
	public static <S,I,T,SP,TP> SP getNodeProperties(UniversalAutomaton<S, I, T, SP, TP> $this, S node) {
		return $this.getStateProperty(node);
	}


	public static <S,I,T,SP,TP> Pair<I, TP> getEdgeProperties(UniversalAutomaton<S,I,T,SP,TP> $this, Pair<I, T> edge) {
		TP transProp = $this.getTransitionProperty(edge.getSecond());
		return Pair.make(edge.getFirst(), transProp);
	}
	
	@Override
	public Collection<S> getNodes() {
		return getNodes(this);
	}

	@Override
	public NodeIDs<S> nodeIDs() {
		return nodeIDs(this);
	}

	@Override
	public Collection<Pair<I, T>> getOutgoingEdges(S node) {
		return getOutgoingEdges(this, node);
	}

	@Override
	public S getTarget(Pair<I, T> edge) {
		return getTarget(this, edge);
	}

	@Override
	public <V> MutableMapping<S, V> createStaticNodeMapping() {
		return createStaticNodeMapping(this);
	}

	@Override
	public <V> MutableMapping<S, V> createDynamicNodeMapping() {
		return createDynamicNodeMapping(this);
	}


	@Override
	public SP getNodeProperties(S node) {
		return getNodeProperties(this, node);
	}


	@Override
	public Pair<I, TP> getEdgeProperties(Pair<I, T> edge) {
		return getEdgeProperties(this, edge);
	}


}
