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

public class CharRangeIterator implements ListIterator<Character> {
	
	private final IntRangeIterator delegate;
 
	public CharRangeIterator(char low, int step, int size) {
		this(low, step, size, 0);
	}
	
	public CharRangeIterator(char low, int step, int size, int curr) {
		this(new IntRangeIterator(low, step, size, curr));
	}
	
	
	public CharRangeIterator(IntRangeIterator delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean hasNext() {
		return delegate.hasNext();
	}

	@Override
	public Character next() {
		return Character.valueOf((char)delegate.intNext());
	}

	@Override
	public boolean hasPrevious() {
		return delegate.hasPrevious();
	}

	@Override
	public Character previous() {
		return Character.valueOf((char)delegate.intPrevious());
	}

	@Override
	public int nextIndex() {
		return delegate.nextIndex();
	}

	@Override
	public int previousIndex() {
		return delegate.previousIndex();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(Character e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Character e) {
		throw new UnsupportedOperationException();
	}

}