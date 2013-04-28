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
package net.automatalib.util.graphs.traversal;

/**
 * Visitor interface for graph traversals.
 * <p>
 * This interface declares methods that are called upon basic graph traversal actions.
 *  
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <N> node class
 * @param <E> edge class
 * @param <D> user data class
 */
public interface GraphTraversalVisitor<N, E, D> {
	/**
	 * Called when a node is processed <i>initially</i>.
	 * @param initialNode the node that is processed
	 * @return the action to perform
	 */
	public GraphTraversalAction<D> processInitial(N initialNode);
	
	/**
	 * Called when the exploration of a node is started.
	 * @param node the node which's exploration is about to be started
	 * @param data the user data associated with this node
	 * @return the action to perform
	 */
	public boolean startExploration(N node, D data);
	
	/**
	 * Called when the exploration of a node is finished.
	 * @param node the node which's exploration is being finished
	 * @param data the user data associated with this node
	 */
	public void finishExploration(N node, D data);
	
	/**
	 * Called when an edge is processed.
	 * @param srcNode the source node
	 * @param srcData the user data associated with the source node
	 * @param edge the edge that is being processed
	 * @return the action to perform
	 */
	public GraphTraversalAction<D> processEdge(N srcNode, D srcData, E edge, N tgtNode);
	
	public void backtrackEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData);
}
