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
package net.automatalib.brics;

import java.util.Collection;
import java.util.Map;

import net.automatalib.graphs.dot.DefaultDOTHelper;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * DOT rendering helper for Brics automaton adapters.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
final class BricsDOTHelper extends DefaultDOTHelper<State, Transition> {

	private final AbstractBricsAutomaton automaton;
	
	/**
	 * Constructor.
	 * @param automaton the automaton to render
	 */
	public BricsDOTHelper(AbstractBricsAutomaton automaton) {
		this.automaton = automaton;
	}
	

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#initialNodes()
	 */
	@Override
	protected Collection<? extends State> initialNodes() {
		return automaton.getInitialStates();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getNodeProperties(State node, Map<String, String> properties) {
		if(!super.getNodeProperties(node, properties))
			return false;
		
		String str = node.toString();
		int wsIdx1 = str.indexOf(' ');
		int wsIdx2 = str.indexOf(' ', wsIdx1 + 1);
		properties.put(LABEL, "s" + str.substring(wsIdx1 + 1, wsIdx2));
		if(node.isAccept())
			properties.put(SHAPE, "doublecircle");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getEdgeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getEdgeProperties(Transition edge,
			Map<String, String> properties) {
		if(!super.getEdgeProperties(edge, properties))
			return false;
		
		String label = BricsTransitionProperty.toString(edge.getMin(), edge.getMax());
		properties.put(LABEL, label);
		return true;
	}

}
