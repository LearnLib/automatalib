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
package net.automatalib.algorithms.graph.sssp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.algorithms.graph.GraphAlgorithms;
import net.automatalib.commons.smartcollections.BinaryHeap;
import net.automatalib.commons.smartcollections.ElementReference;
import net.automatalib.commons.smartcollections.SmartDynamicPriorityQueue;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.EdgeWeights;


/**
 * Implementation of Dijkstras algorithm for the single-source shortest path
 * problem.
 * 
 * @author Malte Isberner
 *
 * @param <N> node class
 * @param <E> edge class
 */
@ParametersAreNonnullByDefault
public class DijkstraSSSP<N,E> implements SSSPResult<N,E> {
	
	/*
	 * Internal data record
	 */
	@ParametersAreNonnullByDefault
	private static final class Record<N,E> implements Comparable<Record<N,E>> {
		@Nonnull
		public final N node;
		public float dist;
		@Nullable
		public ElementReference ref;
		@Nullable
		public E reach;
		@Nullable
		public Record<N,E> parent;
		int depth;
		
		
		public Record(N node, float dist) {
			this(node, dist, null, null);
		}
		
		public Record(N node, float dist, @Nullable E reach, @Nullable Record<N,E> parent) {
			this.node = node;
			this.dist = dist;
			this.reach = reach;
			this.parent = parent;
			this.depth = (parent != null) ? parent.depth + 1 : 0;
		}

		@Override
		public int compareTo(Record<N, E> o) {
			if(dist < o.dist)
				return -1;
			return (dist == o.dist) ? 0 : 1;
		}
	}
	
	/**
	 * Search for the shortest paths from a single source node in a graph.
	 * 
	 * @param graph the graph in which to perform the search
	 * @param init the initial (source) node
	 * @param edgeWeights the edge weights
	 * @return the single-source shortest path results
	 */
	@Nonnull
	public static <N,E> SSSPResult<N,E> findSSSP(Graph<N,E> graph, N init, EdgeWeights<E> edgeWeights) {
		DijkstraSSSP<N, E> dijkstra = new DijkstraSSSP<N, E>(graph, init, edgeWeights);
		dijkstra.findSSSP();
		return dijkstra;
	}
	
	private final Graph<N,E> graph;
	private final N init;
	private final EdgeWeights<E> edgeWeights;
	private final MutableMapping<N,Record<N,E>> records;
	
	/**
	 * Constructor.
	 * @param graph the graph in which to search for shortest paths
	 * @param init the initial node
	 * @param edgeWeights the edge weights
	 */
	public DijkstraSSSP(Graph<N,E> graph, N init, EdgeWeights<E> edgeWeights) {
		this.graph = graph;
		this.init = init;
		this.edgeWeights = edgeWeights;
		this.records = graph.createStaticNodeMapping();
	}
	
	
	/**
	 * Start the search. This method may only be invoked once.
	 */
	public void findSSSP() {
		Record<N,E> initRec = new Record<>(init, 0.0f);
		if(records.put(init, initRec) != null)
			throw new IllegalStateException("Search has already been performed!");
		
		SmartDynamicPriorityQueue<Record<N,E>> pq = BinaryHeap.create(graph.size());
		initRec.ref = pq.referencedAdd(initRec);
		
		while(!pq.isEmpty()) {
			// Remove node with minimum distance
			Record<N,E> rec = pq.extractMin();
			float dist = rec.dist;
			
			N node = rec.node;
			
			// edge scanning
			for(E edge : graph.getOutgoingEdges(node)) {
				float w = edgeWeights.getEdgeWeight(edge);
				float newDist = dist + w;
				
				N tgt = graph.getTarget(edge);
				Record<N,E> tgtRec = records.get(tgt);
				if(tgtRec == null) {
					// node has not been visited before, add a record
					// and add it to the queue
					tgtRec = new Record<>(tgt, newDist, edge, rec);
					tgtRec.ref = pq.referencedAdd(tgtRec);
					records.put(tgt, tgtRec);
				}
				else if(newDist < tgtRec.dist) {
					// using currently considered edge decreases current distance
					tgtRec.dist = newDist;
					tgtRec.reach = edge;
					tgtRec.depth = rec.depth + 1;
					tgtRec.parent = rec;
					// update it's position in the queue
					pq.keyChanged(tgtRec.ref);
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.algorithms.graph.sssp.SSSPResult#getShortestPathDistance(java.lang.Object)
	 */
	@Override
	public float getShortestPathDistance(N target) {
		Record<N,E> rec = records.get(target);
		if(rec == null)
			return GraphAlgorithms.INVALID_DISTANCE;
		return rec.dist;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.algorithms.graph.sssp.SSSPResult#getShortestPath(java.lang.Object)
	 */
	@Override
	public List<E> getShortestPath(N target) {
		Record<N,E> rec = records.get(target);
		if(rec == null)
			return null;
		
		if(rec.depth == 0)
			return Collections.emptyList();
		
		List<E> result = new ArrayList<>(rec.depth);
		
		E edge;
		while((edge = rec.reach) != null) {
			result.add(edge);
			rec = rec.parent;
		}
		
		Collections.reverse(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.algorithms.graph.sssp.SSSPResult#getInitialNode()
	 */
	@Override
	public N getInitialNode() {
		return init;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.algorithms.graph.sssp.SSSPResult#getShortestPathEdge(java.lang.Object)
	 */
	@Override
	public E getShortestPathEdge(N target) {
		Record<N,E> rec = records.get(target);
		if(rec == null)
			return null;
		return rec.reach;
	}
}
