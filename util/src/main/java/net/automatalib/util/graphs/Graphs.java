/* Copyright (C) 2013-2015 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.util.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.BidirectionalGraph;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.graphs.UniversalIndefiniteGraph;


public abstract class Graphs {
			
	public static <N,E> Mapping<N,? extends Collection<? extends E>> incomingEdges(final Graph<N,E> graph) {
		if(graph instanceof BidirectionalGraph) {
			final BidirectionalGraph<N,E> bdGraph = (BidirectionalGraph<N, E>)graph;
			return n -> bdGraph.getIncomingEdges(n);
		}
		
		MutableMapping<N,Collection<E>> inEdgesMapping
			= graph.createStaticNodeMapping();
		
		for(N node : graph) {
			Collection<? extends E> outEdges = graph.getOutgoingEdges(node);
			for(E e : outEdges) {
				N tgt = graph.getTarget(e);
				Collection<E> inEdges = inEdgesMapping.get(tgt);
				if(inEdges == null) {
					inEdges = new ArrayList<>();
					inEdgesMapping.put(tgt, inEdges);
				}
				inEdges.add(e);
			}
		}
		
		return inEdgesMapping;
	}

	public static <N,E> Path<N,E> findShortestPath(final IndefiniteGraph<N, E> graph, int limit, N start, Collection<? extends N> targets) {
		return ShortestPaths.shortestPath(graph, start, limit, targets);
	}
	
	public static <N,E> Path<N,E> findShortestPath(IndefiniteGraph<N, E> graph, int limit, N start, Predicate<? super N> targetPred) {
		return ShortestPaths.shortestPath(graph, start, limit, targetPred);
	}
	
	
	
	@Deprecated
	public static <N,NP> Mapping<N,NP> nodeProperties(final UniversalIndefiniteGraph<N, ?, NP, ?> graph) {
		return n -> graph.getNodeProperty(n);
	}
	
	@Deprecated
	public static <E,EP> Mapping<E,EP> edgeProperties(final UniversalIndefiniteGraph<?, E, ?, EP> graph) {
		return e -> graph.getEdgeProperty(e);
	}
	
	private Graphs() {}
}
