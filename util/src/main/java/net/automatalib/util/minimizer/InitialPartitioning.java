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

/**
 * Interface for the initial partitioning data structure.
 * 
 * This interface is needed to generalize the optimized (if the number
 * of partitions is known and the classification can be done using integers)
 * and non-optimized variants of initial partitionings.
 *  
 * @author Malte Isberner
 *
 * @param <S> state class.
 * @param <L> transition label class.
 */
interface InitialPartitioning<S, L> {
	
	/**
	 * Retrieves the initial block for a given state. If no such block exists,
	 * it will be created.
	 * @param origState the original state.
	 * @return the block for the state in the initial partitioning.
	 */
	public Block<S,L> getBlock(S origState);
	
	/**
	 * Retrieves all blocks in the initial partitioning.
	 * @return the initial blocks.
	 */
	public Collection<Block<S,L>> getInitialBlocks();
}
