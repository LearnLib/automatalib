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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.util.graphs.traversal;

/**
 * An action to perform during a graph traversal.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <D> user data class
 */
public final class GraphTraversalAction<D> {
	
	/**
	 * The type of a {@link GraphTraversalAction} to be performed.
	 * 
	 * @author Malte Isberner <malte.isberner@gmail.com>
	 *
	 */
	public static enum Type {
		/**
		 * Ignore the respective node or edge.
		 */
		IGNORE,
		/**
		 * Explore the respective node (in this case, the user data is regarded).
		 */
		EXPLORE,
		/**
		 * Abort the exploration of the current node.
		 */
		ABORT_NODE,
		/**
		 * Abort the traversal of the whole graph.
		 */
		ABORT_TRAVERSAL
	}
	
	
	public final Type type;
	public final D data;
	
	/**
	 * Constructor. Initializes the action with the specified type and
	 * <tt>null</tt> user data.
	 * @param type the action type
	 */
	public GraphTraversalAction(Type type) {
		this(type, null);
	}
	
	/**
	 * Constructor. Initializes the action with the specified type and user data.
	 * @param type the action type
	 * @param data the user data
	 */
	public GraphTraversalAction(Type type, D data) {
		this.type = type;
		this.data = data;
	}
}
