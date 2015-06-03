/* Copyright (C) 2015 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import org.kohsuke.MetaInfServices;

@MetaInfServices(VisualizationProvider.class)
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
