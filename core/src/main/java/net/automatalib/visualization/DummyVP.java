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

import java.util.Map;

import javax.swing.JOptionPane;

import net.automatalib.graphs.Graph;
import net.automatalib.graphs.dot.GraphDOTHelper;

public class DummyVP implements VisualizationProvider {

	@Override
	public String getId() {
		return "dummy";
	}

	@Override
	public boolean checkUsable() {
		return true;
	}

	@Override
	public <N, E> void visualize(Graph<N, E> graph,
			GraphDOTHelper<N, ? super E> helper, boolean modal, Map<String,String> options) {
		System.err.println("Attempted to visualize graph with " + graph.size() + " nodes, but not usable "
				+ "visualization provider configured");
		if (modal) {
			JOptionPane.showMessageDialog(null, "Press OK to continue ...");
		}
	}
	
	@Override
	public int getPriority() {
		return Integer.MIN_VALUE;
	}
}
