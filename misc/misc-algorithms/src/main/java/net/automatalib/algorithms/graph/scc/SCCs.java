/*
 * Copyright (C) 2013-2014 TU Dortmund This file is part of AutomataLib,
 * http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); modify it under
 * you may not use this file except in compliance with the License. modify it under
 * You may obtain a copy of the License at modify it under
 *  modify it under
 *     http://www.apache.org/licenses/LICENSE-2.0 modify it under
 *  modify it under
 * Unless required by applicable law or agreed to in writing, software modify it under
 * distributed under the License is distributed on an "AS IS" BASIS, modify it under
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. modify it under
 * See the License for the specific language governing permissions and modify it under
 * limitations under the License. modify it under
 */
package net.automatalib.algorithms.graph.scc;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.graphs.Graph;
import net.automatalib.util.graphs.traversal.GraphTraversal;

/**
 * Algorithms for finding strongly-connected components (SCCs) in a graph.
 * 
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public abstract class SCCs {

	private SCCs() {
	}

	/**
	 * Find all strongly-connected components in a graph. When a new SCC is
	 * found, the {@link SCCListener#foundSCC(java.util.Collection)} method is
	 * invoked. The listener object may hence not be null.
	 * <p>
	 * Tarjan's algorithm is used for realizing the SCC search.
	 * 
	 * @param graph
	 *            the graph
	 * @param listener
	 *            the SCC listener
	 * 
	 * @see TarjanSCCVisitor
	 */
	public static <N, E> void findSCCs(Graph<N, E> graph,
			SCCListener<N> listener) {
		TarjanSCCVisitor<N, E> vis = new TarjanSCCVisitor<>(graph, listener);
		for (N node : graph) {
			if (!vis.hasVisited(node))
				GraphTraversal.depthFirst(graph, node, vis);
		}
	}

	/**
	 * Collects all strongly-connected components in a graph. The SCCs are
	 * returned as a list of lists.
	 * <p>
	 * Tarjan's algorithm is used for realizing the SCC search.
	 * 
	 * @param graph
	 *            the graph
	 * @return a list of all SCCs, each represented as a list of its nodes
	 * 
	 * @see TarjanSCCVisitor
	 */
	@Nonnull
	public static <N, E> List<List<N>> collectSCCs(Graph<N, E> graph) {
		SCCCollector<N> coll = new SCCCollector<>();
		findSCCs(graph, coll);
		return coll.getSCCList();
	}

}
