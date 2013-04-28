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
package net.automatalib.util.graphs.traversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

import net.automatalib.commons.util.Triple;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.util.graphs.traversal.GraphTraversalAction.Type;


public abstract class GraphTraversal {
	
	private static final GraphTraversalAction<Object> IGNORE
		= new GraphTraversalAction<>(Type.IGNORE);
	
	private static final GraphTraversalAction<Object> ABORT_NODE
		= new GraphTraversalAction<>(Type.ABORT_NODE);
	
	private static final GraphTraversalAction<Object> ABORT_TRAVERSAL
		= new GraphTraversalAction<>(Type.ABORT_TRAVERSAL);
	
	private static final GraphTraversalAction<Object> DEFAULT_EXPLORE_ACTION
		= new GraphTraversalAction<>(Type.EXPLORE);
	
	@SuppressWarnings("unchecked")
	public static final <D> GraphTraversalAction<D> ignore() {
		return (GraphTraversalAction<D>)IGNORE;
	}
	
	@SuppressWarnings("unchecked")
	public static final <D> GraphTraversalAction<D> abortNode() {
		return (GraphTraversalAction<D>)ABORT_NODE;
	}
	
	@SuppressWarnings("unchecked")
	public static final <D> GraphTraversalAction<D> abortTraversal() {
		return (GraphTraversalAction<D>)ABORT_TRAVERSAL;
	}
	
	public static final <D> GraphTraversalAction<D> explore(D data) {
		if(data == null)
			return explore();
		return new GraphTraversalAction<D>(Type.EXPLORE, data);
	}
	
	@SuppressWarnings("unchecked")
	public static final <D> GraphTraversalAction<D> explore() {
		return (GraphTraversalAction<D>)DEFAULT_EXPLORE_ACTION;
	}
	
	public static <N,E,D> void breadthFirst(IndefiniteGraph<N,E> graph, int limit, N initialNode, GraphTraversalVisitor<N, E, D> visitor) {
		breadthFirst(graph, limit, Collections.singleton(initialNode), visitor);
	}
	
	public static <N,E,D> void breadthFirst(IndefiniteGraph<N,E> graph, Collection<N> initialNodes, GraphTraversalVisitor<N, E, D> visitor) {
		breadthFirst(graph, -1, initialNodes, visitor);
	}
	
	public static <N,E,D> void breadthFirst(IndefiniteGraph<N,E> graph, N initialNode, GraphTraversalVisitor<N, E, D> visitor) {
		breadthFirst(graph, -1, Collections.singleton(initialNode), visitor);
	}
	
	public static <N,E,D> void breadthFirst(IndefiniteGraph<N, E> graph, int limit, Collection<N> initialNodes, GraphTraversalVisitor<N, E, D> vis) {
		
		Queue<BFRecord<N,D>> bfsQueue = new ArrayDeque<BFRecord<N,D>>();
		
		int nodeCount = 0;
		
		for(N init : initialNodes) {
			GraphTraversalAction<D> act = vis.processInitial(init);
			switch(act.type) {
			case IGNORE:
			case ABORT_NODE:
				continue;
			case ABORT_TRAVERSAL:
				return;
			case EXPLORE:
			}
			D data = act.data;
			
			if(nodeCount != limit) { // not equals will always be true for negative limit values
				bfsQueue.offer(new BFRecord<N,D>(init, data));
				nodeCount++;
			}
		}
		
		
bfs_loop:
		while(!bfsQueue.isEmpty()) {
			BFRecord<N,D> current = bfsQueue.poll();
			
			N currNode = current.node;
			D currData = current.data;
			if(!vis.startExploration(currNode, currData))
				continue;
			
			Collection<E> edges = graph.getOutgoingEdges(currNode);
			
			if(edges == null)
				continue;
			
			for(E edge : edges) {
				
				N tgtNode = graph.getTarget(edge);
				GraphTraversalAction<D> act 
					= vis.processEdge(currNode, currData, edge, tgtNode);
				
				switch(act.type) {
				case IGNORE:
					continue;
				case ABORT_NODE:
					continue bfs_loop;
				case ABORT_TRAVERSAL:
					return;
				case EXPLORE:
				}
				
				D data = act.data;
				if(nodeCount != limit) { // not equals will always be true for negative limit values
					bfsQueue.offer(new BFRecord<N,D>(tgtNode, data));
					nodeCount++;
				}
			}
			
			vis.finishExploration(currNode, currData);
		}
	}
	
	public static <N,E,D> void depthFirst(IndefiniteGraph<N,E> graph, N initNode,
			GraphTraversalVisitor<N, E, D> vis) {
		depthFirst(graph, -1, initNode, vis);
	}
	
