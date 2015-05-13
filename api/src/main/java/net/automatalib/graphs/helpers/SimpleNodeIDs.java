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
package net.automatalib.graphs.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.graphs.SimpleGraph;
import net.automatalib.graphs.concepts.NodeIDs;

public class SimpleNodeIDs<N> implements NodeIDs<N> {

	private final Map<N,Integer> nodeIds;
	private final List<N> nodes;
	
	public SimpleNodeIDs(SimpleGraph<N> graph) {
		this.nodes = new ArrayList<N>(graph.getNodes());
		int numNodes = this.nodes.size();
		this.nodeIds = new HashMap<N,Integer>((int)(numNodes / 0.75) + 1);
		
		for(int i = 0; i < numNodes; i++) {
			N node = this.nodes.get(i);
			nodeIds.put(node, i);
		}
	}

	@Override
	public int getNodeId(N node) {
		return nodeIds.get(node).intValue();
	}

	@Override
	public N getNode(int id) {
		return nodes.get(id);
	}
}
