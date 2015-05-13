/* Copyright (C) 2015 TU Dortmund
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
package net.automatalib.automata.transout.impl.map;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.automatalib.graphs.MutableGraph;
import net.automatalib.graphs.ShrinkableGraph;

import com.google.common.collect.Iterators;

/**
 * A very simple graph realization, where nodes can be arbitrary Java objects.
 * This graph does not support edge properties.
 * <p>
 * This class provides maximum flexibility, but should only be used if performance
 * is not a major concern.
 * 
 * @author Malte Isberner
 *
 * @param <N> node type
 */
public class SimpleMapGraph<N> implements MutableGraph<N,N,N,Void>, ShrinkableGraph<N, N> {

	@Nonnull
	private final Map<N,Collection<N>> structureMap;
	@Nonnull
	private final Supplier<? extends Collection<N>> adjCollSupplier;
	
	/**
	 * Initializes a graph where the adjacency structure is stored using a {@link HashMap},
	 * and adjacency information for a single node is stored using {@link HashSet}s.
	 */
	public SimpleMapGraph() {
		this(new HashMap<>(), HashSet::new);
	}
	
	/**
	 * Initializes a graph where the adjacency structure is stored using a {@link HashMap},
	 * and adjacency information for a single node is stored in data structures created via
	 * the provided supplier.
	 * 
	 * @param adjCollSupplier the supplier for per-node adjacency collections
	 */
	public SimpleMapGraph(Supplier<? extends Collection<N>> adjCollSupplier) {
		this(new HashMap<>(), adjCollSupplier);
	}
	
	/**
	 * Initializes a graph using the given adjacency structure, and adjacency information for
	 * a single node is stored using {@link HashSet}s.
	 * @param structureMap the map for the overall graph structure
	 */
	public SimpleMapGraph(Map<N,Collection<N>> structureMap) {
		this(structureMap, HashSet::new);
	}
	
	/**
	 * Initializes a graph using the given adjacency structure, and adjacency information for
	 * a single node is stored in data structures created via the provided supplier.
	 * @param structureMap the map for the overall graph structure
	 * @param adjCollSupplier the supplier for per-node adjacency collections
	 */
	public SimpleMapGraph(Map<N,Collection<N>> structureMap, Supplier<? extends Collection<N>> adjCollSupplier) {
		this.structureMap = structureMap;
		this.adjCollSupplier = adjCollSupplier;
	}

	@Override
	public Collection<? extends N> getOutgoingEdges(N node) {
		return Collections.unmodifiableCollection(structureMap.getOrDefault(node, Collections.emptySet()));
	}

	@Override
	public N getTarget(N edge) {
		return edge;
	}

	@Override
	public Collection<? extends N> getAdjacentTargets(N node) {
		return getOutgoingEdges(node);
	}
	
	@Override
	public Iterator<N> adjacentTargetsIterator(N node) {
		return outgoingEdgesIterator(node);
	}
	
	@Override
	public Stream<N> adjacentTargetsStream(N node) {
		return outgoingEdgesStream(node);
	}

	@Override
	public Set<? extends N> getNodes() {
		return Collections.unmodifiableSet(structureMap.keySet());
	}

	@Override
	public Iterator<N> iterator() {
		return Iterators.unmodifiableIterator(structureMap.keySet().iterator());
	}

	@Override
	public N getNodeProperty(N node) {
		return node;
	}

	@Override
	public Void getEdgeProperty(N edge) {
		return null;
	}

	@Override
	public N addNode(N property) {
		structureMap.putIfAbsent(property, adjCollSupplier.get());
		return property;
	}

	@Override
	public N connect(N source, N target, Void property) {
		structureMap.get(source).add(target);
		return target;
	}

	@Override
	@Deprecated
	public void setNodeProperty(N node, N property) {
		if (!Objects.equals(node, property)) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void setEdgeProperty(N edge, Void property) {
	}

	@Override
	public void removeNode(N node) {
		structureMap.remove(node);
		structureMap.values().stream().forEach(a -> a.remove(node));
	}

	@Override
	public void removeNode(N node, N replacement) {
		structureMap.remove(node);
		structureMap.values().stream().forEach(a -> {
			if (a.remove(node)) {
				a.add(replacement);
			}
		});
	}

	@Override
	public void removeEdge(N node, N edge) {
		structureMap.get(node).remove(edge);
	}

}
