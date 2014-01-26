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
package net.automatalib.algorithms.graph;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.algorithms.graph.apsp.APSPResult;
import net.automatalib.algorithms.graph.apsp.FloydWarshallAPSP;
import net.automatalib.algorithms.graph.scc.SCCListener;
import net.automatalib.algorithms.graph.scc.SCCs;
import net.automatalib.algorithms.graph.scc.TarjanSCCVisitor;
import net.automatalib.algorithms.graph.sssp.DijkstraSSSP;
import net.automatalib.algorithms.graph.sssp.SSSPResult;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.EdgeWeights;

/**
 * Convenience entry points and helper methods for various graph algorithms.
 * 
 * @author Malte Isberner
 *
 */
@ParametersAreNonnullByDefault
public class GraphAlgorithms {

	/**
	 * Float value to signal that no valid distance is returned (e.g., when attempting
	 * to retrieve the length of a path that does not exist).
	 */
	public static final float INVALID_DISTANCE = Float.NEGATIVE_INFINITY;
	
	
	/**
	 * Converts a list of edges into a corresponding list of nodes. Note that the
	 * list of nodes is always one larger than the respective list of edges.
	 * 
	 * @param edgeList the list of edges
	 * @param graph the graph
	 * @param init the initial node
	 * @return the node list corresponding to the given edge list.
	 */
	public static <N,E> List<N> toNodeList(List<E> edgeList, Graph<N,E> graph, N init) {
		List<N> result = new ArrayList<>(edgeList.size() + 1);
		result.add(init);
		
		for(E edge : edgeList) {
			N tgt = graph.getTarget(edge);
			result.add(tgt);
		}
		
		return result;
	}
	
	
	/**
	 * Computes the shortest paths between all pairs of nodes in a graph, using the
	 * Floyd-Warshall dynamic programming algorithm. Note that the result is only correct
	 * if the graph contains no cycles with negative edge weight sums.
	 * @param graph the graph
	 * 
	 * @param edgeWeights the edge weights 
	 * @return the all pairs shortest paths result
	 * @see FloydWarshallAPSP
	 */
	public static <N,E> APSPResult<N,E> findAPSP(Graph<N,E> graph, EdgeWeights<E> edgeWeights) {
		return FloydWarshallAPSP.findAPSP(graph, edgeWeights);
	}
	
	/**
	 * Computes the shortest paths between a single source node and all other nodes in a graph,
	 * using Dijkstra's algorithm. Note that the result is only correct if the graph contains
	 * no edges with negative weights.
	 * 
	 * @param graph the graph
	 * @param init the source node
	 * @param edgeWeights the edge weights
	 * @return the single-source shortest paths result
	 * @see DijkstraSSSP
	 */
	public static <N,E> SSSPResult<N,E> findSSSP(Graph<N,E> graph, N init, EdgeWeights<E> edgeWeights) {
		return DijkstraSSSP.findSSSP(graph, init, edgeWeights);
	}

	/**
	 * Collects all strongly-connected components in a graph. The SCCs are returned as
	 * a list of lists.
	 * <p>
	 * Tarjan's algorithm is used for realizing the SCC search.
	 * 
	 * @param graph the graph
	 * @return a list of all SCCs, each represented as a list of its nodes
	 * 
	 * @see TarjanSCCVisitor
	 * @see SCCs
	 */
	public static <N,E> List<List<N>> collectSCCs(Graph<N,E> graph) {
		return SCCs.collectSCCs(graph);
	}
	
	/**
	 * Find all strongly-connected components in a graph. When a new SCC is found, the
	 * {@link SCCListener#foundSCC(java.util.Collection)} method is invoked. The listener
	 * object may hence not be null.
	 * <p>
	 * Tarjan's algorithm is used for realizing the SCC search.
	 * 
	 * @param graph the graph
	 * @param listener the SCC listener
	 * 
	 * @see TarjanSCCVisitor
	 * @see SCCs
	 */
	public static <N,E> void findSCCs(Graph<N,E> graph, SCCListener<N> sccListener) {
		SCCs.findSCCs(graph, sccListener);
	}
}
