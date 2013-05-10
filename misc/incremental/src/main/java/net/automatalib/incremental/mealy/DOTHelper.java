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
package net.automatalib.incremental.mealy;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.words.Alphabet;

/**
 * DOT helper used for rendering.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 */
final class DOTHelper extends DefaultDOTHelper<State, TransitionRecord> {
	private final State initial;
	private final Alphabet<?> inputAlphabet;
	
	/**
	 * Constructor.
	 * @param inputAlphabet the input alphabet
	 * @param initial the initial state
	 */
	public DOTHelper(Alphabet<?> inputAlphabet, State initial) {
		this.inputAlphabet = inputAlphabet;
		this.initial = initial;
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#initialNodes()
	 */
	@Override
	protected Collection<? extends State> initialNodes() {
		return Collections.singleton(initial);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getNodeProperties(State node, Map<String, String> properties) {
		if(!super.getNodeProperties(node, properties))
			return false;
		
		String shape = node.isConfluence() ? "octagon" : "circle";
		properties.put("shape", shape);
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getEdgeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getEdgeProperties(TransitionRecord edge,
			Map<String, String> properties) {
		if(!super.getEdgeProperties(edge, properties))
			return false;
		
		Object in = inputAlphabet.getSymbol(edge.transIdx);
		Object out = edge.source.getOutput(edge.transIdx);
		properties.put("label", String.valueOf(in) + " / " + String.valueOf(out));
		return true;
	}	
}