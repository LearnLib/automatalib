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
package net.automatalib.graphs;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.ts.UniversalTransitionSystem;

/**
 * A universal graph, i.e., with (possibly empty) node and edge properties. For a documentation
 * on the concept of "universal", see {@link UniversalTransitionSystem}.
 * 
 * @author Malte Isberner
 *
 * @param <N> node class
 * @param <E> edge class
 * @param <NP> node property class
 * @param <EP> edge property class
 */
@ParametersAreNonnullByDefault
public interface UniversalIndefiniteGraph<N, E, NP, EP> extends IndefiniteGraph<N,E> {
	/**
	 * Retrieves the property of a given node.
	 * @param node the node
	 * @return the property of the specified node
	 */
	@Nullable
	public NP getNodeProperty(N node);
	
	/**
	 * Retrieves the property of a given edge.
	 * @param edge the edge
	 * @return the property of the specified edge
	 */
	@Nullable
	public EP getEdgeProperty(E edge);
}
