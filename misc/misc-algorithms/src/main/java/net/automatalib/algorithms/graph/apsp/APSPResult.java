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
package net.automatalib.algorithms.graph.apsp;

import java.util.List;

/**
 * Result interface for the all pairs shortest paths problem.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <N> node class
 * @param <E> edge class
 */
public interface APSPResult<N,E> {

	
	/**
	 * Retrieves the length of the shortest path between the given nodes.
	 * @param src the source node
	 * @param tgt the target node
	 * @return the length of the shortest path from <tt>src</tt> to <tt>tgt</tt>,
	 * or {@link GraphAlgorithms#INVALID_DISTANCE} if there exists no such path.
	 */
	public float getShortestPathDistance(N src, N tgt);
	
	/**
	 * Retrieves the shortest path between the given nodes, or <tt>null</tt> if there
	 * exists no such path.
	 * @param src the source node
	 * @param tgt the target node
	 * @return the shortest path from <tt>src</tt> to <tt>tgt</tt>, or <tt>null</tt>
	 * if there exists no such path.
	 */
	public List<E> getShortestPath(N src, N tgt);
}
