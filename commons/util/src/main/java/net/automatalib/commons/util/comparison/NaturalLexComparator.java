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
package net.automatalib.commons.util.comparison;

import java.util.Comparator;

/**
 * Lexicographical comparator using the natural ordering.
 * 
 * @author Malte Isberner 
 *
 * @param <T> iterable class
 * @param <U> element class
 */
final class NaturalLexComparator<T extends Iterable<U>, U extends Comparable<U>>
		implements Comparator<T> {
	
	@SuppressWarnings("rawtypes")
	private static final NaturalLexComparator<?,?> INSTANCE
		= new NaturalLexComparator();
	
	@SuppressWarnings("unchecked")
	public static <T extends Iterable<U>, U extends Comparable<U>>
	NaturalLexComparator<T,U> getInstance() {
		return (NaturalLexComparator<T,U>)INSTANCE;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(T o1, T o2) {
		return CmpUtil.lexCompare(o1, o2);
	}

}
