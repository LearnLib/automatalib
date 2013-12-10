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
import java.util.Map;

import net.automatalib.commons.util.mappings.Mapping;

public class DelegateDOTHelper<N, E> implements GraphDOTHelper<N, E> {

	private final GraphDOTHelper<N,? super E> parent;
	
	protected DelegateDOTHelper(GraphDOTHelper<N,? super E> parent) {
		this.parent = parent;
	}

	@Override
	public void writePreamble(Appendable a) throws IOException {
		parent.writePreamble(a);
	}

	@Override
	public void writePostamble(Mapping<N, String> identifiers, Appendable a)
			throws IOException {
		parent.writePostamble(identifiers, a);
	}

	@Override
	public boolean getNodeProperties(N node, Map<String, String> properties) {
		return parent.getNodeProperties(node, properties);
	}

	@Override
	public boolean getEdgeProperties(N src, E edge, N tgt,
			Map<String, String> properties) {
		return parent.getEdgeProperties(src, edge, tgt, properties);
	}

	@Override
	public void getGlobalNodeProperties(Map<String, String> properties) {
		parent.getGlobalNodeProperties(properties);
	}

	@Override
	public void getGlobalEdgeProperties(Map<String, String> properties) {
		parent.getGlobalEdgeProperties(properties);
	}
}
