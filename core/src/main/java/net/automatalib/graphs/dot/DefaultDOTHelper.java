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
package net.automatalib.graphs.dot;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.automatalib.commons.util.mappings.Mapping;

public class DefaultDOTHelper<N, E> implements GraphDOTHelper<N, E> {
	
	protected static final String START_PREFIX = "__start";
	
	
	private static final DefaultDOTHelper<Object,Object> DEFAULT_INSTANCE
		= new DefaultDOTHelper<Object,Object>();
	
	public static DefaultDOTHelper<Object,Object> getInstance() {
		return DEFAULT_INSTANCE;
	}
	
	protected Collection<? extends N> initialNodes() {
		return Collections.emptySet();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.GraphDOTHelper#writePreamble(java.lang.Appendable)
	 */
	@Override
	public void writePreamble(Appendable a) throws IOException {
		int size = initialNodes().size();
		
		for(int i = 0; i < size; i++) {
			a.append(START_PREFIX).append(Integer.toString(i));
			a.append(" [label=\"\" shape=\"none\"];\n");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.GraphDOTHelper#writePostamble(java.lang.Appendable)
	 */
	@Override
	public void writePostamble(Mapping<N,String> identifiers, Appendable a) throws IOException {
		Collection<? extends N> initials = initialNodes();
		
		int i = 0;
		for(N init : initials) {
			a.append(START_PREFIX).append(Integer.toString(i++));
			a.append(" -> ").append(identifiers.get(init)).append(";\n");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.GraphDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getNodeProperties(N node, Map<String,String> properties) {
		String label = String.valueOf(node);
		properties.put(LABEL, label);
		properties.put(SHAPE, "circle");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.GraphDOTHelper#getEdgeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getEdgeProperties(N src, E edge, N tgt, Map<String,String> properties) {
		return true;
	}

}
