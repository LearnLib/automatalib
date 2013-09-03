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

import java.util.Map;

import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.transout.TransitionOutputAutomaton;


public class DOTHelperMealy<S, I, T, O> extends
		DefaultDOTHelperAutomaton<S, I, T, TransitionOutputAutomaton<S, I, T, O>> {

	public DOTHelperMealy(TransitionOutputAutomaton<S, I, T, O> automaton) {
		super(automaton);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.dot.DefaultDOTHelperAutomaton#getEdgeProperties(net.automatalib.commons.util.Pair, java.util.Map)
	 */
	@Override
	public boolean getEdgeProperties(S src, TransitionEdge<I, T> edge, S tgt, Map<String,String> properties) {
		if(!super.getEdgeProperties(src, edge, tgt, properties))
			return false;
		String label = String.valueOf(edge.getInput()) + " / ";
		O output = automaton.getTransitionOutput(edge.getTransition());
		if(output != null)
			label += String.valueOf(output);
		properties.put("label", label);
		return true;
	}
	
	

}
