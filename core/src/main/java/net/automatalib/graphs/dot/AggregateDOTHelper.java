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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.graphs.dot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.automatalib.commons.util.mappings.Mapping;

public class AggregateDOTHelper<N, E> implements GraphDOTHelper<N, E> {
	
	private final List<GraphDOTHelper<N,? super E>> helpers;
	
	public AggregateDOTHelper() {
		helpers = new ArrayList<>();
	}
	
	public AggregateDOTHelper(List<? extends GraphDOTHelper<N, ? super E>> helpers) {
		this.helpers = new ArrayList<>(helpers);
	}
	
	public void add(GraphDOTHelper<N,? super E> helper) {
		this.helpers.add(helper);
	}

	@Override
	public void writePreamble(Appendable a) throws IOException {
		for(GraphDOTHelper<N,? super E> helper : helpers)
			helper.writePreamble(a);
	}

	@Override
	public void writePostamble(Mapping<N, String> identifiers, Appendable a)
			throws IOException {
		for(GraphDOTHelper<N,? super E> helper : helpers)
			helper.writePostamble(identifiers, a);
	}

	@Override
	public boolean getNodeProperties(N node, Map<String, String> properties) {
		for(GraphDOTHelper<N,? super E> helper : helpers) {
			if(!helper.getNodeProperties(node, properties))
				return false;
		}
		return true;
	}

	@Override
	public boolean getEdgeProperties(E edge, Map<String, String> properties) {
		for(GraphDOTHelper<N,? super E> helper : helpers) {
			if(!helper.getEdgeProperties(edge, properties))
				return false;
		}
		return true;
	}

}
