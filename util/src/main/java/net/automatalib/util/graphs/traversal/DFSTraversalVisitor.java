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
package net.automatalib.util.graphs.traversal;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;

final class DFSTraversalVisitor<N, E, D> implements GraphTraversalVisitor<N, E, DFSData<D>> {
	private final IndefiniteGraph<N, E> graph;
	private final DFSVisitor<? super N, ? super E, D> visitor;
	private int dfsNum;
	private final MutableMapping<N, DFSData<D>> records;
	
	public DFSTraversalVisitor(IndefiniteGraph<N,E> graph, DFSVisitor<? super N, ? super E, D> visitor) {
		this.graph = graph;
		this.visitor = visitor;
		this.records = graph.createStaticNodeMapping();
	}

	@Override
	public GraphTraversalAction<DFSData<D>> processInitial(N initialNode) {
		D data = visitor.initialize(initialNode);
		DFSData<D> rec = new DFSData<D>(data, dfsNum++);
		records.put(initialNode, rec);
		return GraphTraversal.explore(rec);
	}

	@Override
	public boolean startExploration(N node, DFSData<D> data) {
		visitor.explore(node, data.data);
		return true;
	}

	@Override
	public void finishExploration(N node, DFSData<D> data) {
		visitor.finish(node, data.data);
		data.finished = true;
	}

	@Override
	public GraphTraversalAction<DFSData<D>> processEdge(N srcNode,
			DFSData<D> srcData, E edge) {
		N tgt = graph.getTarget(edge);
		DFSData<D> tgtRec = records.get(tgt);
		if(tgtRec == null) {
			D data = visitor.treeEdge(srcNode, srcData.data, edge, tgt);
			tgtRec = new DFSData<D>(data, dfsNum++);
			records.put(tgt, tgtRec);
			return GraphTraversal.explore(tgtRec);
		}
		if(!tgtRec.finished)
			visitor.backEdge(srcNode, srcData.data, edge, tgt, tgtRec.data);
		else if(tgtRec.dfsNumber > srcData.dfsNumber)
			visitor.forwardEdge(srcNode, srcData.data, edge, tgt, tgtRec.data);
		else
			visitor.crossEdge(srcNode, srcData.data, edge, tgt, tgtRec.data);
		return GraphTraversal.ignore();
	}
	
	
}
