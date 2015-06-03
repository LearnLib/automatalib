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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.swing.JOptionPane;

import org.kohsuke.MetaInfServices;

import net.automatalib.commons.dotutil.DOT;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.visualization.VisualizationProvider;

@MetaInfServices(VisualizationProvider.class)
public class GraphVizBrowserVisualizationProvider implements VisualizationProvider {

	@Override
	public String getId() {
		return "graphviz-browser";
	}
	
	@Override
	public int getPriority() {
		return 10;
	}

	@Override
	public boolean checkUsable() {
		return GraphDOT.isDotUsable();
	}

	@Override
	public <N, E> void visualize(Graph<N, E> graph,
			GraphDOTHelper<N, ? super E> helper, boolean modal,
			Map<String, String> visOptions) {
		try (StringWriter sw = new StringWriter()) {
			GraphDOT.writeRaw(graph, helper, sw);
			File imgTmp = File.createTempFile("graphviz-browser", ".png");
			DOT.runDOT(sw.getBuffer().toString(), "png", imgTmp);
			File htmlTmp = File.createTempFile("graphviz-browser", ".html");
			try (PrintWriter pw = new PrintWriter(htmlTmp)) {
				pw.print("<html><body><img src=\"");
				pw.print(imgTmp.toURI().toString());
				pw.println("\"></body></html>");
			}
			Desktop.getDesktop().browse(htmlTmp.toURI());
			if (modal) {
				JOptionPane.showMessageDialog(null, "Click OK to continue ...");
			}
		}
		catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Could not render graph: " + ex.getMessage());
		}
	}

}
