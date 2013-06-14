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

import net.automatalib.commons.util.mappings.MutableMapping;

/**
 * Interface for an (indefinite) graph structure. A graph consists of nodes, each of which
 * has outgoing edges connecting to other nodes. In an indefinite graph, the node set is not
 * required to be finite.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <N> node class.
 * @param <E> edge class.
 */
public interface IndefiniteGraph<N, E> {
	
	/**
	 * Retrieves the outgoing edges of a given node.
	 * @param node the node.
	 * @return a {@link Collection} of all outgoing edges, or <code>null</code> if
	 * the node has no outgoing edges.
	 */
	public Collection<E> getOutgoingEdges(N node);
	
	/**
	 * Retrieves, for a given edge, its target node.
	 * @param edge the edge.
	 * @return the target node of the given edge.
	 */
	public N getTarget(E edge);
	
	
	public <V> MutableMapping<N,V> createStaticNodeMapping();
	public <V> MutableMapping<N,V> createDynamicNodeMapping();
}
