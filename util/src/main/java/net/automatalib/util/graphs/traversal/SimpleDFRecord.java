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
package net.automatalib.util.graphs.traversal;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import net.automatalib.graphs.IndefiniteGraph;


class SimpleDFRecord<N, E> {
	public final N node;
	
	private Iterator<E> edgeIterator;
	private E retractedEdge = null;


	public SimpleDFRecord(N node) {
		this.node = node;
	}
	
	public final boolean wasStarted() {
		return (edgeIterator != null);
	}
	
	public final boolean start(IndefiniteGraph<N,E> graph) {
		if(edgeIterator != null)
			return false;
		Collection<E> outEdges = graph.getOutgoingEdges(node);
		if(outEdges == null)
			this.edgeIterator = Collections.<E>emptySet().iterator();
		else
			this.edgeIterator = outEdges.iterator();
		return true;
	}
	
	public final boolean hasNextEdge() {
		return (retractedEdge != null) || edgeIterator != null && edgeIterator.hasNext();
	}
	
	public final E nextEdge() {
		if(retractedEdge != null) {
			E tmp = retractedEdge;
			retractedEdge = null;
			return tmp;
		}
		return edgeIterator.next();
	}
	
	public final void retract(E edge) {
		assert retractedEdge == null;
		retractedEdge = edge;
	}

}
