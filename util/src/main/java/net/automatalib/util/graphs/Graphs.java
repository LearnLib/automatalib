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

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.BidirectionalGraph;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.graphs.UniversalIndefiniteGraph;
import net.automatalib.util.graphs.traversal.GraphTraversal;


public abstract class Graphs {
	
			
	public static <N,E> Mapping<N,Collection<E>> incomingEdges(final Graph<N,E> graph) {
		if(graph instanceof BidirectionalGraph)
			return new InEdgesMapping<N,E>((BidirectionalGraph<N,E>)graph);
		
		MutableMapping<N,Collection<E>> inEdgesMapping
			= graph.createStaticNodeMapping();
		
		for(N node : graph) {
			Collection<E> outEdges = graph.getOutgoingEdges(node);
			if(outEdges == null)
				continue;
			for(E e : outEdges) {
				N tgt = graph.getTarget(e);
				Collection<E> inEdges = inEdgesMapping.get(tgt);
				if(inEdges == null) {
					inEdges = new ArrayList<E>();
					inEdgesMapping.put(tgt, inEdges);
				}
				inEdges.add(e);
			}
		}
		
		return inEdgesMapping;
	}
	
	public static <N,E> List<E> findShortestPath(final IndefiniteGraph<N, E> graph, int limit, N start, Collection<? extends N> targets) {
		FindShortestPathVisitor<N, E> vis = new FindShortestPathVisitor<N, E>(graph, targets);
		
		GraphTraversal.breadthFirst(graph, limit, Collections.singleton(start), vis);
		
		if(!vis.wasSuccessful())
			return null;
		
		return vis.getTargetPath().getSecond();
	}
	
	
	public static <N,NP> Mapping<N,NP> nodeProperties(final UniversalIndefiniteGraph<N, ?, NP, ?> graph) {
		return new Mapping<N,NP>() {
			@Override
			public NP get(N elem) {
				return graph.getNodeProperty(elem);
			}
		};
	}
	
	public static <E,EP> Mapping<E,EP> edgeProperties(final UniversalIndefiniteGraph<?, E, ?, EP> graph) {
		return new Mapping<E,EP>() {
			@Override
			public EP get(E elem) {
				return graph.getEdgeProperty(elem);
			}
		};
	}
	
	private Graphs() {}
}
