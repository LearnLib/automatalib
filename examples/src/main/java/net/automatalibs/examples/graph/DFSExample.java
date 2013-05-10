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
package net.automatalibs.examples.graph;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import net.automatalib.commons.dotutil.DOT;
import net.automatalib.graphs.base.compact.CompactEdge;
import net.automatalib.graphs.base.compact.CompactSimpleGraph;
import net.automatalib.graphs.dot.EmptyDOTHelper;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.util.graphs.traversal.BaseDFSVisitor;
import net.automatalib.util.graphs.traversal.GraphTraversal;

public class DFSExample {
	
	public static enum EdgeType {
		TREE("bold"),
		FORWARD("dotted"),
		BACK("solid"),
		CROSS("dashed");
		
		private final String style;
		
		private EdgeType(String style) {
			this.style = style;
		}
		
		public String getStyle() {
			return style;
		}
	}
	
	public static class MyDFSVisitor<N,E> extends BaseDFSVisitor<N, E, Void> {
		private final Map<N,Integer> dfsNumbers = new HashMap<>();
		private final Map<E,EdgeType> edgeTypes = new HashMap<>();
		
		/* (non-Javadoc)
		 * @see net.automatalib.util.graphs.traversal.BaseDFSVisitor#explore(java.lang.Object, java.lang.Object)
		 */
		@Override
		public void explore(N node, Void data) {
			dfsNumbers.put(node, dfsNumbers.size());
		}

		@Override
		public Void treeEdge(N srcNode, Void srcData, E edge,
				N tgtNode) {
			edgeTypes.put(edge, EdgeType.TREE);
			return null;
		}
		
		@Override
		public void backEdge(N srcNode, Void srcData, E edge,
				N tgtNode, Void tgtData) { 
			edgeTypes.put(edge, EdgeType.BACK);
		}
		
		@Override
		public void crossEdge(N srcNode, Void srcData, E edge,
				N tgtNode, Void tgtData) {
			edgeTypes.put(edge, EdgeType.CROSS);
		}
		@Override
		public void forwardEdge(N srcNode, Void srcData, E edge,
				N tgtNode, Void tgtData) {
			edgeTypes.put(edge, EdgeType.FORWARD);
		}
		
		public Map<N,Integer> getDfsNumbers() {
			return dfsNumbers;
		}
		
		public Map<E,EdgeType> getEdgeTypes() {
			return edgeTypes;
		}
	}
	
	public static class DFSResultDOTHelper<N,E> extends EmptyDOTHelper<N,E> {
		private final Map<N,Integer> dfsNumbers;
		private final Map<E,EdgeType> edgeTypes;
		
		/* (non-Javadoc)
		 * @see net.automatalib.graphs.dot.EmptyDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
		 */
		@Override
		public boolean getNodeProperties(N node,
				Map<String, String> properties) {
			String lbl = properties.get("label");
			Integer dfsNum = dfsNumbers.get(node);
			assert dfsNum != null;
			properties.put("label", lbl + " [#" + dfsNum + "]");
			return true;
		}
		
		/* (non-Javadoc)
		 * @see net.automatalib.graphs.dot.EmptyDOTHelper#getEdgeProperties(java.lang.Object, java.util.Map)
		 */
		@Override
		public boolean getEdgeProperties(E edge,
				Map<String, String> properties) {
			EdgeType et = edgeTypes.get(edge);
			assert et != null;
			properties.put("style", et.getStyle());
			return true;
		}
		
		public DFSResultDOTHelper(Map<N, Integer> dfsNumbers,
				Map<E,EdgeType> edgeTypes) {
			this.dfsNumbers = dfsNumbers;
			this.edgeTypes = edgeTypes;
		}
		
		public DFSResultDOTHelper(MyDFSVisitor<N,E> vis) {
			this(vis.getDfsNumbers(), vis.getEdgeTypes());
		}
	}

	
	public static void main(String[] args) throws Exception {
		CompactSimpleGraph<Void> graph = new CompactSimpleGraph<>();
		
		int n0 = graph.addIntNode(), n1 = graph.addIntNode(), n2 = graph.addIntNode(), n3 = graph.addIntNode(), n4 = graph.addIntNode();
		
		graph.connect(n0, n1);
		graph.connect(n0, n2);
		graph.connect(n1, n1);
		graph.connect(n1, n2);
		graph.connect(n2, n3);
		graph.connect(n3, n1);
		graph.connect(n3, n0);
		graph.connect(n0, n4);
		graph.connect(n4, n3);
		
		MyDFSVisitor<Integer,CompactEdge<Void>> vis = new MyDFSVisitor<>();
		GraphTraversal.dfs(graph, n0, vis);
		DFSResultDOTHelper<Integer,CompactEdge<Void>> helper = new DFSResultDOTHelper<>(vis);
		
		Writer w = DOT.createDotWriter(true);
		GraphDOT.write(graph, w, helper);
		w.close();
	}

}
