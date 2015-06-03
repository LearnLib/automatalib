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
package net.automatalib.visualization.jung;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper.EdgeAttrs;
import net.automatalib.graphs.dot.GraphDOTHelper.NodeAttrs;
import net.automatalib.visualization.VisualizationProvider;

import org.apache.commons.collections15.Transformer;
import org.kohsuke.MetaInfServices;

import com.github.misberner.graphvizawtshapes.ShapeLibrary;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;


@MetaInfServices(VisualizationProvider.class)
public class JungGraphVisualizationProvider implements VisualizationProvider {
	
	public static final class NodeVisualization {
		public static final Transformer<NodeVisualization,String> LABEL
			= new Transformer<NodeVisualization,String>() {
				@Override
				public String transform(NodeVisualization input) {
					return input.label;
				}
		};
		public static final Transformer<NodeVisualization,Paint> DRAW_COLOR
			= new Transformer<NodeVisualization,Paint>() {
			@Override
			public Paint transform(NodeVisualization input) {
				return input.color;
			}
		};
		public static final Transformer<NodeVisualization,Paint> FILL_COLOR
			= new Transformer<NodeVisualization,Paint>() {
			@Override
			public Paint transform(NodeVisualization input) {
				return input.fillColor;
			}
		};
		public static final Transformer<NodeVisualization,Shape> SHAPE
			= new Transformer<NodeVisualization,Shape>() {
				@Override
				public Shape transform(NodeVisualization input) {
					return input.shape;
				}
		};
		public static final Transformer<NodeVisualization,Stroke> STROKE
			= new Transformer<NodeVisualization, Stroke>() {
				@Override
				public Stroke transform(NodeVisualization input) {
					return input.stroke;
				}
			
		};
		public final String label;
		public final Color color;
		public final Color fillColor;
		public final Shape shape;
		public final Stroke stroke;
		
		public NodeVisualization(String label, Color drawColor, Color fillColor, Shape shape, Stroke stroke) {
			this.label = label;
			this.color = drawColor;
			this.fillColor = fillColor;
			this.shape = shape;
			this.stroke = stroke;
		}
	}
	
	public static final class EdgeVisualization {
		public static final Transformer<EdgeVisualization,String> LABEL
			= new Transformer<EdgeVisualization,String>() {
				@Override
				public String transform(EdgeVisualization input) {
					return input.label;
				}
		};
		public static final Transformer<EdgeVisualization,Paint> DRAW_COLOR
			= new Transformer<EdgeVisualization,Paint>() {
				@Override
				public Paint transform(EdgeVisualization input) {
					return input.drawColor;
				}
		};
		
		public final String label;
		public final Color drawColor;
		
		public EdgeVisualization(String label, Color drawColor) {
			this.label = label;
			this.drawColor = drawColor;
		}
	}
	
