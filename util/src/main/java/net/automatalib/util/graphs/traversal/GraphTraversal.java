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

import net.automatalib.commons.util.Holder;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.util.graphs.traversal.DFRecord.LastEdge;
import net.automatalib.util.traversal.TraversalOrder;


public abstract class GraphTraversal {
	
	
	public static <N,E,D>
	boolean traverse(TraversalOrder order,
			IndefiniteGraph<N,E> graph, 
			int limit, 
			Collection<? extends N> initialNodes, 
			GraphTraversalVisitor<N, E, D> vis) {
		switch(order) {
		case BREADTH_FIRST:
			return breadthFirst(graph, limit, initialNodes, vis);
		case DEPTH_FIRST:
			return depthFirst(graph, limit, initialNodes, vis);
		default:
			throw new IllegalArgumentException("Unknown traversal order " + order);
		}
	}
	
	public static <N,E,D>
	boolean traverse(TraversalOrder order,
			IndefiniteGraph<N,E> graph,
			int limit,
			N initialNode,
			GraphTraversalVisitor<N,E,D> vis) {
		return traverse(order, graph, limit, Collections.singleton(initialNode), vis);
	}
	
	public static <N,E,D>
	boolean traverse(TraversalOrder order,
			IndefiniteGraph<N,E> graph,
			N initialNode,
			GraphTraversalVisitor<N,E,D> vis) {
		return traverse(order, graph, -1, Collections.singleton(initialNode), vis);
	}
	
	public static <N,E,D>
	boolean traverse(TraversalOrder order,
			IndefiniteGraph<N,E> graph,
			Collection<? extends N> initialNodes,
			GraphTraversalVisitor<N,E,D> vis) {
		return traverse(order, graph, -1, initialNodes, vis);
	}
	
	
	public static <N,E,D>
	boolean breadthFirst(IndefiniteGraph<N, E> graph, int limit, Collection<? extends N> initialNodes, GraphTraversalVisitor<N, E, D> vis) {
		
		Queue<BFRecord<N,D>> bfsQueue = new ArrayDeque<BFRecord<N,D>>();
		
		// setting the following to false means that the traversal had to be aborted
		// due to reaching the limit
		boolean complete = true;
		int nodeCount = 0;
		
		Holder<D> dataHolder = new Holder<>();
		
		for(N init : initialNodes) {
			dataHolder.value = null;
			GraphTraversalAction act = vis.processInitial(init, dataHolder);
				
			switch(act) {
			case IGNORE:
			case ABORT_NODE:
				continue;
			case ABORT_TRAVERSAL:
				return complete;
			case EXPLORE:
				if(nodeCount != limit) { // not equals will always be true for negative limit values
					bfsQueue.offer(new BFRecord<N,D>(init, dataHolder.value));
					nodeCount++;
				}
				else
					complete = false;
				break;
			}
		}
		
		
bfs_loop:
		while(!bfsQueue.isEmpty()) {
			BFRecord<N,D> current = bfsQueue.poll();
			
			N currNode = current.node;
			D currData = current.data;
			
			if(!vis.startExploration(currNode, currData))
				continue;
			
			Collection<? extends E> edges = graph.getOutgoingEdges(currNode);

			
			for(E edge : edges) {
				
				N tgtNode = graph.getTarget(edge);
				
				dataHolder.value = null;
				GraphTraversalAction act = vis.processEdge(currNode, currData, edge, tgtNode, dataHolder);

				
				switch(act) {
				case IGNORE:
					continue;
				case ABORT_NODE:
					continue bfs_loop;
				case ABORT_TRAVERSAL:
					return complete;
				case EXPLORE:
					if(nodeCount != limit) { // not equals will always be true for negative limit values
						bfsQueue.offer(new BFRecord<N,D>(tgtNode, dataHolder.value));
						nodeCount++;
					}
					else
						complete = false;
				}
			}
			
			vis.finishExploration(currNode, currData);
		}
		
		return complete;
	}
	
	public static <N,E,D>
	boolean breadthFirst(IndefiniteGraph<N,E> graph, int limit, N initialNode, GraphTraversalVisitor<N, E, D> visitor) {
		return breadthFirst(graph, limit, Collections.singleton(initialNode), visitor);
	}
	
