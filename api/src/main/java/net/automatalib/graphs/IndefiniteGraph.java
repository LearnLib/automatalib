/* Copyright (C) 2013-2015 TU Dortmund
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.Iterators;

/**
 * Interface for an (indefinite) graph structure. A graph consists of nodes, each of which
 * has outgoing edges connecting to other nodes. In an indefinite graph, the node set is not
 * required to be finite.
 * 
 * @author Malte Isberner 
 *
 * @param <N> node type
 * @param <E> edge type
 */
@ParametersAreNonnullByDefault
public interface IndefiniteGraph<N, E> extends IndefiniteSimpleGraph<N> {
	
	@Override
	default public Collection<? extends N> getAdjacentTargets(N node) {
		return adjacentTargetsStream(node).collect(Collectors.toList());
	}
	
	@Override
	default public Iterator<N> adjacentTargetsIterator(N node) {
		return Iterators.transform(outgoingEdgesIterator(node), this::getTarget);
	}
	
	@Override
	default public Stream<N> adjacentTargetsStream(N node) {
		return outgoingEdgesStream(node).map(this::getTarget);
	}
	
	/**
	 * Retrieves the outgoing edges of a given node.
	 * @param node the node.
	 * @return a {@link Collection} of all outgoing edges, or <code>null</code> if
	 * the node has no outgoing edges.
	 */
	@Nonnull
	public Collection<? extends E> getOutgoingEdges(N node);
	
	@Nonnull
	@SuppressWarnings("unchecked")
	default public Iterator<E> outgoingEdgesIterator(N node) {
		return (Iterator<E>) getOutgoingEdges(node).iterator();
	}
	
	@Nonnull
	default public Iterable<E> outgoingEdges(N node) {
		return () -> outgoingEdgesIterator(node);
	}
	
	@Nonnull
	@SuppressWarnings("unchecked")
	default public Stream<E> outgoingEdgesStream(N node) {
		return (Stream<E>) getOutgoingEdges(node).stream();
	}
	
	/**
	 * Retrieves, for a given edge, its target node.
	 * @param edge the edge.
	 * @return the target node of the given edge.
	 */
	@Nonnull
	public N getTarget(E edge);
	
	@Nonnull
	default public Collection<? extends E> getEdgesBetween(N from, N to) {
		return getOutgoingEdges(from).stream()
				.filter(e -> Objects.equals(getTarget(e), to))
				.collect(Collectors.<E>toList());
	}
	
	@Override
	default public IndefiniteGraph<N,E> asNormalGraph() {
		return this;
	}
	
}