	public static <N,E,D> void depthFirst(IndefiniteGraph<N,E> graph, int limit, N initNode,
			GraphTraversalVisitor<N, E, D> vis) {
		depthFirst(graph, Collections.singleton(initNode), vis);
	}
	
	public static <N,E,D> void depthFirst(IndefiniteGraph<N,E> graph, Collection<? extends N> initialNodes,
			GraphTraversalVisitor<N, E, D> vis) {
		depthFirst(graph, -1, initialNodes, vis);
	}
	
	public static <N,E,D> void depthFirst(IndefiniteGraph<N,E> graph, int limit, Collection<? extends N> initialNodes,
			GraphTraversalVisitor<N, E, D> vis) {
		
		if(limit < 0)
			limit = Integer.MAX_VALUE;
		
		int nodeCount = 0;
			
		
		Deque<DFRecord<N,E,D>> dfsStack
			= new ArrayDeque<DFRecord<N,E,D>>();
		
		for(N init : initialNodes) {
			GraphTraversalAction<D> act = vis.processInitial(init);
			
			switch(act.type) {
			case IGNORE:
			case ABORT_NODE:
				continue;
			case ABORT_TRAVERSAL:
				return;
			case EXPLORE:
			}
			
			D data = act.data;
			if(nodeCount < limit) {
				dfsStack.push(new DFRecord<N,E,D>(init, data));
				nodeCount++;
			}
		}
		
		
		while(!dfsStack.isEmpty()) {
			DFRecord<N,E,D> current = dfsStack.peek();
			
			N currNode = current.node;
			D currData = current.data;
			
			if(current.start(graph)) {
				if(!vis.startExploration(currNode, currData)) {
					dfsStack.pop();
					continue;
				}
			}
			
			Triple<E,N,D> lastEdge = current.getLastEdge();
			if(lastEdge != null) {
				vis.backtrackEdge(currNode, currData, lastEdge.getFirst(),
						lastEdge.getSecond(), lastEdge.getThird());
			}
			
			if(!current.hasNextEdge()) {
				dfsStack.pop();
				vis.finishExploration(currNode, currData);
				continue;
			}
			
			E edge = current.nextEdge();
			
			N tgt = graph.getTarget(edge);
			GraphTraversalAction<D> act = vis.processEdge(currNode, currData, edge, tgt);
			
			switch(act.type) {
			case IGNORE:
				continue;
			case ABORT_NODE:
				dfsStack.pop();
				continue;
			case ABORT_TRAVERSAL:
				return;
			case EXPLORE:
			}
			
			D data = act.data;
			
			if(nodeCount < limit) {
				current.setLastEdge(edge, tgt, data);
				dfsStack.push(new DFRecord<N,E,D>(tgt, data));
				nodeCount++;
			}
		}
	}
	
	
	public static <N,E,D> void dfs(IndefiniteGraph<N, E> graph, int limit, Collection<? extends N> initialNodes, DFSVisitor<? super N, ? super E, D> visitor) {
		GraphTraversalVisitor<N, E, DFSData<D>> traversalVisitor
			= new DFSTraversalVisitor<N, E, D>(graph, visitor);
		depthFirst(graph, limit, initialNodes, traversalVisitor);
	}
	
	public static <N,E,D> void dfs(IndefiniteGraph<N, E> graph, N initialNode, DFSVisitor<? super N, ? super E, D> visitor) {
		dfs(graph, -1, Collections.singleton(initialNode), visitor);
	}
	
	public static <N,E,D> void dfs(IndefiniteGraph<N, E> graph, Collection<? extends N> initialNodes, DFSVisitor<? super N, ? super E, D> visitor) {
		dfs(graph, -1, initialNodes, visitor);
	}
	
	
	public static <N,E> Iterator<N> bfIterator(IndefiniteGraph<N,E> graph, Collection<? extends N> start) {
		return new BreadthFirstIterator<>(graph, start);
	}
	
	public static <N,E> Iterable<N> breadthFirstOrder(
			final IndefiniteGraph<N,E> graph,
			final Collection<? extends N> start) {
		
		return new Iterable<N>() {
			@Override
			public Iterator<N> iterator() {
				return bfIterator(graph, start);
			}
		};
	}
	
	public static <N,E> Iterator<N> dfIterator(IndefiniteGraph<N,E> graph, Collection<? extends N> start) {
		return new DepthFirstIterator<>(graph, start);
	}
	
	public static <N,E> Iterable<N> depthFirstOrder(
			final IndefiniteGraph<N,E> graph,
			final Collection<? extends N> start) {
		
		return new Iterable<N>() {
			@Override
			public Iterator<N> iterator() {
				return dfIterator(graph, start);
			}
		};
	}
	
	private GraphTraversal() {} // prevent inheritance

}
