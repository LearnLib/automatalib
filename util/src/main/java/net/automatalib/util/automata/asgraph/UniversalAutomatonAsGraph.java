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

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.UniversalGraph;

public class UniversalAutomatonAsGraph<S, I, T, SP, TP,A extends UniversalAutomaton<S, I, T, SP, TP>>
		extends AutomatonAsGraph<S, I, T, A> implements UniversalGraph<S, Pair<I,T>, SP, Pair<I,TP>> {
	
	public UniversalAutomatonAsGraph(A automaton,
			Collection<? extends I> inputs) {
		super(automaton, inputs);
	}
	
	@Override
	public SP getNodeProperties(S node) {
		return automaton.getStateProperty(node);
	}

	@Override
	public Pair<I, TP> getEdgeProperties(Pair<I, T> edge) {
		return Pair.make(edge.getFirst(),
				automaton.getTransitionProperty(edge.getSecond()));
	}

}
