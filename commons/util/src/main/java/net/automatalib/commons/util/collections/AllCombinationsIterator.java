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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

final class AllCombinationsIterator<T> implements Iterator<List<T>> {
	private final Iterable<T>[] iterables;
	private final Iterator<T>[] iterators;
	private final List<T> current;
	private boolean first = true;
	
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public AllCombinationsIterator(Iterable<T> ...iterables) {
		this.iterables = iterables;
		this.iterators = new Iterator[iterables.length];
		this.current = new ArrayList<T>(iterables.length);
		for(int i = 0; i < iterators.length; i++) {
			Iterator<T> it = iterables[i].iterator();
			this.iterators[i] = it;
			this.current.add(it.next());
		}	
	}

	@Override
	public boolean hasNext() {
		for(int i = 0; i < iterators.length; i++) {
			Iterator<T> it = iterators[i];
			if(it == null || it.hasNext())
				return true;
		}
		return false;
	}

	@Override
	public List<T> next() {
		if(first) {
			first = false;
			return current;
		}
		
		for(int i = 0; i < iterators.length; i++) {
			Iterator<T> it = iterators[i];
			
			if(iterators[i].hasNext()) {
				current.set(i, it.next());
				return current;
			}
			
			iterators[i] = it = iterables[i].iterator();
			current.set(i, it.next());
		}
		
		throw new NoSuchElementException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	
}
