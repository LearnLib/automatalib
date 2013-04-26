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
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.util.traversal.VisitedState;

final class DepthFirstIterator<N, E> implements Iterator<N> {
	
	
	private final MutableMapping<N,VisitedState> visited;
	private final Deque<SimpleDFRecord<N,E>> dfsStack
		= new ArrayDeque<>();
	private final IndefiniteGraph<N, E> graph;
	
	public DepthFirstIterator(IndefiniteGraph<N,E> graph, Collection<? extends N> start) {
		this.graph = graph;
		this.visited = graph.createStaticNodeMapping();
		for(N startNode : start)
			dfsStack.push(new SimpleDFRecord<N,E>(startNode));
	}
	
	@Override
	public boolean hasNext() {
		return !dfsStack.isEmpty();
	}

	@Override
	public N next() {
		SimpleDFRecord<N,E> rec = dfsStack.peek();
		if(rec == null)
			throw new NoSuchElementException();
		N result;
		if(rec.start(graph)) {
			result = rec.node;
			visited.put(result, VisitedState.VISITED);
		}
		else {
			E edge = rec.nextEdge();
			result = graph.getTarget(edge);
			
			if(visited.get(result) != VisitedState.VISITED)
				dfsStack.push(new SimpleDFRecord<N,E>(result));
		}
		
		
		cleanup();
		
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * Performs a cleanup operation on the stack. After a cleanup, the stack will be either empty,
	 * or the top record on the step will be:
	 * - an unstarted record of a node that has NOT been visited, or,
	 * - a started record of a node with at least one outgoing edge to a node that has not been visited
	 */
	private void cleanup() {
		SimpleDFRecord<N,E> rec;
		while((rec = dfsStack.peek()) != null) {
			if(!rec.wasStarted()) {
				if(visited.get(rec.node) != VisitedState.VISITED)
					return;
			}
			else {
				while(rec.hasNextEdge()) {
					E edge = rec.nextEdge();
					N tgt = graph.getTarget(edge);
					if(visited.get(tgt) != VisitedState.VISITED) {
						rec.retract(edge);
						return;
					}
				}
			}
			dfsStack.pop();
		}
	}

}
