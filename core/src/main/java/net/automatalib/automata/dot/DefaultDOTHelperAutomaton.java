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
package net.automatalib.automata.dot;

import java.util.Collection;
import java.util.Map;

import net.automatalib.automata.Automaton;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.dot.DefaultDOTHelper;


public class DefaultDOTHelperAutomaton<S, I, T, A extends Automaton<S, I, T>> 
		extends DefaultDOTHelper<S,Pair<I,T>> {
	
	protected final A automaton;
	
	public DefaultDOTHelperAutomaton(A automaton) {
		this.automaton = automaton;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#initialNodes()
	 */
	@Override
	protected Collection<? extends S> initialNodes() {
		return automaton.getInitialStates();
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getEdgeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getEdgeProperties(Pair<I, T> edge, Map<String,String> properties) {
		if(!super.getEdgeProperties(edge, properties))
			return false;
		String label = String.valueOf(edge.getFirst());
		properties.put("label", label);
		return true;
	}

}
