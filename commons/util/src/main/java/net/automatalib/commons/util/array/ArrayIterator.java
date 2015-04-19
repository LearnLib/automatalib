/* Copyright (C) 2015 TU Dortmund
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
package net.automatalib.commons.util.array;

import java.util.ListIterator;

public class ArrayIterator<T> implements ListIterator<T> {

	private final T[] array;
	private int curr;
	private final int start, end;
	
	public ArrayIterator(T[] array, int start, int curr, int end) {
		this.array = array;
		this.start = start;
		this.curr = curr;
		this.end = end;
	}
	
	public ArrayIterator(T[] array, int start, int end) {
		this(array, start, start, end);
	}
	
	public ArrayIterator(T[] array, int start) {
		this(array, start, array.length);
	}
	
	public ArrayIterator(T[] array) {
		this(array, 0);
	}

	@Override
	public boolean hasNext() {
		return curr < end;
	}

	@Override
	public T next() {
		return array[curr++];
	}

	@Override
	public boolean hasPrevious() {
		return curr > start;
	}

	@Override
	public T previous() {
		return array[--curr];
	}

	@Override
	public int nextIndex() {
		return curr - start;
	}

	@Override
	public int previousIndex() {
		return curr - start - 1;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(T e) {
		array[curr - 1] = e;
	}

	@Override
	public void add(T e) {
		throw new UnsupportedOperationException();
	}

}
