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
import java.util.Iterator;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.graphs.dot.EmptyDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.graphs.helpers.SimpleNodeIDs;

import com.google.common.collect.Iterators;

/**
 * The finite version of a {@link IndefiniteSimpleGraph}.
 * 
 * @author Malte Isberner
 *
 * @param <N> node type
 */
public interface SimpleGraph<N> extends IndefiniteSimpleGraph<N>, Iterable<N> {
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
	
	@SuppressWarnings("unchecked")
	default public Stream<N> nodesStream() {
		return (Stream<N>) getNodes().stream();
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
	
	default public GraphDOTHelper<N,?> getGraphDOTHelper() {
		return new EmptyDOTHelper<>();
	}
	
	default public Graph<N,?> asNormalGraph() {
		return new NormalGraphView<>(this);
	}
	
	
	public class NormalGraphView<N,G extends SimpleGraph<N>> extends IndefiniteSimpleGraph.NormalGraphView<N,G>
			implements Graph<N,N> {

		public NormalGraphView(G simpleGraph) {
			super(simpleGraph);
		}

		@Override
		public Collection<? extends N> getNodes() {
			return simpleGraph.getNodes();
		}
		
		@Override
		public Iterator<N> iterator() {
			return simpleGraph.iterator();
		}

		@Override
		public int size() {
			return simpleGraph.size();
		}
		
		@Override
		public Stream<N> nodesStream() {
			return simpleGraph.nodesStream();
		}
		
	}

}
