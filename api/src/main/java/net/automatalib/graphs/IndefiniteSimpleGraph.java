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
package net.automatalib.graphs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.automatalib.commons.util.mappings.MapMapping;
import net.automatalib.commons.util.mappings.MutableMapping;

/**
 * A simplified interface for indefinite graphs, exposing only adjacency
 * information, but no further information about edge objects.
 * 
 * @author Malte Isberner
 *
 * @param <N> node type
 */
public interface IndefiniteSimpleGraph<N> {
	
	@Nonnull
	public Collection<? extends N> getAdjacentTargets(N node);
	
	@Nonnull
	@SuppressWarnings("unchecked")
	default public Iterator<N> adjacentTargetsIterator(N node) {
		return (Iterator<N>) getAdjacentTargets(node).iterator();
	}
	
	@Nonnull
	default public Iterable<N> adjacentTargets(N node) {
		return () -> adjacentTargetsIterator(node);
	}
	
	@Nonnull
	@SuppressWarnings("unchecked")
	default public Stream<N> adjacentTargetsStream(N node) {
		return (Stream<N>) getAdjacentTargets(node).stream();
	}
	
	default public boolean isConnected(N source, N target) {
		return adjacentTargetsStream(source).anyMatch(n -> Objects.equals(n, target));
	}
	
	@Nonnull
	default public <V> MutableMapping<N,V> createStaticNodeMapping() {
		return new MapMapping<>(new HashMap<N,V>());
	}
	
	@Nonnull
	default public <V> MutableMapping<N,V> createDynamicNodeMapping() {
		return new MapMapping<>(new HashMap<N,V>());
	}
	
	default public IndefiniteGraph<N,?> asNormalGraph() {
		return new NormalGraphView<>(this);
	}
	
	public class NormalGraphView<N,G extends IndefiniteSimpleGraph<N>>
			implements IndefiniteGraph<N,N> {
		
		protected final G simpleGraph;
		
		public NormalGraphView(G simpleGraph) {
			this.simpleGraph = simpleGraph;
		}
		
		@Override
		public Collection<? extends N> getAdjacentTargets(N node) {
			return simpleGraph.getAdjacentTargets(node);
		}
		
		@Override
		public Iterator<N> adjacentTargetsIterator(N node) {
			return simpleGraph.adjacentTargetsIterator(node);
		}
		
		@Override
		public Iterable<N> adjacentTargets(N node) {
			return simpleGraph.adjacentTargets(node);
		}
		
		@Override
		public Stream<N> adjacentTargetsStream(N node) {
			return simpleGraph.adjacentTargetsStream(node);
		}

		@Override
		public Collection<? extends N> getOutgoingEdges(N node) {
			return simpleGraph.getAdjacentTargets(node);
		}

		@Override
		public N getTarget(N edge) {
			return edge;
		}
		
		@Override
		public boolean isConnected(N source, N target) {
			return simpleGraph.isConnected(source, target);
		}
		
		@Override
		public <V> MutableMapping<N,V> createStaticNodeMapping() {
			return simpleGraph.createStaticNodeMapping();
		}
		
		@Override
		public <V> MutableMapping<N,V> createDynamicNodeMapping() {
			return simpleGraph.createDynamicNodeMapping();
		}
	}
}
