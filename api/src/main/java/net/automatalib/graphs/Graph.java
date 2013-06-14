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

import net.automatalib.graphs.concepts.NodeIDs;


/**
 * Graph interface. Like an {@link IndefiniteGraph}, but with the additional requirement
 * that the set of nodes be finite.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <N> node class
 * @param <E> edge class
 */
public interface Graph<N, E> extends IndefiniteGraph<N,E>, Iterable<N> {
	
	/**
	 * Retrieves the number of nodes of this graph.
	 * @return the number of nodes of this graph.
	 */
	public int size();
	
	/**
	 * Retrieves an (unmodifiable) collection of the nodes in this graph. 
	 * @return the nodes in this graph
	 */
	public Collection<N> getNodes();
	
	
	public NodeIDs<N> nodeIDs();
	
}
