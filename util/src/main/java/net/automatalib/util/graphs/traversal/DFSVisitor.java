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

public interface DFSVisitor<N, E, D> {
	public D initialize(N node);
	public void explore(N node, D data);
	public void finish(N node, D data);
	
	public D treeEdge(N srcNode, D srcData, E edge, N tgtNode);
	public void backEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData);
	public void crossEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData);
	public void forwardEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData);
	
	public void backtrackEdge(N srcNode, D srcDate, E edge, N tgtNode, D tgtData);
}
