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

import java.util.Collection;

import net.automatalib.commons.util.Holder;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.graphs.MutableGraph;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.util.graphs.traversal.GraphTraversalAction;
import net.automatalib.util.graphs.traversal.GraphTraversalVisitor;
import net.automatalib.util.traversal.TraversalOrder;

final class TraversalGraphCopy<N1, E1, N2, E2, NP2, EP2>
		extends AbstractGraphCopy<N1, E1, N2, E2, NP2, EP2, IndefiniteGraph<N1,E1>>
		implements GraphTraversalVisitor<N1, E1, N2> {

	private final TraversalOrder traversalOrder;
	private final Collection<? extends N1> initNodes;
	private final int limit;
	
	public TraversalGraphCopy(TraversalOrder traversalOrder,
			int limit,
			IndefiniteGraph<N1, E1> inGraph,
			Collection<? extends N1> initNodes,
			MutableGraph<N2, E2, NP2, EP2> outGraph,
			Mapping<? super N1, ? extends NP2> npMapping,
			Mapping<? super E1, ? extends EP2> epMapping) {
		super(inGraph, outGraph, npMapping, epMapping);
		this.limit = limit;
		this.traversalOrder = traversalOrder;
		this.initNodes = initNodes;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.copy.AbstractGraphCopy#doCopy()
	 */
	@Override
	public void doCopy() {
		GraphTraversal.traverse(traversalOrder, inGraph, limit, initNodes, this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.GraphTraversalVisitor#processInitial(java.lang.Object, net.automatalib.util.graphs.traversal.GraphTraversalAction)
	 */
	@Override
	public GraphTraversalAction processInitial(N1 initialNode,
			Holder<N2> outData) {
		N2 n2 = copyNode(initialNode);
		outData.value = n2;
		return GraphTraversalAction.EXPLORE;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.GraphTraversalVisitor#startExploration(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean startExploration(N1 node, N2 data) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.GraphTraversalVisitor#finishExploration(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void finishExploration(N1 node, N2 data) {
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.GraphTraversalVisitor#processEdge(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, net.automatalib.util.graphs.traversal.GraphTraversalAction)
	 */
	@Override
	public GraphTraversalAction processEdge(N1 srcNode, N2 srcData, E1 edge, N1 tgtNode,
			Holder<N2> outData) {
		N2 freshTgt = copyEdgeChecked(srcData, edge, tgtNode);
		if(freshTgt != null) {
			outData.value = freshTgt;
			return GraphTraversalAction.EXPLORE;
		}
		return GraphTraversalAction.IGNORE;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.GraphTraversalVisitor#backtrackEdge(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void backtrackEdge(N1 srcNode, N2 srcData, E1 edge, N1 tgtNode,
			N2 tgtData) {
	}

}
