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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.util.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.commons.util.Holder;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.util.graphs.Path.PathData;
import net.automatalib.util.graphs.traversal.DefaultGraphTraversalVisitor;
import net.automatalib.util.graphs.traversal.GraphTraversalAction;


final class FindShortestPathVisitor<N, E> extends
		DefaultGraphTraversalVisitor<N, E, Void> {
	
	private static final class Pred<N,E> {
		public final N node;
		public final E edge;
		
		public Pred(N node, E edge) {
			this.node = node;
			this.edge = edge;
		}
	}
	
	private final MutableMapping<N, Pred<N,E>> predMapping;
	private final Collection<? extends N> targetNodes;
	private N foundTarget;
	
	public FindShortestPathVisitor(IndefiniteGraph<N,E> graph, Collection<? extends N> targetNodes) {
		this.targetNodes = targetNodes;
		this.predMapping = graph.createStaticNodeMapping();
	}

	@Override
	public GraphTraversalAction processInitial(N initialNode, Holder<Void> outData) {
		predMapping.put(initialNode, new Pred<N,E>(null, null));
		if(targetNodes.contains(initialNode)) {
			this.foundTarget = initialNode;
			return GraphTraversalAction.ABORT_TRAVERSAL;
		}
		return GraphTraversalAction.EXPLORE;
	}

	@Override
	public GraphTraversalAction processEdge(N srcNode, Void srcData,
			E edge, N tgtNode, Holder<Void> outData) {
		
		if(targetNodes.contains(tgtNode)) {
			Pred<N,E> pred = new Pred<>(srcNode, edge);
			predMapping.put(tgtNode, pred);
			this.foundTarget = tgtNode;
			return GraphTraversalAction.ABORT_TRAVERSAL;
		}
		
		Pred<N,E> pred = predMapping.get(tgtNode);
		
		if(pred != null)
			return GraphTraversalAction.IGNORE;
		
		pred = new Pred<>(srcNode, edge);
		
		predMapping.put(tgtNode, pred);
		
		return GraphTraversalAction.EXPLORE;
	}
	
	public boolean wasSuccessful() {
		return (foundTarget != null);
	}
	
	public PathData<N,E> getTargetPath() {
		List<E> edges = new ArrayList<E>();
		
		N currNode = foundTarget;
		Pred<N,E> pred = predMapping.get(currNode);
		
		while(pred != null && pred.edge != null) {
			edges.add(pred.edge);
			
			currNode = pred.node;
			pred = predMapping.get(currNode);
		}
		
		Collections.reverse(edges);
		
		return new PathData<>(currNode, edges);
	}
}
