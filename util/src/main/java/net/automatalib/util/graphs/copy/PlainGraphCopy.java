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
package net.automatalib.util.graphs.copy;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.MutableGraph;

final class PlainGraphCopy<N1, E1, N2, E2, NP2, EP2>
		extends AbstractGraphCopy<N1, E1, N2, E2, NP2, EP2, Graph<N1,E1>> {

	private static class NodeRec<N1,N2> {
		private final N1 inNode;
		private final N2 outNode;
		
		public NodeRec(N1 inNode, N2 outNode) {
			this.inNode = inNode;
			this.outNode = outNode;
		}
	}
	
	public PlainGraphCopy(Graph<N1, E1> inGraph,
			MutableGraph<N2, E2, NP2, EP2> outGraph,
			Mapping<? super N1, ? extends NP2> npMapping,
			Mapping<? super E1, ? extends EP2> epMapping) {
		super(inGraph, outGraph, npMapping, epMapping);
	}

	@Override
	public void doCopy() {
		List<NodeRec<N1,N2>> outNodes = new ArrayList<>(inGraph.size());
		// Copy nodes
		for(N1 n1 : inGraph) {
			N2 n2 = copyNode(n1);
			outNodes.add(new NodeRec<>(n1, n2));
		}
		
		// Copy edges
		for(NodeRec<N1,N2> p : outNodes) {
			N1 n1 = p.inNode;
			N2 n2 = p.outNode;
			
			for(E1 edge : inGraph.getOutgoingEdges(n1)) {
				N1 tgt1 = inGraph.getTarget(edge);
				copyEdge(n2, edge, tgt1);
			}
		}
	}
	
}
