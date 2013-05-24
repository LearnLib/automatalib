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
package net.automatalib.incremental.dfa;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.words.Alphabet;

final class DOTHelper extends DefaultDOTHelper<State, EdgeRecord> {
	
	private final Alphabet<?> alphabet;
	private final State initial;
	
	public DOTHelper(Alphabet<?> alphabet, State initial) {
		this.alphabet = alphabet;
		this.initial = initial;
	}

	
	@Override
	protected Collection<? extends State> initialNodes() {
		return Collections.singleton(initial);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.GraphDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getNodeProperties(State node, Map<String, String> properties) {
		if(!super.getNodeProperties(node, properties))
			return false;
		
		String baseShape = node.isConfluence() ? "octagon" : "circle";
		
		if(node.getAcceptance() == Acceptance.TRUE)
			properties.put("shape", "double" + baseShape);
		else {
			properties.put("shape", baseShape);
			if(node.getAcceptance() == Acceptance.DONT_KNOW)
				properties.put("style", "dashed");
		}
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.GraphDOTHelper#getEdgeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getEdgeProperties(State src, EdgeRecord edge, State tgt,
			Map<String, String> properties) {
		if(!super.getEdgeProperties(src, edge, tgt, properties))
			return false;
		
		Object sym = alphabet.getSymbol(edge.transIdx);
		properties.put("label", String.valueOf(sym));
		return true;
	}

}
