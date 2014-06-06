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
package net.automatalib.util.minimizer;

import java.util.Arrays;
import java.util.List;

/**
 * A "block automaton", i.e. an automaton-style representation of the
 * minimization result in which each block forms a state.
 * 
 * @author Malte Isberner 
 *
 * @param <S> state class.
 * @param <L> transition label class.
 */
public class BlockAutomaton<S, L> {
	
	// Edges array
	private final BlockEdge<S,L>[][] edges;
	
	/**
	 * Constructor. Creates the block automaton.
	 * @param minResult the minimization result.
	 */
	@SuppressWarnings("unchecked")
	BlockAutomaton(MinimizationResult<S, L> minResult) {
		edges = new BlockEdge[minResult.getNumBlocks()][];
		
		for(Block<S,L> block : minResult.getBlocks()) {
			int id = block.getId();
			State<S,L> rep = block.getStates().choose();
			List<Edge<S,L>> outgoing = rep.getOutgoing();
			BlockEdge<S,L>[] array = new BlockEdge[outgoing.size()];
			int i = 0;
			for(Edge<S,L> e : outgoing) {
				array[i++] = new BlockEdge<S,L>(block,
						e.getTarget().getBlock(),
						e.getTransitionLabel().getOriginalLabel());
			}
			edges[id] = array;
		}
	}
	
	/**
	 * Retrieves a list of outgoing edges of a block (state).
	 * @param block the block (state).
	 * @return the outgoing edges of the given block (state).
	 */
	public List<BlockEdge<S,L>> getOutgoingEdges(Block<S,L> block) {
		return Arrays.asList(edges[block.getId()]);
	}
	
	/**
	 * Retrieves an array of outgoing edges of a block (state).
	 * @see #getOutgoingEdges(Block)
	 */
	public BlockEdge<S,L>[] getOutgoingEdgeArray(Block<S,L> block) {
		return edges[block.getId()];
	}
}
