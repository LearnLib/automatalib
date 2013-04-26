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


final class SafeComparator<T> implements Comparator<T> {
	
	private final int firstNullResult;
	private final Comparator<T> baseComparator;
	
	/**
	 * Constructor.
	 * @param baseComparator the underlying comparator.
	 * @param nullOrdering the <code>null</code> element ordering policy.
	 */
	public SafeComparator(Comparator<T> baseComparator, CmpUtil.NullOrdering nullOrdering) {
		this.firstNullResult = nullOrdering.firstNullResult;
		this.baseComparator = baseComparator;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(T o1, T o2) {
		if(o1 == null) {
			if(o2 == null)
				return 0;
			return firstNullResult;
		}
		else if(o2 == null)
			return -firstNullResult;
			
		return baseComparator.compare(o1, o2);
	}

}
