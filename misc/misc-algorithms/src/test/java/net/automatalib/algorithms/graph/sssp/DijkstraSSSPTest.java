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
package net.automatalib.algorithms.graph.sssp;

import java.util.Arrays;
import java.util.List;

import net.automatalib.algorithms.graph.GraphAlgorithms;
import net.automatalib.algorithms.graph.sssp.DijkstraSSSP;
import net.automatalib.algorithms.graph.sssp.SSSPResult;
import net.automatalib.graphs.base.compact.CompactEdge;
import net.automatalib.graphs.base.compact.CompactSimpleGraph;
import net.automatalib.graphs.concepts.EdgeWeights;
import net.automatalib.util.graphs.concepts.PropertyEdgeWeights;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class DijkstraSSSPTest {

	private CompactSimpleGraph<Float> graph;
	private EdgeWeights<CompactEdge<Float>> weights;
	Integer n0, n1, n2, n3, n4, n5;

	@BeforeClass
	public void setUp() {
		graph = new CompactSimpleGraph<>();
		
		n0 = graph.addNode();
		n1 = graph.addNode();
		n2 = graph.addNode();
		n3 = graph.addNode();
		n4 = graph.addNode();
		n5 = graph.addNode();
		
		graph.connect(n0, n1, Float.valueOf(2.3f));
		graph.connect(n1, n2, Float.valueOf(3.0f));
		graph.connect(n0, n2, Float.valueOf(6.0f));
		graph.connect(n2, n3, Float.valueOf(10.0f));
		graph.connect(n1, n3, Float.valueOf(7.0f));
		graph.connect(n2, n1, Float.valueOf(1.0f));
		graph.connect(n3, n4, Float.valueOf(1.0f));
		graph.connect(n2, n4, Float.valueOf(5.1f));
		graph.connect(n4, n1, Float.valueOf(10.0f));
		graph.connect(n5, n1, Float.valueOf(0.1f));
		
		this.weights = new PropertyEdgeWeights<CompactEdge<Float>>(graph);
	}
	
	@Test
	public void testSSSP() {
		DijkstraSSSP<Integer, CompactEdge<Float>> sssp = new DijkstraSSSP<>(graph, n0, weights);
		sssp.findSSSP();
		
		assertSPDist(sssp, n1, 2.3f); // n0 -> n1
		assertSPNodes(sssp, n1, n0, n1);
		assertSPDist(sssp, n2, 5.3f); // n0 -> n1 -> n2
		assertSPNodes(sssp, n2, n0, n1, n2);
		assertSPDist(sssp, n3, 9.3f); // n0 -> n1 -> n3
		assertSPNodes(sssp, n3, n0, n1, n3);
		assertSPDist(sssp, n4, 10.3f); // n0 -> n1 -> n3 -> n4
		assertSPNodes(sssp, n4, n0, n1, n3, n4);
		assertSPDist(sssp, n5, GraphAlgorithms.INVALID_DISTANCE);
	}
	
	private static <N> void assertSPDist(SSSPResult<N, ?> res, N tgt, float dist) {
		Assert.assertEquals(res.getShortestPathDistance(tgt), dist);
	}
	
	private void assertSPNodes(SSSPResult<Integer,CompactEdge<Float>> res, Integer tgt, Integer ...expNodes) {
		List<Integer> nodes = GraphAlgorithms.toNodeList(res.getShortestPath(tgt), graph, res.getInitialNode());
		Assert.assertEquals(nodes, Arrays.asList(expNodes));
	}
}
