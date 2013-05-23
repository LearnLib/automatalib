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
package net.automatalib.util.graphs.traversal;

import net.automatalib.commons.util.Holder;

public class DefaultGraphTraversalVisitor<N, E, D> implements
		GraphTraversalVisitor<N, E, D> {

	@Override
	public GraphTraversalAction processInitial(N initialNode, Holder<D> outData) {
		return GraphTraversalAction.EXPLORE;
	}

	@Override
	public boolean startExploration(N node, D data) {
		return true;
	}

	@Override
	public void finishExploration(N node, D data) {
	}

	@Override
	public GraphTraversalAction processEdge(N srcNode, D srcData, E edge, N tgtNode,
			Holder<D> outData) {
		return GraphTraversalAction.EXPLORE;
	}

	@Override
	public void backtrackEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
	}

}
