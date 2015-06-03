/* Copyright (C) 2013-2014 TU Dortmund
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
package net.automatalib.brics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.fsa.FiniteStateAcceptor;
import net.automatalib.automata.graphs.AbstractAutomatonGraphView;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.concepts.GraphViewable;
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
public abstract class AbstractBricsAutomaton implements
		FiniteStateAcceptor<State,Character>, GraphViewable {
	
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
	
	@Override
	public GraphView graphView() {
		return new GraphView();
	}
	
	public class GraphView
			extends
			AbstractAutomatonGraphView<State, AbstractBricsAutomaton, Transition>
			implements
			UniversalGraph<State, Transition, Boolean, BricsTransitionProperty> {
		public GraphView() {
			super(AbstractBricsAutomaton.this);
		}

		@Override
		public Collection<? extends Transition> getOutgoingEdges(State node) {
			return node.getTransitions();
		}

		@Override
		public State getTarget(Transition edge) {
			return edge.getDest();
		}

		@Override
		public GraphDOTHelper<State, Transition> getGraphDOTHelper() {
			return new BricsDOTHelper(AbstractBricsAutomaton.this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.automatalib.graphs.UniversalIndefiniteGraph#getNodeProperties
		 * (java.lang.Object)
		 */
		@Override
		public Boolean getNodeProperty(State node) {
			return node.isAccept();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.automatalib.graphs.UniversalIndefiniteGraph#getEdgeProperties
		 * (java.lang.Object)
		 */
		@Override
		public BricsTransitionProperty getEdgeProperty(Transition edge) {
			return new BricsTransitionProperty(edge);
		}
	}

}
