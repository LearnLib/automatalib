/* Copyright (C) 2013-2014 TU Dortmund
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
package net.automatalib.util.graphs.traversal;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.graphs.IndefiniteGraph;


class SimpleDFRecord<N, E> {
	public final N node;
	
	private Iterator<? extends E> edgeIterator;


	public SimpleDFRecord(N node) {
		this.node = node;
	}
	
	public final boolean wasStarted() {
		return (edgeIterator != null);
	}
	
	public final boolean start(IndefiniteGraph<N,E> graph) {
		if(edgeIterator != null)
			return false;
		Collection<? extends E> outEdges = graph.getOutgoingEdges(node);
		this.edgeIterator = outEdges.iterator();
		return true;
	}
	
	public final boolean hasNextEdge() {
		return edgeIterator.hasNext();
	}
	
	public final E nextEdge() {
		return edgeIterator.next();
	}

}
