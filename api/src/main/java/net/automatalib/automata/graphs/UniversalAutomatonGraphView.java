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

import java.util.Collection;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.graphs.TransitionEdge.Property;
import net.automatalib.graphs.UniversalGraph;


public class UniversalAutomatonGraphView<S, I, T, SP, TP, A extends UniversalAutomaton<S, I, T, SP, TP>>
		extends AutomatonGraphView<S,I,T,A> implements UniversalGraph<S,TransitionEdge<I,T>,SP,TransitionEdge.Property<I,TP>> {
	
	public static <S,I,T,SP,TP,A extends UniversalAutomaton<S, I, T, SP, TP>>
	UniversalAutomatonGraphView<S, I, T, SP, TP, A> create(A automaton, Collection<? extends I> inputs) {
		return new UniversalAutomatonGraphView<>(automaton, inputs);
	}
	
	public static <S,I,T,SP,TP,A extends UniversalAutomaton<S,I,T,SP,TP> & InputAlphabetHolder<I>>
	UniversalAutomatonGraphView<S, I, T, SP, TP, A> create(A automaton) {
		return new UniversalAutomatonGraphView<>(automaton, automaton.getInputAlphabet());
	}
	
	public UniversalAutomatonGraphView(A automaton, Collection<? extends I> inputs) {
		super(automaton, inputs);
	}
	

	@Override
	public SP getNodeProperty(S node) {
		return automaton.getStateProperty(node);
	}

	@Override
	public Property<I, TP> getEdgeProperty(TransitionEdge<I, T> edge) {
		return new TransitionEdge.Property<>(edge.getInput(), automaton.getTransitionProperty(edge.getTransition()));
	}

}