	protected static NodeVisualization createNodeVisualization(Map<String,String> props, int id) {
		String label = props.get(NodeAttrs.LABEL);
		if(label == null) {
			label = "v" + id;
		}
		Color drawColor = getColor(props, "color", Color.BLACK);
		Color fillColor = getColor(props, "fillcolor", Color.WHITE);
		
		String shapeName = props.get(NodeAttrs.SHAPE);
		if(shapeName == null) {
			shapeName = "circle";
		}
		ShapeLibrary shapeLib = ShapeLibrary.getInstance();
		
		Shape shape = shapeLib.createShape(shapeName);
		if(shape == null) {
			System.err.println("Could not create shape " + shapeName);
			shape = shapeLib.createShape("circle");
		}
		
		String[] styles = {};
		String styleAttr = props.get("style");
		if(styleAttr != null) {
			styles = styleAttr.toLowerCase().split(",");
		}
		List<String> styleList = Arrays.asList(styles);
		
		float penWidth = 1.0f;
		Stroke stroke;
		if(styleList.contains("bold")) {
			penWidth = 3.0f;
		}
		
		if(styleList.contains("dashed")) {
			float[] dash = {10.0f};
			stroke = new BasicStroke(penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		}
		else if(styleList.contains("dotted")) {
			float[] dash = {penWidth, penWidth + 7.0f};
			stroke = new BasicStroke(penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		}
		else {
			stroke = new BasicStroke(penWidth);
		}
		
		return new NodeVisualization(label, drawColor, fillColor, shape, stroke);
	}
	
	protected static EdgeVisualization createEdgeVisualization(Map<String,String> props) {
		String label = props.get(EdgeAttrs.LABEL);
		if(label == null) {
			label = "";
		}
		Color drawColor = getColor(props, "color", Color.BLACK);
		
		return new EdgeVisualization(label, drawColor);
	}
	
	protected static Color getColor(Map<String,String> props, String propName, Color defColor) {
		String colName = props.get(propName);
		if(colName == null) {
			return defColor;
		}
		Color col = Color.getColor(colName);
		if(col == null) {
			return defColor;
		}
		return col;
	}
	
	public static <N,E> DirectedGraph<NodeVisualization,EdgeVisualization> createVisualizationGraph(
			Graph<N,E> graph, GraphDOTHelper<N,? super E> helper) {
		
		Map<String,String> defaultProps = new HashMap<>();
		helper.getGlobalNodeProperties(defaultProps);
		
		DirectedGraph<NodeVisualization, EdgeVisualization> jungGraph
			= new DirectedSparseMultigraph<>();
			
		MutableMapping<N,NodeVisualization> mapping = graph.createStaticNodeMapping();
		
		NodeIDs<N> nodeIds = graph.nodeIDs();
		
		for(N node : graph) {
			int id = nodeIds.getNodeId(node);
			Map<String,String> props = new HashMap<>(defaultProps);
			helper.getNodeProperties(node, props);
			NodeVisualization vis = createNodeVisualization(props, id);
			mapping.put(node, vis);
		}
		
		defaultProps = new HashMap<>();
		helper.getGlobalEdgeProperties(defaultProps);
		
		for(N node : graph) {
			NodeVisualization srcVis = mapping.get(node);
			for(E edge : graph.getOutgoingEdges(node)) {
				N target = graph.getTarget(edge);
				NodeVisualization tgtVis = mapping.get(target);
				Map<String,String> props = new HashMap<>(defaultProps);
				helper.getEdgeProperties(node, edge, target, props);
				EdgeVisualization edgeVis = createEdgeVisualization(props);
				jungGraph.addEdge(edgeVis, srcVis, tgtVis);
			}
		}
		
		return jungGraph;
	}
	
	private final PluggableGraphMouse mouse;
	
	public JungGraphVisualizationProvider() {
		mouse = new PluggableGraphMouse();
	    mouse.add(new PickingGraphMousePlugin<>());
	    mouse.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON3_MASK));
	    mouse.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0,
	                    1 / 1.1f, 1.1f));
	}
	
	public static void setupRenderContext(RenderContext<NodeVisualization, EdgeVisualization> ctx) {
		ctx.setVertexLabelTransformer(NodeVisualization.LABEL);
		ctx.setVertexDrawPaintTransformer(NodeVisualization.DRAW_COLOR);
		ctx.setVertexFillPaintTransformer(NodeVisualization.FILL_COLOR);
		ctx.setVertexShapeTransformer(NodeVisualization.SHAPE);
		ctx.setVertexStrokeTransformer(NodeVisualization.STROKE);
		
		ctx.setEdgeLabelTransformer(EdgeVisualization.LABEL);
		ctx.setEdgeDrawPaintTransformer(EdgeVisualization.DRAW_COLOR);
	}
	

	@Override
	public <N,E> void visualize(Graph<N,E> graph, GraphDOTHelper<N,? super E> helper,
			                    boolean modal, Map<String,String> options) {
		
		DirectedGraph<NodeVisualization,EdgeVisualization> visGraph
			= createVisualizationGraph(graph, helper);
		
		Layout<NodeVisualization,EdgeVisualization> layout
			= new KKLayout<>(visGraph);
		
		VisualizationViewer<NodeVisualization,EdgeVisualization> vv = new VisualizationViewer<>(layout);
		setupRenderContext(vv.getRenderContext());
		vv.setGraphMouse(mouse);

        final JDialog frame = new JDialog((Dialog)null, "Visualization", modal);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
	}
	
	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String getId() {
		return "jung";
	}

	@Override
	public boolean checkUsable() {
		return true;
	}

	
}
