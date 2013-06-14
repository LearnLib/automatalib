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
import java.util.Collection;
import java.util.List;

import net.automatalib.commons.util.mappings.MutableMapping;

/**
 * Class for associating arbitrary values with the blocks of a minimization
 * result.
 * <p>
 * The storage and lookup are performed in constant time.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <V> value class.
 */
public class BlockMap<V> implements MutableMapping<Block<?,?>,V> {
	private final Object[] storage;
	
	
	/**
	 * Constructor.
	 * @param minResult the result structure.
	 */
	public BlockMap(MinimizationResult<?, ?> minResult) {
		this.storage = new Object[minResult.getNumBlocks()];
	}
	
	/**
	 * Retrieves a value.
	 * @param block the block.
	 * @return the associated value.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public V get(Block<?,?> block) {
		return (V)storage[block.getId()];
	}
	
	/**
	 * Stores a value.
	 * @param block the associated block.
	 * @param value the value.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public V put(Block<?,?> block, V value) {
		V old = (V)storage[block.getId()];
		storage[block.getId()] = value;
		return old;
	}
	
	/**
	 * Retrieves all values that are stored in this map.
	 * @return the values that are stored in this map.
	 */
	@SuppressWarnings("unchecked")
	public Collection<V> values() {
		return (List<V>)Arrays.asList(storage);
	}
}
