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

import net.automatalib.automata.fsa.FiniteStateAcceptor;


public class DOTHelperFSA<S, I> extends DefaultDOTHelperAutomaton<S, I, S, FiniteStateAcceptor<S,I>> {

	public DOTHelperFSA(FiniteStateAcceptor<S, I> automaton) {
		super(automaton);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getNodeProperties(S node, Map<String,String> properties) {
		if(!super.getNodeProperties(node, properties))
			return false;
		if(automaton.isAccepting(node))
			properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
		return true;
	}
	
	
}
