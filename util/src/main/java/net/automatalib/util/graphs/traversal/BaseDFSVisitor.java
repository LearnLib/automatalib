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
package net.automatalib.util.graphs.traversal;

/**
 * A base implementation of a {@link DFSVisitor}.
 * 
 * @author Malte Isberner 
 *
 * @param <N> node class
 * @param <E> edge class
 * @param <D> user data class
 */
public class BaseDFSVisitor<N, E, D> implements DFSVisitor<N, E, D> {

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.DFSVisitor#exploreInitial(java.lang.Object)
	 */
	@Override
	public D initialize(N node) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.DFSVisitor#explore(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void explore(N node, D data) {
	}
	
	/**
	 * Most general edge handler. In their default implementations, the following methods
	 * resort to calling this method:
	 * <ul>
	 * <li>{@link #treeEdge(Object, Object, Object, Object)}
	 * <li>{@link #nontreeEdge(Object, Object, Object, Object, Object)}
	 * </ul>
	 * Provided that the latter is not overwritten, the following methods
	 * resort to this method indirectly in their default implementation:
	 * <ul>
	 * <li>{@link #grayTarget(Object, Object, Object, Object, Object)}
	 * <li>{@link #blackTarget(Object, Object, Object, Object, Object)}
	 * </ul>
	 * 
	 * @param srcNode the source node
	 * @param srcData the data associated with the source node
	 * @param edge the edge that is being processed
	 * @param tgtNode the target node of this edge
	 */
	public void edge(N srcNode, D srcData, E edge, N tgtNode) {
	}
	
	public void nontreeEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
		edge(srcNode, srcData, edge, tgtNode);
	}

	public void grayTarget(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
		nontreeEdge(srcNode, srcData, edge, tgtNode, tgtData);
	}
	
	public void blackTarget(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
		nontreeEdge(srcNode, srcData, edge, tgtNode, tgtData);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.DFSVisitor#treeEdge(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public D treeEdge(N srcNode, D srcData, E edge, N tgtNode) {
		edge(srcNode, srcData, edge, tgtNode);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.DFSVisitor#backEdge(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void backEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
		grayTarget(srcNode, srcData, edge, tgtNode, tgtData);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.DFSVisitor#crossEdge(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void crossEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
		blackTarget(srcNode, srcData, edge, tgtNode, tgtData);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.DFSVisitor#forwardEdge(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void forwardEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
		blackTarget(srcNode, srcData, edge, tgtNode, tgtData);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.graphs.traversal.DFSVisitor#finish(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void finish(N node, D data) {
	}

	@Override
	public void backtrackEdge(N srcNode, D srcDate, E edge, N tgtNode, D tgtData) {
	}
}
