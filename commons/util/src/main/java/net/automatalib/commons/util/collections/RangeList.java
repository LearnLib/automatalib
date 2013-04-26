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
package net.automatalib.commons.util.collections;

import java.util.AbstractList;
import java.util.RandomAccess;

final class RangeList extends AbstractList<Integer> implements RandomAccess {
	
	
	private final int start;
	private final int step;
	private final int end;
	
	public RangeList(int start, int end) {
		this(start, 1, end);
	}
	
	public RangeList(int start, int step, int end) {
		this.start = start;
		this.step = step;
		this.end = end;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	public Integer get(int index) {
		if(index < 0 || start + step*index >= end)
			throw new IndexOutOfBoundsException();
		return start + step*index;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return (end - start - 1)/step + 1;
	}

}
