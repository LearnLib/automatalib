/* Copyright (C) 2014 TU Dortmund
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
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.dot.DefaultDOTHelperAutomaton;
import net.automatalib.graphs.dot.GraphDOTHelper;

public class AutomatonGraphView<S, I, T, A extends Automaton<S,I,T>> extends AbstractAutomatonGraphView<S,A,TransitionEdge<I,T>> {
	
	public static <S,I,T> Collection<TransitionEdge<I,T>> createTransitionEdges(Automaton<S,I,T> automaton, Collection<? extends I> inputs, S state) {
		List<TransitionEdge<I,T>> result
			= new ArrayList<TransitionEdge<I,T>>();
	
		
		for(I input : inputs) {
			Collection<? extends T> transitions = automaton.getTransitions(state, input);
			for(T t : transitions) {
				result.add(new TransitionEdge<>(input, t));
			}
		}
		
		return result;
	}
	
	public static <S,I,T,A extends Automaton<S,I,T>>
	AutomatonGraphView<S,I,T,A> create(A automaton, Collection<? extends I> inputs) {
		return new AutomatonGraphView<>(automaton, inputs);
	}
	
	public static <S,I,T,A extends Automaton<S,I,T> & InputAlphabetHolder<I>>
	AutomatonGraphView<S,I,T,A> create(A automaton) {
		return new AutomatonGraphView<>(automaton, automaton.getInputAlphabet());
	}
	
	
	protected final Collection<? extends I> inputs;
	
	public AutomatonGraphView(A automaton, Collection<? extends I> inputs) {
		super(automaton);
		this.inputs = inputs;
	}

	@Override
	public Collection<? extends TransitionEdge<I, T>> getOutgoingEdges(S node) {
		return createTransitionEdges(automaton, inputs, node);
	}

	@Override
	public S getTarget(TransitionEdge<I, T> edge) {
		return automaton.getSuccessor(edge.getTransition());
	}

	@Override
	public GraphDOTHelper<S, ? super TransitionEdge<I,T>> getGraphDOTHelper() {
		return new DefaultDOTHelperAutomaton<>(automaton);
	}
}
