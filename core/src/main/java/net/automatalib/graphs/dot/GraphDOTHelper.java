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
package net.automatalib.graphs.dot;

import java.io.IOException;
import java.util.Map;

import net.automatalib.commons.util.mappings.Mapping;

/**
 * Helper interface for plotting graphs using the GraphVIZ DOT format.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
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
		
		public static final String BOX = "box";
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