	public static <N,E,D>
	boolean breadthFirst(IndefiniteGraph<N,E> graph, Collection<? extends N> initialNodes, GraphTraversalVisitor<N, E, D> visitor) {
		return breadthFirst(graph, -1, initialNodes, visitor);
	}
	
	public static <N,E,D>
	boolean breadthFirst(IndefiniteGraph<N,E> graph, N initialNode, GraphTraversalVisitor<N, E, D> visitor) {
		return breadthFirst(graph, -1, Collections.singleton(initialNode), visitor);
	}
	
	
	
	
	public static <N,E,D>
	boolean depthFirst(IndefiniteGraph<N,E> graph, int limit, Collection<? extends N> initialNodes,
			GraphTraversalVisitor<N, E, D> vis) {
		
		// setting the following to false means that the traversal had to be aborted
		// due to reaching the limit
		boolean complete = true;
		
		int nodeCount = 0;
			
		
		Deque<DFRecord<N,E,D>> dfsStack
			= new ArrayDeque<DFRecord<N,E,D>>();
		
		Holder<D> dataHolder = new Holder<>();
		
		for(N init : initialNodes) {
			
			dataHolder.value = null;
			GraphTraversalAction act = vis.processInitial(init, dataHolder);
			
			switch(act) {
			case IGNORE:
			case ABORT_NODE:
				continue;
			case ABORT_TRAVERSAL:
				return complete;
			case EXPLORE:
				if(nodeCount != limit) {
					dfsStack.push(new DFRecord<N,E,D>(init, dataHolder.value));
					nodeCount++;
				}
				else
					complete = false;
				break;
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
			
			LastEdge<E,N,D> lastEdge = current.getLastEdge();
			if(lastEdge != null) {
				vis.backtrackEdge(currNode, currData, lastEdge.edge,
						lastEdge.node, lastEdge.data);
			}
			
			if(!current.hasNextEdge()) {
				dfsStack.pop();
				vis.finishExploration(currNode, currData);
				continue;
			}
			
			E edge = current.nextEdge();
			
			N tgt = graph.getTarget(edge);
			
			GraphTraversalAction act = vis.processEdge(currNode, currData, edge, tgt, dataHolder);
			
			switch(act) {
			case IGNORE:
				continue;
			case ABORT_NODE:
				dfsStack.pop();
				continue;
			case ABORT_TRAVERSAL:
				return complete;
			case EXPLORE:
				if(nodeCount != limit) {
					D data = dataHolder.value;
					current.setLastEdge(edge, tgt, data);
					dfsStack.push(new DFRecord<N,E,D>(tgt, data));
					nodeCount++;
				}
				else
					complete = false;
				break;
			}
		}
		
		return complete;
	}
	
	public static <N,E,D>
	boolean depthFirst(IndefiniteGraph<N,E> graph, N initNode,
			GraphTraversalVisitor<N, E, D> vis) {
		return depthFirst(graph, -1, initNode, vis);
	}
	
	public static <N,E,D>
	boolean depthFirst(IndefiniteGraph<N,E> graph, int limit, N initNode,
			GraphTraversalVisitor<N, E, D> vis) {
		return depthFirst(graph, Collections.singleton(initNode), vis);
	}
	
	public static <N,E,D>
	boolean depthFirst(IndefiniteGraph<N,E> graph, Collection<? extends N> initialNodes,
			GraphTraversalVisitor<N, E, D> vis) {
		return depthFirst(graph, -1, initialNodes, vis);
	}
	
	
	
	
	
	public static <N,E,D>
	boolean dfs(IndefiniteGraph<N, E> graph, int limit, Collection<? extends N> initialNodes, DFSVisitor<? super N, ? super E, D> visitor) {
		GraphTraversalVisitor<N, E, DFSData<D>> traversalVisitor
			= new DFSTraversalVisitor<N, E, D>(graph, visitor);
		return depthFirst(graph, limit, initialNodes, traversalVisitor);
	}
	
	public static <N,E,D>
	boolean dfs(IndefiniteGraph<N, E> graph, N initialNode, DFSVisitor<? super N, ? super E, D> visitor) {
		return dfs(graph, -1, Collections.singleton(initialNode), visitor);
	}
	
	public static <N,E,D>
	boolean dfs(IndefiniteGraph<N, E> graph, Collection<? extends N> initialNodes, DFSVisitor<? super N, ? super E, D> visitor) {
		return dfs(graph, -1, initialNodes, visitor);
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
