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
package net.automatalib.util.graphs.dot.visualization;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.swing.JOptionPane;

import net.automatalib.commons.dotutil.DOT;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.visualization.VisualizationProvider;

public class GraphVizSwingVisualizationProvider implements VisualizationProvider {

	@Override
	public String getId() {
		return "graphviz-swing";
	}
	
	public int getPriority() {
		return 11;
	}

	@Override
	public boolean checkUsable() {
		return GraphDOT.isDotUsable();
	}

	@Override
	public <N, E> void visualize(Graph<N, E> graph,
			GraphDOTHelper<N, ? super E> helper, boolean modal,
			Map<String, String> visOptions) {
		try (Writer w = DOT.createDotWriter(modal)) {
			GraphDOT.writeRaw(graph, helper, w);
		}
		catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Error rendering graph: " + ex.getMessage());
		}
	}
}
