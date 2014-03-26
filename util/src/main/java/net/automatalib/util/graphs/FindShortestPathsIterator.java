/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.util.graphs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;

final class FindShortestPathsIterator<N, E> extends AbstractIterator<Path<N,E>>{
	
	private static final class Pred<N,E> {
		public final N node;
		public final E edge;
		
		public Pred(N node, E edge) {
			this.node = node;
			this.edge = edge;
		}
	}

	private final Queue<N> bfsQueue = new ArrayDeque<>();
	private final IndefiniteGraph<N, E> graph;
	private final MutableMapping<N, Pred<N,E>> preds;
	private final Predicate<? super N> targetPred;
	private final int limit;
	private int count;

	public FindShortestPathsIterator(IndefiniteGraph<N, E> graph, Collection<? extends N> start, int limit, Predicate<? super N> targetPred) {
		this.graph = graph;
		this.preds = graph.createStaticNodeMapping();
		this.limit = limit;
		this.targetPred = targetPred;
		
		for(N startNode : start) {
			preds.put(startNode, new Pred<N,E>(null, null));
			bfsQueue.add(startNode);
		}
	}

	@Override
	protected Path<N, E> computeNext() {
		while(!bfsQueue.isEmpty()) {
			if(count++ == limit) {
				return endOfData();
			}
			N curr = bfsQueue.poll();
			if(targetPred.apply(curr)) {
				return makePath(curr);
			}
			
			for(E edge : graph.getOutgoingEdges(curr)) {
				N tgt = graph.getTarget(edge);
				Pred<N,E> pred = preds.get(tgt);
				if(pred == null) {
					preds.put(tgt, new Pred<>(curr, edge));
					bfsQueue.add(tgt);
				}
			}
		}
		
		return endOfData();
	}
	
	private Path<N,E> makePath(N target) {
		List<E> edges = new ArrayList<>();
		
		N currNode = target;
		Pred<N,E> pred = preds.get(currNode);
		
		while(pred != null && pred.edge != null) {
			edges.add(pred.edge);
			
			currNode = pred.node;
			pred = preds.get(currNode);
		}
		
		Collections.reverse(edges);
		
		return new Path<>(graph, currNode, edges);
	}
	
	
}
