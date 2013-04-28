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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * Various methods for operating on collections.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public abstract class CollectionsUtil {
	
	/*
	 * Prevent inheritance.
	 */
	private CollectionsUtil() {
	}
	
	/**
	 * Adds all elements from an iterable to a collection.
	 * @param <E> element class.
	 * @param coll the collection to which the elements are added.
	 * @param it the iterable providing the elements to add.
	 * @return <code>true</code> if the collection was modified,
	 * <code>false</code> otherwise.
	 */
	public static <E> boolean addAll(Collection<E> coll, Iterable<? extends E> it) {
		boolean modified = false;
		for(E e : it)
			modified = coll.add(e) || modified;
		return modified;
	}
	
	/**
	 * Retrieves an unmodifiable list only containing null values
	 * of the given size.
	 * @param size the size
	 * @return a list consisting of the specified number of <tt>null</tt> values
	 */
	@SuppressWarnings("unchecked")
	public static <E> List<E> nullList(int size) {
		return (List<E>)(List<?>)new NullList(size);
	}
	
	public static List<Integer> rangeList(int start, int end) {
		return new RangeList(start, end);
	}
	
	public static List<Integer> rangeList(int start, int step, int end) {
		return new RangeList(start, step, end);
	}
	
	public static <T> List<? extends T> randomAccessList(Collection<? extends T> coll) {
		if(coll instanceof List && coll instanceof RandomAccess)
			return (List<? extends T>)coll;
		return new ArrayList<>(coll);
	}
	
	
	public static <T> Iterable<List<T>> allTuples(final Iterable<T> domain, final int minLength, final int maxLength) {
		// Check if domain is empty
		// If it is, then the empty tuple (if not excluded by minLength > 0) is still part of the result
		// Otherwise, the result is empty
		if(!domain.iterator().hasNext()) {
			if(minLength == 0)
				return Collections.singletonList(Collections.<T>emptyList());
			return Collections.<List<T>>emptyList();
		}
		
		return new Iterable<List<T>>() {
			@Override
			public Iterator<List<T>> iterator() {
				return new AllTuplesIterator<>(domain, minLength, maxLength);
			}
		};
	}
	
	public static <T> Iterable<List<T>> allTuples(final Iterable<T> domain, final int length) {
		return allTuples(domain, length, length);
	}
	
	@SafeVarargs
	public static <T> Iterable<List<T>> allCombinations(final Iterable<T> ...iterables) {
		if(iterables.length == 0)
			return Collections.singletonList(Collections.<T>emptyList());
		
		return new Iterable<List<T>>() {
			@Override
			public Iterator<List<T>> iterator() {
				try {
					return new AllCombinationsIterator<>(iterables);
				}
				catch(NoSuchElementException ex) {
					// FIXME: Special case if one of the iterables is empty, then the whole set
					// of combinations is empty. Maybe handle this w/o exception?
					return Collections.<List<T>>emptySet().iterator();
				}
			}
		};
	}

}
