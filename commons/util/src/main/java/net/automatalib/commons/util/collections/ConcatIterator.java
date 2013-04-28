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

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ConcatIterator<T> implements Iterator<T> {
	
	private final Iterator<? extends T>[] iterators;
	private int currentIndex;
	
	@SafeVarargs
	public ConcatIterator(Iterator<? extends T> ...iterators) {
		int numIts = iterators.length;
		int i = 0;
		while(i < numIts) {
			Iterator<? extends T> it = iterators[i];
			if(it.hasNext())
				break;
			i++;
		}
		if(i == numIts) {
			this.iterators = null;
			this.currentIndex = -1;
		}
		else {
			this.iterators = iterators;
			this.currentIndex = i;
		}
	}

	@Override
	public boolean hasNext() {
		if(iterators != null && currentIndex < iterators.length)
			return true;
		return false;
	}

	@Override
	public T next() {
		if(iterators == null || currentIndex >= iterators.length)
			throw new NoSuchElementException();
		Iterator<? extends T> curr = iterators[currentIndex];
		T nxt = curr.next();
		if(!curr.hasNext())
			while(++currentIndex < iterators.length && !iterators[currentIndex].hasNext());
		return nxt;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
