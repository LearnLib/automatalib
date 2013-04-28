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
package net.automatalib.algorithms.graph.sssp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <N> node class
 * @param <E> edge class
 */
public class DijkstraSSSP<N,E> implements SSSPResult<N,E> {
	
	public static final float INVALID_DISTANCE = -1.0f;

	private static final class Record<N,E> implements Comparable<Record<N,E>> {
		public final N node;
		public float dist;
		public ElementReference ref;
		public E reach;
		public Record<N,E> parent;
		int depth;
		
		
		public Record(N node, float dist) {
			this(node, dist, null, null);
		}
		
		public Record(N node, float dist, E reach, Record<N,E> parent) {
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
	
	public static <N,E> SSSPResult<N,E> findSSSP(Graph<N,E> graph, N init, EdgeWeights<E> edgeWeights) {
		DijkstraSSSP<N, E> dijkstra = new DijkstraSSSP<N, E>(graph, init, edgeWeights);
		dijkstra.findSSSP();
		return dijkstra;
	}
	
	private final Graph<N,E> graph;
	private final N init;
	private final EdgeWeights<E> edgeWeights;
	private final MutableMapping<N,Record<N,E>> records;
	
	public DijkstraSSSP(Graph<N,E> graph, N init, EdgeWeights<E> edgeWeights) {
		this.graph = graph;
		this.init = init;
		this.edgeWeights = edgeWeights;
		this.records = graph.createStaticNodeMapping();
	}
	
	
	public void findSSSP() {
		SmartDynamicPriorityQueue<Record<N,E>> pq = BinaryHeap.create(graph.size());
		
		Record<N,E> initRec = new Record<>(init, 0.0f);
		initRec.ref = pq.referencedAdd(initRec);
		
		while(!pq.isEmpty()) {
			Record<N,E> rec = pq.extractMin();
			float dist = rec.dist;
			
			N node = rec.node;
			for(E edge : graph.getOutgoingEdges(node)) {
				float w = edgeWeights.getEdgeWeight(edge);
				float newDist = dist + w;
				
				N tgt = graph.getTarget(edge);
				Record<N,E> tgtRec = records.get(tgt);
				if(tgtRec == null) {
					tgtRec = new Record<>(tgt, newDist, edge, rec);
					tgtRec.ref = pq.referencedAdd(tgtRec);
					records.put(tgt, tgtRec);
				}
				else if(newDist < tgtRec.dist) {
					tgtRec.dist = newDist;
					tgtRec.reach = edge;
					tgtRec.depth = rec.depth + 1;
					tgtRec.parent = rec;
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
}
