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
package net.automatalib.automata.transout;

import java.util.Collection;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.dot.DOTHelperMealy;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.graphs.UniversalAutomatonGraphView;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.ts.transout.MealyTransitionSystem;

/**
 *
 * @author fh
 */
public interface MealyMachine<S,I,T,O> extends UniversalDeterministicAutomaton<S, I, T, Void, O>,
		TransitionOutputAutomaton<S,I,T,O>, MealyTransitionSystem<S, I, T, O> {
	
	public static class MealyGraphView<S, I, T, O, A extends MealyMachine<S, I, T, O>>
			extends UniversalAutomatonGraphView<S, I, T, Void, O, A> {

		public MealyGraphView(A automaton, Collection<? extends I> inputs) {
			super(automaton, inputs);
		}

		@Override
		public GraphDOTHelper<S, ? super TransitionEdge<I, T>> getGraphDOTHelper() {
			return new DOTHelperMealy<>(automaton);
		}
	}
	
	@Override
	default public UniversalGraph<S,TransitionEdge<I,T>,Void,TransitionEdge.Property<I,O>>
	transitionGraphView(Collection<? extends I> inputs) {
		return new MealyGraphView<S,I,T,O,MealyMachine<S,I,T,O>>(this, inputs);
	}
}
