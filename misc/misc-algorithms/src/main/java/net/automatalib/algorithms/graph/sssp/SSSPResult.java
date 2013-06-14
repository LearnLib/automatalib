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
package net.automatalib.algorithms.graph.sssp;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.algorithms.graph.GraphAlgorithms;

/**
 * Result interface for the single-source shortest path (SSSP) problem.
 * 
 * @author Malte Isberner
 *
 * @param <N> node class
 * @param <E> edge class
 */
@ParametersAreNonnullByDefault
public interface SSSPResult<N, E> {
	
	
	/**
	 * Retrieves the node the source was started from.
	 * @return the source node
	 */
	@Nonnull
	public N getInitialNode();
	
	/**
	 * Retrieves the length of the shortest path from the initial node
	 * to the given one.
	 * @param target the target node
	 * @return the length of the shortest path from the initial node to
	 * the given target node, or {@link GraphAlgorithms#INVALID_DISTANCE} if there exists no
	 * such path.
	 */
	public float getShortestPathDistance(N target);
	
	/**
	 * Retrieves the shortest path from the initial node to the given one (as a sequence of edges),
	 * or <tt>null</tt> if there exists no such path.
	 * <p>
	 * Note that implementations might construct these paths on-the-fly.
	 * 
	 * @param target the target node
	 * @return the path from the initial node to the given target node, or <tt>null</tt> if
	 * there exists no such path.
	 */
	@Nullable
	public List<E> getShortestPath(N target);
	
	/**
	 * Retrieves the incoming edge via which the given node is reached on the shortest path.
	 * If the node is not reachable or it is the initial node, <tt>null</tt> is returned.
	 * 
	 * @param target the target node
	 * @return the reaching edge on the shortest path, or <tt>null</tt>.
	 */
	@Nullable
	public E getShortestPathEdge(N target);
}
