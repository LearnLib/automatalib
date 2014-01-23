/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.incremental.mealy.tree;

import java.util.Objects;

import net.automatalib.incremental.ConflictException;

final class Node<I,O> {
	private final Edge<I,O>[] outEdges;
	
	
	@SuppressWarnings("unchecked")
	public Node(int alphabetSize) {
		this.outEdges = new Edge[alphabetSize];
	}
	
	public Edge<I,O> getEdge(int idx) {
		return outEdges[idx];
	}
	
	public void setEdge(int idx, Edge<I,O> edge) {
		outEdges[idx] = edge;
	}
	
	public void setSuccessor(int idx, O output, Node<I,O> succ) {
		outEdges[idx] = new Edge<I,O>(output, succ);
	}
	
	public Node<I,O> getSuccessor(int idx) {
		Edge<I,O> edge = outEdges[idx];
		if(edge != null) {
			return edge.getTarget();
		}
		return null;
	}
	
	public Node<I,O> successor(int idx, O output) throws ConflictException {
		Edge<I,O> edge = outEdges[idx];
		if(edge != null) {
			if(!Objects.equals(output, edge.getOutput())) {
				throw new ConflictException("Output mismatch: '" + output + "' vs '" + edge.getOutput() + "'");
			}
			return edge.getTarget();
		}
		Node<I,O> succ = new Node<I,O>(outEdges.length);
		edge = new Edge<I,O>(output, succ);
		outEdges[idx] = edge;
		
		return succ;
	}
}
