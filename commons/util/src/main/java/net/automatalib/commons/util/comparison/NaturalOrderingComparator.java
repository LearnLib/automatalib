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
package net.automatalib.commons.util.comparison;

import java.util.Comparator;

/**
 * A {@link Comparator} that compares elements according to their
 * natural ordering (i.e., they have to implement the {@link Comparable}
 * interface). If this comparator is used on objects that don't implement
 * this interface, this may result in a {@link ClassCastException}.
 * 
 * This class is a singleton, since due to type erasure, different
 * instantiations won't really differ from each other.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <T> element class.
 */
final class NaturalOrderingComparator<T extends Comparable<T>> implements Comparator<T> {
	private static final NaturalOrderingComparator<? extends Comparable<?>> INSTANCE
		= new NaturalOrderingComparator<Integer>();
	
	
	/**
	 * Singleton instance access method.
	 * @param <T> element class.
	 * @return the natural ordering comparator.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> NaturalOrderingComparator<T> getInstance() {
		return (NaturalOrderingComparator<T>)INSTANCE;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(T o1, T o2) {
		return o1.compareTo(o2);
	}
}
