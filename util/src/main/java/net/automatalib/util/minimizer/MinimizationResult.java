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

import java.util.Collection;

import net.automatalib.commons.util.mappings.Mapping;



/**
 * The result structure of a minimization process. The result of a minimization
 * process is a partition on the original set of states. This is represented
 * by a collection of {@link Block}s containing states that are equivalent
 * and thus can be merged.
 * <p>
 * Since all states in a block are equivalent (and thus especially have the
 * same set of outgoing edge labels), a minimized automaton can be created
 * from this partition by instantiating a state for each block. The edges
 * between those states are created in the following way: For each state/block,
 * an original state is chosen arbitrarily from this block. The edges are
 * created according to the edges of this state, only that they point to
 * the states representing the blocks the respective target states belong to
 * (using {@link #getBlockForState(Object)}).
 * <p>
 * The blocks in the result partition are guaranteed to have contiguous IDs
 * (see {@link Block#getId()}), starting at 0. This allows an efficient
 * construction of the resulting automaton.
 * <p>
 * A more convenient way to obtain a representation of the resulting, minimized
 * automaton is using a {@link BlockAutomaton}.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class.
 * @param <L> transition label class.
 */
public final class MinimizationResult<S, L> {
	// the state storage, used for retrieving the State records
	// for an original state
	private final Mapping<S,State<S,L>> stateStorage;
	// the blocks in the final partition
	private final Collection<Block<S,L>> blocks;
	
	/**
	 * Constructor.
	 * @param stateStorage the state storage,
	 * @param blocks the final partition.
	 */
	MinimizationResult(Mapping<S,State<S,L>> stateStorage,
			Collection<Block<S,L>> blocks) {
		this.stateStorage = stateStorage;
		this.blocks = blocks;
	}
	
	/**
	 * Retrieves the number of blocks in the final partition.
	 * @return the number of blocks.
	 */
	public int getNumBlocks() {
		return blocks.size();
	}
	
	/**
	 * Retrieves all blocks in the final partition.
	 * @return the final partition.
	 */
	public Collection<Block<S,L>> getBlocks() {
		return blocks;
	}
	
	
	/**
	 * Retrieves all (original) states in a block.
	 * @param block the block.
	 * @return a collection containing all original states in this
	 * block.
	 */
	public static <S,L> Collection<S> getStatesInBlock(Block<S,L> block) {
		return new OriginalStateCollection<S>(block.getStates());
	}
	
	/**
	 * Chooses a representative (i.e., an arbitrary element of the
	 * set of states) from a block.
	 * @param block the block.
	 * @return an arbitrary element of the state set of the given block.
	 */
	public S getRepresentative(Block<S,L> block) {
		return block.getStates().choose().getOriginalState();
	}
	
	/**
	 * Retrieves the block to which a given original state belongs.
	 * @param origState the original state.
	 * @return the corresponding block.
	 */
	public Block<S,L> getBlockForState(S origState) {
		State<S,L> state = stateStorage.get(origState);
		return state.getBlock();
	}
	
	/**
	 * Creates a {@link BlockAutomaton} using this results structure.
	 * @return the block automaton.
	 */
	public BlockAutomaton<S,L> asBlockAutomaton() {
		return new BlockAutomaton<S, L>(this);
	}
}
