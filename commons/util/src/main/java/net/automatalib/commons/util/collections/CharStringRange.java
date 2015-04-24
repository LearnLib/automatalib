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
package net.automatalib.commons.util.collections;

import java.util.AbstractList;
import java.util.RandomAccess;

import net.automatalib.commons.util.array.ArrayWritable;

public class CharStringRange extends AbstractList<String> implements
		ArrayWritable<String>, RandomAccess {
	
	private final IntRange delegate;

	public CharStringRange(char low, char high) {
		this(low, high, 1);
	}
	
	public CharStringRange(char low, char high, int step) {
		this(new IntRange(low, high, step));
	}
	
	public CharStringRange(IntRange delegate) {
		this.delegate = delegate;
	}

	public char charGet(int index) {
		int i = delegate.intGet(index);
		return (char)i;
	}
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	public String get(int index) {
		return String.valueOf(charGet(index));
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return delegate.size();
	}

	/* (non-Javadoc)
	 * @see net.automatalib.commons.util.array.ArrayWritable#writeToArray(int, java.lang.Object[], int, int)
	 */
	@Override
	public void writeToArray(int offset, Object[] array, int tgtOfs, int num) {
		int si = offset;
		int ti = tgtOfs;
		for(int i = 0; i < num; i++)
			array[ti++] = charGet(si++);
	}
	
	public int indexOf(String s) {
		if (s.length() != 1) {
			return -1;
		}
		return delegate.indexOf(s.charAt(0));
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		if(o == null || o.getClass() != String.class)
			return -1;
		return indexOf((String) o);
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
	public CharStringRangeIterator iterator() {
		return new CharStringRangeIterator(delegate.iterator());
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#listIterator()
	 */
	@Override
	public CharStringRangeIterator listIterator() {
		return new CharStringRangeIterator(delegate.listIterator());
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#listIterator(int)
	 */
	@Override
	public CharStringRangeIterator listIterator(int index) {
		return new CharStringRangeIterator(delegate.listIterator(index));
	}

	
	public char charValue(int i) {
		return (char)delegate.intValue(i);
	}

}
