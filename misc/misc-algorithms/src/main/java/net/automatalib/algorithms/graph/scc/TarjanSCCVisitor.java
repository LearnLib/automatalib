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
package net.automatalib.algorithms.graph.scc;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.util.graphs.traversal.GraphTraversalAction;
import net.automatalib.util.graphs.traversal.GraphTraversalVisitor;


/**
 * Depth-first traversal visitor realizing Tarjan's algorithm for finding all
 * strongly-connected components (SCCs) in a graph.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <N> node class
 * @param <E> edge class
 */
public class TarjanSCCVisitor<N, E> implements
		GraphTraversalVisitor<N, E, TarjanSCCRecord> {
	
	
	private int counter = 0;
	private final MutableMapping<N,TarjanSCCRecord> records;
	private final List<N> currentScc = new ArrayList<>();
	private final SCCListener<N> listener;
	
	/**
	 * Constructor.
	 * @param graph the graph
	 * @param listener the SCC listener to use, <b>may not be null</b>
	 */
	public TarjanSCCVisitor(Graph<N,E> graph, SCCListener<N> listener) {
		records = graph.createStaticNodeMapping();
		this.listener = listener;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.GraphTraversalVisitor#processInitial(java.lang.Object)
	 */
	@Override
	public GraphTraversalAction<TarjanSCCRecord> processInitial(N initialNode) {
		return explore();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.GraphTraversalVisitor#startExploration(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean startExploration(N node, TarjanSCCRecord data) {
		records.put(node, data);
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.GraphTraversalVisitor#finishExploration(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void finishExploration(N node, TarjanSCCRecord data) {
		currentScc.add(node);
		if(data.lowLink == data.number) {
			listener.foundSCC(currentScc);
			currentScc.clear();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.GraphTraversalVisitor#processEdge(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public GraphTraversalAction<TarjanSCCRecord> processEdge(N srcNode,
			TarjanSCCRecord srcData, E edge, N tgtNode) {
		TarjanSCCRecord rec = records.get(tgtNode);
		if(rec == null)
			return explore();
		if(rec.lowLink != -1)
			srcData.lowLink = Math.min(srcData.lowLink, rec.number);
		return GraphTraversal.ignore();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.GraphTraversalVisitor#backtrackEdge(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void backtrackEdge(N srcNode, TarjanSCCRecord srcData, E edge,
			N tgtNode, TarjanSCCRecord tgtData) {
		int tgtLl = tgtData.lowLink;
		if(tgtLl < srcData.lowLink)
			srcData.lowLink = tgtLl;
	}
	
	public boolean hasVisited(N node) {
		return (records.get(node) != null);
	}

	private GraphTraversalAction<TarjanSCCRecord> explore() {
		return GraphTraversal.explore(new TarjanSCCRecord(counter++));
	}
	
}
