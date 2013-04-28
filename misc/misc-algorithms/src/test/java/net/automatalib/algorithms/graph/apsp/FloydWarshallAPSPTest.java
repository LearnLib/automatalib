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
package net.automatalib.algorithms.graph.apsp;

import java.util.Arrays;
import java.util.List;

import net.automatalib.algorithms.graph.GraphAlgorithms;
import net.automatalib.graphs.base.compact.CompactEdge;
import net.automatalib.graphs.base.compact.CompactGraph;
import net.automatalib.graphs.concepts.EdgeWeights;
import net.automatalib.util.graphs.concepts.PropertyEdgeWeights;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class FloydWarshallAPSPTest {
	
	private CompactGraph<Void,Float> graph;
	private EdgeWeights<CompactEdge<Float>> weights;
	Integer n0, n1, n2, n3, n4;

	@BeforeClass
	public void setUp() {
		graph = new CompactGraph<>();
		
		n0 = graph.addNode();
		n1 = graph.addNode();
		n2 = graph.addNode();
		n3 = graph.addNode();
		n4 = graph.addNode();
		
		graph.connect(n0, n1, Float.valueOf(2.3f));
		graph.connect(n1, n2, Float.valueOf(3.0f));
		graph.connect(n0, n2, Float.valueOf(6.0f));
		graph.connect(n2, n3, Float.valueOf(10.0f));
		graph.connect(n1, n3, Float.valueOf(7.0f));
		graph.connect(n2, n1, Float.valueOf(1.0f));
		graph.connect(n3, n4, Float.valueOf(1.0f));
		graph.connect(n2, n4, Float.valueOf(5.1f));
		graph.connect(n4, n1, Float.valueOf(10.0f));
		
		this.weights = new PropertyEdgeWeights<CompactEdge<Float>>(graph);
	}
	
	@Test
	public void testAPSP() {
		FloydWarshallAPSP<Integer, CompactEdge<Float>> apsp = new FloydWarshallAPSP<>(graph, weights);
		apsp.findAPSP();
		
		assertSPDist(apsp, n0, n1, 2.3f); // n0 -> n1
		assertSPNodes(apsp, n0, n1, n0, n1);
		assertSPDist(apsp, n0, n2, 5.3f); // n0 -> n1 -> n2
		assertSPNodes(apsp, n0, n2, n0, n1, n2);
		assertSPDist(apsp, n0, n3, 9.3f); // n0 -> n1 -> n3
		assertSPNodes(apsp, n0, n3, n0, n1, n3);
		assertSPDist(apsp, n0, n4, 10.3f); // n0 -> n1 -> n3 -> n4
		assertSPNodes(apsp, n0, n4, n0, n1, n3, n4);
		
		assertSPDist(apsp, n1, n0, GraphAlgorithms.INVALID_DISTANCE);
		assertSPDist(apsp, n1, n2, 3.0f); // n1 -> n2
		assertSPNodes(apsp, n1, n2, n1, n2);
		assertSPDist(apsp, n1, n3, 7.0f); // n1 -> n3
		assertSPNodes(apsp, n1, n3, n1, n3);
		assertSPDist(apsp, n1, n4, 8.0f); // n1 -> n3 -> n4
		assertSPNodes(apsp, n1, n4, n1, n3, n4);
		
	
		assertSPDist(apsp, n2, n0, GraphAlgorithms.INVALID_DISTANCE);
		assertSPDist(apsp, n2, n1, 1.0f); // n2 -> n1
		assertSPNodes(apsp, n2, n1, n2, n1);
		assertSPDist(apsp, n2, n3, 8.0f); // n2 -> n1 -> n3
		assertSPNodes(apsp, n2, n3, n2, n1, n3);
		assertSPDist(apsp, n2, n4, 5.1f); // n2 -> n4
		assertSPNodes(apsp, n2, n4, n2, n4);
		
		assertSPDist(apsp, n3, n0, GraphAlgorithms.INVALID_DISTANCE);
		assertSPDist(apsp, n3, n1, 11.0f); // n3 -> n4 -> n1
		assertSPNodes(apsp, n3, n1, n3, n4, n1);
		assertSPDist(apsp, n3, n2, 14.0f); // n3 -> n4 -> n1 -> n2
		assertSPNodes(apsp, n3, n2, n3, n4, n1, n2);
		assertSPDist(apsp, n3, n4, 1.0f); // n3 -> n4
		assertSPNodes(apsp, n3, n4, n3, n4);
		
		assertSPDist(apsp, n4, n0, GraphAlgorithms.INVALID_DISTANCE);
		assertSPDist(apsp, n4, n1, 10.0f); // n4 -> n1
		assertSPNodes(apsp, n4, n1, n4, n1);
		assertSPDist(apsp, n4, n2, 13.0f); // n4 -> n1 -> n2
		assertSPNodes(apsp, n4, n2, n4, n1, n2);
		assertSPDist(apsp, n4, n3, 17.0f); // n4 -> n1 -> n3
		assertSPNodes(apsp, n4, n3, n4, n1, n3);
	}
	
	private static <N> void assertSPDist(APSPResult<N, ?> res, N src, N tgt, float dist) {
		Assert.assertEquals(res.getShortestPathDistance(src, tgt), dist);
	}
	
	private void assertSPNodes(APSPResult<Integer,CompactEdge<Float>> res, Integer src, Integer tgt, Integer ...expNodes) {
		List<Integer> nodes = GraphAlgorithms.toNodeList(res.getShortestPath(src, tgt), graph, src);
		Assert.assertEquals(nodes, Arrays.asList(expNodes));
	}

}
