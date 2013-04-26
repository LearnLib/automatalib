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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

final class AllTuplesIterator<T> implements Iterator<List<T>> {
	
	private final Iterable<T> iterable;
	private final List<T> current;
	private final Iterator<T>[] iterators;
	private boolean firstEmpty;
	
	@SuppressWarnings("unchecked")
	public AllTuplesIterator(Iterable<T> iterable, int minLength, int maxLength) {
		if(maxLength < minLength || minLength < 0)
			throw new IllegalArgumentException();
		
		this.current = new ArrayList<T>(maxLength);
		this.iterators = new Iterator[maxLength];
		this.iterable = iterable;
		
		for(int i = 1; i < minLength; i++) {
			Iterator<T> it = iterable.iterator();
			iterators[i] = it;
			current.add(it.next());
		}
		
		firstEmpty = (minLength == 0);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		if(firstEmpty)
			return true;
		
		for(int i = 0; i < iterators.length; i++) {
			Iterator<T> it = iterators[i];
			if(it == null || it.hasNext())
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public List<T> next() {
		if(firstEmpty) {
			firstEmpty = false;
			return current;
		}
		
		for(int i = 0; i < iterators.length; i++) {
			Iterator<T> it = iterators[i];
			if(it == null) {
				iterators[i] = it = iterable.iterator();
				current.add(it.next());
				return current;
			}
			
			
			if(iterators[i].hasNext()) {
				current.set(i, it.next());
				return current;
			}
			
			iterators[i] = it = iterable.iterator();
			current.set(i, it.next());
		}
		
		throw new NoSuchElementException();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
