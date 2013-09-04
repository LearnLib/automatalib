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
package net.automatalib.commons.util.collections;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class IntRangeIterator implements ListIterator<Integer> {
	
	private final int low;
	private final int step;
	private final int size;
	private int curr;

	public IntRangeIterator(int low, int step, int size) {
		this(low, step, size, 0);
	}
	
	public IntRangeIterator(int low, int step, int size, int startIdx) {
		this.low = low;
		this.size = size;
		this.step = step;
		this.curr = startIdx;
	}

	@Override
	public boolean hasNext() {
		return curr < size;
	}
	
	public int intNext() {
		if(!hasNext())
			throw new NoSuchElementException();
		return intValue(curr++);
	}

	@Override
	public Integer next() {
		return Integer.valueOf(intNext());
	}

	@Override
	public boolean hasPrevious() {
		return curr > 0;
	}

	@Override
	public Integer previous() {
		return Integer.valueOf(intPrevious());
	}
	
	public int intPrevious() {
		if(!hasPrevious())
			throw new NoSuchElementException();
		return intValue(--curr);
	}

	@Override
	public int nextIndex() {
		if(!hasNext())
			throw new NoSuchElementException();
		return curr;
	}

	@Override
	public int previousIndex() {
		if(!hasPrevious())
			throw new NoSuchElementException();
		return curr-1;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(Integer e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Integer e) {
		throw new UnsupportedOperationException();
	}
	
	public final int intValue(int idx) {
		return low + step * idx;
	}
	
	public final Integer value(int idx) {
		return Integer.valueOf(intValue(idx));
	}
}
