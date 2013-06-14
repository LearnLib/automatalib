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
package net.automatalib.util.graphs.traversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.util.traversal.VisitedState;

import com.google.common.collect.AbstractIterator;

final class DepthFirstIterator<N, E> extends AbstractIterator<N> {
	
	
	private final MutableMapping<N,VisitedState> visited;
	private final Deque<SimpleDFRecord<N,E>> dfsStack
		= new ArrayDeque<>();
	private final IndefiniteGraph<N, E> graph;
	
	public DepthFirstIterator(IndefiniteGraph<N,E> graph, Collection<? extends N> start) {
		this.graph = graph;
		this.visited = graph.createStaticNodeMapping();
		for(N startNode : start) {
			dfsStack.push(new SimpleDFRecord<N,E>(startNode));
		}
	}
	
	@Override
	protected N computeNext() {
		SimpleDFRecord<N, E> rec;
		while((rec = dfsStack.peek()) != null) {
			if(!rec.wasStarted()) {
				visited.put(rec.node, VisitedState.VISITED);
				rec.start(graph);
				return rec.node;
			}
			else if(rec.hasNextEdge()) {
				E edge = rec.nextEdge();
				N tgt = graph.getTarget(edge);
				if(visited.get(tgt) != VisitedState.VISITED) {
					dfsStack.push(new SimpleDFRecord<N,E>(tgt));
				}
			}
			else {
				dfsStack.pop();
			}
		}
		return endOfData();
	}

}
