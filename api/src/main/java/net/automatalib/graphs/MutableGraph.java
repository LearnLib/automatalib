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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A graph that allows modification. Note that this interface only exposes
 * methods for extending a graph. If also destructive modifications should be performed,
 * {@link ShrinkableGraph} is the adequate interface.
 * 
 * @author Malte Isberner
 *
 * @param <N> node class
 * @param <E> edge class
 * @param <NP> node property class
 * @param <EP> edge property class
 */
@ParametersAreNonnullByDefault
public interface MutableGraph<N, E, NP, EP> extends UniversalGraph<N,E,NP,EP> {
	
	/**
	 * Adds a new node with default properties to the graph.
	 * This method behaves equivalently to the below {@link #addNode(Object)} with
	 * a <code>null</code> parameter.
	 * @return the newly inserted node
	 */
	@Nonnull
	public N addNode();
	
	/**
	 * Adds a new node to the graph.
	 * @param property the property for the new node
	 * @return the newly inserted node
	 */
	@Nonnull
	public N addNode(@Nullable NP property);
	
	/**
	 * Inserts an edge in the graph, with the default property.
	 * Calling this method should be equivalent to invoking
	 * {@link #connect(Object, Object, Object)} with a <tt>null</tt>
	 * property value.
	 * @param source the source node
	 * @param target the target node
	 * @return the edge connecting the given nodes
	 */
	@Nonnull
	public E connect(N source, N target);
	
	/**
	 * Inserts an edge in the graph.
	 * @param source the source node of the edge
	 * @param target the target node of the edge
	 * @param property the property of the edge
	 * @return the newly inserted edge
	 */
	@Nonnull
	public E connect(N source, N target, @Nullable EP property);
	
	public void setNodeProperty(N node, @Nullable NP property);
	public void setEdgeProperty(E edge, @Nullable EP property);
	
}
