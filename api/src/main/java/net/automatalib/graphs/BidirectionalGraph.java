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
package net.automatalib.graphs;

import java.util.Collection;

/**
 * Interface for bidirectional graph. A bidirectional graph is conceptually the same as
 * a normal (directed) graph, but provides direct access to not only the outgoing, but also
 * the incoming edges of each state.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <N> node class.
 * @param <E> edge class
 */
public interface BidirectionalGraph<N, E> extends Graph<N, E> {
	/**
	 * Retrieves the incoming edges of a given node.
	 * @param node the node
	 * @return all incoming edges of the specified node.
	 */
	public Collection<E> getIncomingEdges(N node);
	
	/**
	 * Retrieves the source node of a given edge.
	 * @param edge the edge
	 * @return the source node of the given edge
	 */
	public N getSource(E edge);
}
