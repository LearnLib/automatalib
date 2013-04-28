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
package net.automatalib.algorithms.graph.scc;

import java.util.List;

import net.automatalib.graphs.Graph;
import net.automatalib.util.graphs.traversal.GraphTraversal;

/**
 * Algorithms for finding strongly-connected components (SCCs) in a graph.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public abstract class SCCs {

	private SCCs() {
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
	 */
	public static <N,E> void findSCCs(Graph<N,E> graph, SCCListener<N> listener) {
		TarjanSCCVisitor<N, E> vis = new TarjanSCCVisitor<>(graph, listener);
		for(N node : graph) {
			if(!vis.hasVisited(node))
				GraphTraversal.depthFirst(graph, node, vis);
		}
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
	 */
	public static <N,E> List<List<N>> collectSCCs(Graph<N,E> graph) {
		SCCCollector<N> coll = new SCCCollector<>();
		findSCCs(graph, coll);
		return coll.getSCCList();
	}

}
