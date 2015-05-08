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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class IterableUtil {
	
	private static final Iterator<?> EMPTY_ITERATOR = new Iterator<Object>() {
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Object next() {
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
		
	};
	
	@SafeVarargs
	public static <T> Iterator<T> concat(Iterator<? extends T> ...iterators) {
		return new ConcatIterator<>(iterators);
	}
	
	@SafeVarargs
	public static <T> Iterable<T> concat(final Iterable<? extends T> ...iterables) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				@SuppressWarnings("unchecked")
				Iterator<? extends T>[] iterators = new Iterator[iterables.length];
				for(int i = 0; i < iterables.length; i++)
					iterators[i] = iterables[i].iterator();
				return concat(iterators);
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> emptyIterator() {
		return (Iterator<T>)EMPTY_ITERATOR;
	}
	
	public static <T> Iterator<T> unmodifiableIterator(Iterator<T> iterator) {
		return new UnmodifiableIterator<>(iterator);
	}
	
	
	public static <T> Iterable<T> unmodifiableIterable(final Iterable<T> iterable) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return unmodifiableIterator(iterable.iterator());
			}
		};
	}
	
	public static <T> Iterator<List<T>> allCombinationsIterator(List<? extends Iterable<? extends T>> iterables) {
		return allCombinationsIterator(iterables);
	}
	
	@SafeVarargs
	public static <T> Iterator<List<T>> allCombinationsIterator(Iterable<? extends T> ...iterables) {
		return allCombinationsIterator(Arrays.asList(iterables));
	}
	
	public static <T> Iterable<List<T>> allCombinations(List<? extends Iterable<? extends T>> iterables) {
		return new Iterable<List<T>>() {
			@Override
			public Iterator<List<T>> iterator() {
				return allCombinationsIterator(iterables);
			}
		};
	}
	
	@SafeVarargs
	public static <T> Iterable<List<T>> allCombinations(Iterable<? extends T> ...iterables) {
		return allCombinations(Arrays.asList(iterables));
	}
	
	// Prevent inheritance
	private IterableUtil() {}

}
