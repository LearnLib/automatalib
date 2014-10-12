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

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;

import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.graphs.dot.EmptyDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.graphs.helpers.SimpleNodeIDs;

import com.google.common.collect.Iterators;


/**
 * Graph interface. Like an {@link IndefiniteGraph}, but with the additional requirement
 * that the set of nodes be finite.
 * 
 * @author Malte Isberner
 *
 * @param <N> node class
 * @param <E> edge class
 */
public interface Graph<N, E> extends IndefiniteGraph<N,E>, Iterable<N> {
	
	/**
	 * Retrieves the number of nodes of this graph.
	 * @return the number of nodes of this graph.
	 */
	default public int size() {
		return getNodes().size();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Nonnull
	default public Iterator<N> iterator() {
		return (Iterator<N>)Iterators.unmodifiableIterator(getNodes().iterator());
	}
	
	/**
	 * Retrieves an (unmodifiable) collection of the nodes in this graph. 
	 * @return the nodes in this graph
	 */
	@Nonnull
	public Collection<? extends N> getNodes();
	
	
	@Nonnull
	default public NodeIDs<N> nodeIDs() {
		return new SimpleNodeIDs<>(this);
	}
	
	
	default public GraphDOTHelper<N, ? super E> getGraphDOTHelper() {
		return new EmptyDOTHelper<N,E>();
	}
}
