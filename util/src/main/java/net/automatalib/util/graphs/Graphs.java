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
package net.automatalib.util.graphs;

import java.util.ArrayList;
import java.util.Collection;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.BidirectionalGraph;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.graphs.UniversalIndefiniteGraph;


public abstract class Graphs {
			
	public static <N,E> Mapping<N,? extends Collection<? extends E>> incomingEdges(final Graph<N,E> graph) {
		if(graph instanceof BidirectionalGraph)
			return new InEdgesMapping<N,E>((BidirectionalGraph<N,E>)graph);
		
		MutableMapping<N,Collection<E>> inEdgesMapping
			= graph.createStaticNodeMapping();
		
		for(N node : graph) {
			Collection<? extends E> outEdges = graph.getOutgoingEdges(node);
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

	public static <N,E> Path<N,E> findShortestPath(final IndefiniteGraph<N, E> graph, int limit, N start, Collection<? extends N> targets) {
		return ShortestPaths.shortestPath(graph, start, limit, targets);
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
