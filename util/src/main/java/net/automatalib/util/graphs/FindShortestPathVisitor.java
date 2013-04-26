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
package net.automatalib.util.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.util.graphs.traversal.GraphTraversalAction;
import net.automatalib.util.graphs.traversal.GraphTraversalVisitor;


final class FindShortestPathVisitor<N, E> implements
		GraphTraversalVisitor<N, E, Void> {
	
	private final IndefiniteGraph<N,E> graph;
	private final MutableMapping<N, Pair<N,E>> predMapping;
	private final Collection<? extends N> targetNodes;
	private N foundTarget;
	
	public FindShortestPathVisitor(IndefiniteGraph<N,E> graph, Collection<? extends N> targetNodes) {
		this.graph = graph;
		this.targetNodes = targetNodes;
		this.predMapping = graph.createStaticNodeMapping();
	}

	@Override
	public GraphTraversalAction<Void> processInitial(N initialNode) {
		predMapping.put(initialNode, Pair.<N,E>make(null, null));
		return GraphTraversal.explore();
	}

	@Override
	public boolean startExploration(N node, Void data) {
		return true;
	}

	@Override
	public void finishExploration(N node, Void data) {
	}

	@Override
	public GraphTraversalAction<Void> processEdge(N srcNode, Void srcData,
			E edge) {
		N tgtNode = graph.getTarget(edge);
		
		if(targetNodes.contains(tgtNode)) {
			Pair<N,E> pred = Pair.make(srcNode, edge);
			predMapping.put(tgtNode, pred);
			this.foundTarget = tgtNode;
			return GraphTraversal.abortTraversal();
		}
		
		Pair<N,E> pred = predMapping.get(tgtNode);
		
		if(pred != null)
			return GraphTraversal.ignore();
		
		pred = Pair.make(srcNode, edge);
		
		predMapping.put(tgtNode, pred);
		
		return GraphTraversal.explore();
	}
	
	public boolean wasSuccessful() {
		return (foundTarget != null);
	}
	
	public Pair<N,List<E>> getTargetPath() {
		List<E> path = new ArrayList<E>();
		
		N currNode = foundTarget;
		Pair<N,E> pred = predMapping.get(currNode);
		
		do {
			path.add(pred.getSecond());
			
			currNode = pred.getFirst();
			pred = predMapping.get(currNode);
		} while(pred != null && currNode != foundTarget);
		
		Collections.reverse(path);
		
		return Pair.make(currNode, path);
	}
}
