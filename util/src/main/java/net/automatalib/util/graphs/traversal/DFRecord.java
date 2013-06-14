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


class DFRecord<N, E, D> extends SimpleDFRecord<N, E> {
	
	public static class LastEdge<E,N,D> {
		public final E edge;
		public final N node;
		public final D data;
		
		public LastEdge(E edge, N node, D data) {
			this.edge = edge;
			this.node = node;
			this.data = data;
		}
	}
	
	public final D data;
	private LastEdge<E,N,D> lastEdge;
	
	public DFRecord(N node, D data) {
		super(node);
		this.data = data;
	}
	
	public D getData() {
		return data;
	}

	public LastEdge<E, N, D> getLastEdge() {
		LastEdge<E,N,D> result = lastEdge;
		lastEdge = null;
		return result;
	}
	
	public void setLastEdge(E edge, N tgtNode, D tgtData) {
		assert lastEdge == null;
		lastEdge = new LastEdge<>(edge, tgtNode, tgtData);
	}
}
