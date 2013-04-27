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
package net.automatalib.graphs.base.compact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.graphs.BidirectionalGraph;

public class CompactBidiGraph<NP, EP> extends
		AbstractCompactGraph<CompactBidiEdge<EP>, NP, EP> implements BidirectionalGraph<Integer, CompactBidiEdge<EP>> {
	
	private final ResizingObjectArray inEdges;

	public CompactBidiGraph() {
		this.inEdges = new ResizingObjectArray();
	}

	public CompactBidiGraph(int initialCapacity) {
		super(initialCapacity);
		this.inEdges = new ResizingObjectArray(initialCapacity);
	}
	
	@SuppressWarnings("unchecked")
	protected List<CompactBidiEdge<EP>> getInEdgeList(int node) {
		return (List<CompactBidiEdge<EP>>)inEdges.array[node];
	}

	@Override
	protected CompactBidiEdge<EP> createEdge(int source, int target, EP property) {
		return new CompactBidiEdge<>(source, target, property);
	}

	@Override
	public Collection<CompactBidiEdge<EP>> getIncomingEdges(Integer node) {
		return getIncomingEdges(node.intValue());
	}
	
	public Collection<CompactBidiEdge<EP>> getIncomingEdges(int node) {
		List<CompactBidiEdge<EP>> inEdges = getInEdgeList(node);
		return Collections.unmodifiableCollection(inEdges);
	}

	@Override
	public Integer getSource(CompactBidiEdge<EP> edge) {
		return Integer.valueOf(getIntSource(edge));
	}
	
	public int getIntSource(CompactBidiEdge<EP> edge) {
		return edge.getSource();
	}

	/* (non-Javadoc)
	 * @see net.automatalib.graphs.base.compact.AbstractCompactGraph#addIntNode(java.lang.Object)
	 */
	@Override
	public int addIntNode(NP properties) {
		inEdges.ensureCapacity(size + 1);
		int node = super.addIntNode(properties);
		inEdges.array[node] = new ArrayList<CompactBidiEdge<EP>>();
		return node;
	}

	/* (non-Javadoc)
	 * @see net.automatalib.graphs.base.compact.AbstractCompactGraph#connect(int, int, java.lang.Object)
	 */
	@Override
	public CompactBidiEdge<EP> connect(int source, int target, EP property) {
		CompactBidiEdge<EP> edge = super.connect(source, target, property);
		List<CompactBidiEdge<EP>> inEdges = getInEdgeList(source);
		edge.inIndex = inEdges.size();
		inEdges.add(edge);
		return edge;
	}
	
	


}
