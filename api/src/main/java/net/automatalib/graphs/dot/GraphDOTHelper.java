/* Copyright (C) 2013 TU Dortmund
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
package net.automatalib.graphs.dot;

import java.io.IOException;
import java.util.Map;

import net.automatalib.commons.util.mappings.Mapping;

/**
 * Helper interface for plotting graphs using the GraphVIZ DOT format.
 * 
 * @author Malte Isberner 
 *
 * @param <N> node class
 * @param <E> edge class
 */
public interface GraphDOTHelper<N, E> {
	
	public abstract class CommonAttrs {
		public static final String LABEL = "label";
		public static final String COLOR = "color";
		public static final String TEXLBL = "texlbl";
		public static final String STYLE = "style";
	}
	
	public abstract class NodeAttrs extends CommonAttrs {
		public static final String SHAPE = "shape";
		public static final String WIDTH = "width";
		public static final String HEIGHT = "height";
		public static final String FIXEDSIZE = "fixedsize";
	}
	
	public abstract class EdgeAttrs extends CommonAttrs {
		public static final String PENWIDTH = "penwidth";
		public static final String ARROWHEAD = "arrowhead";
	}
	
	public abstract class NodeShapes {
		public static final String NONE = "none";
		
		public static final String OVAL = "oval";
		public static final String DOUBLEOVAL = "doubleoval";
		
		public static final String CIRCLE = "circle";
		public static final String DOUBLECIRCLE = "doublecircle";
		
		public static final String OCTAGON = "octagon";
		public static final String DOUBLEOCTAGON = "doubleoctagon";
		
		public static final String BOX = "box";
	}
	
	public abstract class CommonStyles {
		public static final String DASHED = "dashed";
		public static final String DOTTED = "dotted";
		public static final String SOLID = "solid";
		public static final String BOLD = "bold";
	}
	
	public abstract class NodeStyles extends CommonStyles {
		public static final String FILLED = "filled";
		public static final String INVISIBLE = "invisible";
		public static final String DIAGONALS = "diagonals";
		public static final String ROUNDED = "rounded";
	}
	
	
	
	/**
	 * Called before the node and edge data are written, but <i>after</i>
	 * the opening "digraph {" statement.
	 * @param a the {@link Appendable} to write to
	 * @throws IOException if writing to <tt>a</tt> throws. 
	 */
	public void writePreamble(Appendable a) throws IOException;
	
	/**
	 * Called after the node and edge data are written, but <i>before</i>
	 * the closing brace.
	 * @param a the {@link Appendable} to write to
	 * @throws IOException if writing to <tt>a</tt> throws.
	 */
	public void writePostamble(Mapping<N,String> identifiers, Appendable a) throws IOException;
	
	public void getGlobalNodeProperties(Map<String,String> properties);
	public void getGlobalEdgeProperties(Map<String,String> properties);
	
	/**
	 * Retrieves the GraphVIZ properties for rendering a single node. Additionally,
	 * the return value allows to control whether or not to omit this node from
	 * rendering. If <tt>false</tt> is returned, the node will not be rendered.
	 * Consequently, any modifications to the properties map will have no effect.
	 * 
	 * The properties are stored in the {@link Map} argument. Note that if an implementation
	 * of a base class is overridden, it is probably a good idea to call
	 * <tt>super.getNodeProperties(node, properties);</tt> at the beginning of
	 * the method.
	 * 
	 * @param node the node to be rendered
	 * @param properties the property map
	 * @return whether or not this node should be rendered
	 */
	public boolean getNodeProperties(N node, Map<String,String> properties);
	
	/**
	 * Retrieves the GraphVIZ properties for rendering a single edge. Additionally,
	 * the return value allows to control whether or not to omit this edge from
	 * rendering. If <tt>false</tt> is returned, the edge will not be rendered.
	 * Consequently, any modifications to the properties map will have no effect.
	 * 
	 * The properties are stored in the {@link Map} argument. Note that if an implementation
	 * of a base class is overridden, it is probably a good idea to call
	 * <tt>super.getEdgeProperties(node, properties);</tt> at the beginning of
	 * the method.
	 * 
	 * @param edge the edge to be rendered
	 * @param properties the property map
	 * @return whether or not this edge should be rendered
	 */
	public boolean getEdgeProperties(N src, E edge, N tgt, Map<String,String> properties);
}
