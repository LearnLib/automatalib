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

import java.util.AbstractList;
import java.util.RandomAccess;

import net.automatalib.commons.util.array.ArrayWritable;

final class IntRange extends AbstractList<Integer> implements ArrayWritable<Integer>, RandomAccess {
	
	private final int start;
	private final int step;
	private final int size;
	
	public IntRange(int start, int end) {
		this(start, 1, end);
	}
	
	public IntRange(int start, int step, int end) {
		this.start = start;
		this.step = step;
		this.size = (end - start - 1) / step + 1;
	}

	public int intGet(int index) {
		if(index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		return intValue(index);
	}
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	public Integer get(int index) {
		return Integer.valueOf(intGet(index));
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return size;
	}

	/* (non-Javadoc)
	 * @see net.automatalib.commons.util.array.ArrayWritable#writeToArray(int, java.lang.Object[], int, int)
	 */
	@Override
	public void writeToArray(int offset, Object[] array, int tgtOfs, int num) {
		int x = start + offset * step;
		int ti = tgtOfs;
		for(int i = 0; i < num; i++) {
			array[ti++] = x;
			x += step;
		}
	}
	
	public int indexOf(int i) {
		if(i < start)
			return -1;
		i -= start;
		if(i % step != 0)
			return -1;
		i /= step;
		if(i >= size)
			return -1;
		return i;
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		if(o == null || o.getClass() != Integer.class)
			return -1;
		int i = ((Integer)o).intValue();
		return indexOf(i);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o) {
		return indexOf(o);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#iterator()
	 */
	@Override
	public IntRangeIterator iterator() {
		return new IntRangeIterator(start, step, size);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#listIterator()
	 */
	@Override
	public IntRangeIterator listIterator() {
		return new IntRangeIterator(start, step, size);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#listIterator(int)
	 */
	@Override
	public IntRangeIterator listIterator(int index) {
		return new IntRangeIterator(start, step, size, index);
	}

	
	public int intValue(int i) {
		return start + step * i;
	}
	
	
}
