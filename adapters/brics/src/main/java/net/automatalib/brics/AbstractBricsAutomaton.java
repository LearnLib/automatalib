/* Copyright (C) 2013-2014 TU Dortmund
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
package net.automatalib.brics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.fsa.abstractimpl.AbstractFSA;
import net.automatalib.automata.graphs.AbstractAutomatonGraph;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.GraphDOTHelper;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * Base class for Brics automata adapters.
 * 
 * @author Malte Isberner
 *
 */
@ParametersAreNonnullByDefault
public abstract class AbstractBricsAutomaton extends AbstractFSA<State, Character> implements
		DOTPlottableGraph<State, Transition>, UniversalGraph<State,Transition,Boolean,BricsTransitionProperty> {
	
	protected final Automaton automaton;

	/**
	 * Constructor.
	 * @param automaton the Brics automaton object. 
	 */
	public AbstractBricsAutomaton(Automaton automaton) {
		if(automaton == null) {
			throw new IllegalArgumentException("Provided Brics automaton must not be null");
		}
		this.automaton = automaton;
	}
	
	/**
	 * Retrieves the Brics automaton object.
	 * @return the brics automaton object
	 */
	public Automaton getBricsAutomaton() {
		return automaton;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.Graph#getNodes()
	 */
	@Override
	public Collection<State> getNodes() {
		return AbstractAutomatonGraph.getNodes(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.Graph#nodeIDs()
	 */
	@Override
	public NodeIDs<State> nodeIDs() {
		return AbstractAutomatonGraph.nodeIDs(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#getOutgoingEdges(java.lang.Object)
	 */
	@Override
	public Collection<Transition> getOutgoingEdges(State node) {
		return node.getTransitions();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#getTarget(java.lang.Object)
	 */
	@Override
	public State getTarget(Transition edge) {
		return edge.getDest();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#createStaticNodeMapping()
	 */
	@Override
	public <V> MutableMapping<State, V> createStaticNodeMapping() {
		return AbstractAutomatonGraph.createStaticNodeMapping(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#createDynamicNodeMapping()
	 */
	@Override
	public <V> MutableMapping<State, V> createDynamicNodeMapping() {
		return AbstractAutomatonGraph.createDynamicNodeMapping(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.acceptors.AcceptorTS#isAccepting(java.lang.Object)
	 */
	@Override
	public boolean isAccepting(State state) {
		return state.isAccept();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.TransitionSystem#getTransitions(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Collection<State> getTransitions(State state, @Nonnull Character input) {
		Collection<Transition> transitions = state.getSortedTransitions(false);
		
		Set<State> result = new HashSet<>();
		
		for(Transition t : transitions) {
			char min = t.getMin();
			if(input < min)
				break;
			char max = t.getMax();
			if(input > max)
				continue;
			result.add(t.getDest());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.simple.SimpleTS#getInitialStates()
	 */
	@Override
	public Set<State> getInitialStates() {
		return Collections.singleton(automaton.getInitialState());
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.simple.SimpleAutomaton#getStates()
	 */
	@Override
	public Collection<State> getStates() {
		return automaton.getStates();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DOTPlottableGraph#getGraphDOTHelper()
	 */
	@Override
	public GraphDOTHelper<State, Transition> getGraphDOTHelper() {
		return new BricsDOTHelper(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.UniversalIndefiniteGraph#getNodeProperties(java.lang.Object)
	 */
	@Override
	public Boolean getNodeProperty(State node) {
		return node.isAccept();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.UniversalIndefiniteGraph#getEdgeProperties(java.lang.Object)
	 */
	@Override
	public BricsTransitionProperty getEdgeProperty(Transition edge) {
		return new BricsTransitionProperty(edge);
	}
}
