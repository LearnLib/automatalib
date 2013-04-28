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
package net.automatalib.commons.util;

import java.util.BitSet;
import java.util.Iterator;

/**
 * Iterator for iterating over a BitSet like over a normal collection.
 * The type returned by next() is {@link Integer}.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 */
public class BitSetIterator implements Iterator<Integer> {
	private final BitSet bitSet;
	private int currBitIdx = 0;
	
	/*
	 * Set currBitIdx to the next index which contains a 1-bit.
	 */
	private void findNextSetBit() {
		while(currBitIdx < bitSet.size() && !bitSet.get(currBitIdx))
			currBitIdx++;
	}
	
	/**
	 * Constructor.
	 * @param bitSet the bitset over which to iterate.
	 */
	public BitSetIterator(BitSet bitSet) {
		this.bitSet = bitSet;
		findNextSetBit();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return currBitIdx < bitSet.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Integer next() {
		if(currBitIdx < bitSet.size()) {
			int oldIdx = currBitIdx;
			currBitIdx++;
			findNextSetBit();
			return oldIdx;
		}
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		bitSet.clear(currBitIdx);
		findNextSetBit();
	}
}
