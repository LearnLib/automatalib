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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.util.graphs.traversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.util.traversal.VisitedState;

final class BreadthFirstIterator<N, E> implements Iterator<N> {
	
	private final Queue<N> bfsQueue
		= new ArrayDeque<>();
	private final MutableMapping<N,VisitedState> visited;
	private final IndefiniteGraph<N, E> graph;
	
	public BreadthFirstIterator(IndefiniteGraph<N,E> graph, Collection<? extends N> start) {
		this.graph = graph;
		this.visited = graph.createStaticNodeMapping();
		bfsQueue.addAll(start);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return !bfsQueue.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public N next() {
		N result = bfsQueue.poll();
		if(result == null)
			throw new NoSuchElementException();
		
		for(E edge : graph.getOutgoingEdges(result)) {
			N tgt = graph.getTarget(edge);
			if(visited.put(tgt, VisitedState.VISITED) != VisitedState.VISITED)
				bfsQueue.add(tgt);
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
