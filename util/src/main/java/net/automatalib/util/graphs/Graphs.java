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
package net.automatalib.util.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.Mappings;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.BidirectionalGraph;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.graphs.MutableGraph;
import net.automatalib.graphs.UniversalIndefiniteGraph;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.util.graphs.traversal.GraphTraversalAction;
import net.automatalib.util.graphs.traversal.GraphTraversalVisitor;


public abstract class Graphs {
	
	
	
	public static <N1,E1,N2,E2,NP2,EP2> void copy(final IndefiniteGraph<N1, E1> in,
			int limit,
			Collection<N1> initialNodes,
			final MutableGraph<N2,E2,NP2,EP2> out,
			final Mapping<? super N1,? extends NP2> npCopy,
			final Mapping<? super E1,? extends EP2> epCopy) {
		final MutableMapping<N1,N2> nodeMapping = in.createStaticNodeMapping();
		
		GraphTraversal.depthFirst(in, limit, initialNodes, new GraphTraversalVisitor<N1, E1, N2>() {
			@Override
			public GraphTraversalAction<N2> processInitial(N1 initialNode) {
				NP2 prop2 = npCopy.get(initialNode);
				N2 init2 = out.addNode(prop2);
				nodeMapping.put(initialNode, init2);
				return GraphTraversal.explore(init2);
			}

			@Override
			public boolean startExploration(N1 node, N2 data) {
				return true;
			}

			@Override
			public void finishExploration(N1 node, N2 data) {
			}

			@Override
			public GraphTraversalAction<N2> processEdge(N1 srcNode, N2 srcData,
					E1 edge) {
				EP2 prop2 = epCopy.get(edge);
				N1 tgt1 = in.getTarget(edge);
				boolean newNode = false;
				N2 tgt2 = nodeMapping.get(tgt1);
				if(tgt2 == null) {
					newNode = true;
					NP2 tgtProp2 = npCopy.get(tgt1);
					tgt2 = out.addNode(tgtProp2);
					nodeMapping.put(tgt1, tgt2);
				}
				
				out.connect(srcData, tgt2, prop2);
				
				if(newNode)
					return GraphTraversal.explore(tgt2);
				return GraphTraversal.ignore();
			}
			
		});
	}
	
	public static <N1,E1,NP1,EP1,N2,E2,NP2,EP2> void copy(final UniversalIndefiniteGraph<N1, E1, ? extends NP1, ? extends EP1> in,
			int limit,
			Collection<N1> initialNodes,
			final MutableGraph<N2,E2,? super NP2,? super EP2> out,
			final Mapping<NP1,NP2> npCopy,
			final Mapping<EP1,EP2> epCopy) {
		final MutableMapping<N1,N2> nodeMapping = in.createStaticNodeMapping();
		
		GraphTraversal.depthFirst(in, limit, initialNodes, new GraphTraversalVisitor<N1, E1, N2>() {
			@Override
			public GraphTraversalAction<N2> processInitial(N1 initialNode) {
				NP1 prop1 = in.getNodeProperties(initialNode);
				NP2 prop2 = npCopy.get(prop1);
				N2 init2 = out.addNode(prop2);
				nodeMapping.put(initialNode, init2);
				return GraphTraversal.explore(init2);
			}

			@Override
			public boolean startExploration(N1 node, N2 data) {
				return true;
			}

			@Override
			public void finishExploration(N1 node, N2 data) {
			}

			@Override
			public GraphTraversalAction<N2> processEdge(N1 srcNode, N2 srcData,
					E1 edge) {
				EP1 prop1 = in.getEdgeProperties(edge);
				EP2 prop2 = epCopy.get(prop1);
				N1 tgt1 = in.getTarget(edge);
				boolean newNode = false;
				N2 tgt2 = nodeMapping.get(tgt1);
				if(tgt2 == null) {
					newNode = true;
					NP1 tgtProp1 = in.getNodeProperties(tgt1);
					NP2 tgtProp2 = npCopy.get(tgtProp1);
					tgt2 = out.addNode(tgtProp2);
					nodeMapping.put(tgt1, tgt2);
				}
				
				out.connect(srcData, tgt2, prop2);
				
				if(newNode)
					return GraphTraversal.explore(tgt2);
				return GraphTraversal.ignore();
			}
			
		});
	}
	
	public static <N1,E1,NP,EP,N2,E2> void copy(final UniversalIndefiniteGraph<N1, E1, ? extends NP, ? extends EP> input,
			int limit,
			Collection<N1> initialNodes,
			final MutableGraph<N2, E2, NP, EP> out) {
		copy(input, limit, initialNodes, out, Mappings.<NP>identity(), Mappings.<EP>identity());
	}
			
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
	
	private Graphs() {}
}
