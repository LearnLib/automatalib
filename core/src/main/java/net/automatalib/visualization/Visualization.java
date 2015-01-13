/* Copyright (C) 2015 TU Dortmund
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
package net.automatalib.visualization;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import net.automatalib.AutomataLibSettings;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.dot.AggregateDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;

public class Visualization {
	
	private static final Visualization INSTANCE = new Visualization();
	
	
	@SafeVarargs
	public static <N,E> void visualizeGraph(Graph<N,E> graph, boolean modal, GraphDOTHelper<N,? super E>... addlHelpers) {
		INSTANCE.visualize(graph, modal, addlHelpers);
	}
	
	private final VPManager manager = new VPManager();
	private final VisualizationProvider provider;
	
	private Visualization() {
		AutomataLibSettings settings = AutomataLibSettings.getInstance();
		
		String providerId = settings.getProperty("visualization.provider");
		VisualizationProvider vp = null;
		
		manager.load();
		
		if (providerId != null) {
			vp = manager.getProviderByName(providerId);
		}
		
		if (vp == null) {
			vp = manager.getBestProvider();
		}
		
		if (vp == null) {
			System.err.println("Error setting visualization provider, defaulting to dummy provider...");
		}
		
		provider = vp;
	}
	
	
	@SafeVarargs
	public final <N,E> void visualize(Graph<N,E> graph, boolean modal, GraphDOTHelper<N,? super E>... addlHelpers) {
		List<GraphDOTHelper<N,? super E>> helpers = new ArrayList<>(addlHelpers.length + 1);
		helpers.add(graph.getGraphDOTHelper());
		for (GraphDOTHelper<N,? super E> h : addlHelpers) {
			helpers.add(h);
		}
		GraphDOTHelper<N, E> aggHelper = new AggregateDOTHelper<N, E>(helpers);
		
		visualize(graph, aggHelper, modal, Collections.emptyMap());
	}
	
	public <N,E> void visualize(Graph<N,E> graph, GraphDOTHelper<N, ? super E> helper,
			                    boolean modal, Map<String,String> options) {
		provider.visualize(graph, helper, modal, options);
	}

}
