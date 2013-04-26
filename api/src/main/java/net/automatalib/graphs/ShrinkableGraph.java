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
package net.automatalib.graphs;

/**
 * A graph that supports (desirably efficient) removal of nodes and edges.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <N> node class
 * @param <E> edge class
 */
public interface ShrinkableGraph<N, E> extends Graph<N, E> {
	
	/**
	 * Removes a node from this graph. All incoming and outgoing edges are removed as well.
	 * @param node the node to remove.
	 */
	public void removeNode(N node);
	
	/**
	 * Removes a node from this graph, and redirects all incoming edges to
	 * the given replacement node (node that outgoing edges are still removed).
	 * If a <tt>null</tt> replacement is specified, then this function behaves
	 * equivalently to the above {@link #removeNode(Object)}. 
	 * @param node the node to remove
	 * @param replacement the replacement node for incoming edges
	 */
	public void removeNode(N node, N replacement);
	
	/**
	 * Removes an outgoing edge from the given node.
	 * @param node the node
	 * @param edge the edge to remove
	 */
	public void removeEdge(N node, E edge);
}
