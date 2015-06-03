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
package net.automatalib.commons.util.collections;

import java.util.AbstractList;
import java.util.RandomAccess;

import net.automatalib.commons.util.array.ArrayWritable;

final class IntRange extends AbstractList<Integer> implements ArrayWritable<Integer>, RandomAccess {
	
	private final int start;
	private final int step;
	private final int size;
	
	public IntRange(int start, int end) {
		this(start, end, 1);
	}
	
	public IntRange(int start, int end, int step) {
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
