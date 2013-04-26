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
package net.automatalib.commons.util.array;

import java.util.Collection;

/**
 * Unified interface for (collection) classes that allow writing their contents to an array.
 * The intended behavior differs from the standard Java {@link Collection#toArray(Object[])} method
 * in the following way:
 *  - 
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <T> type class. This is a marker parameter that is not reflected in the signatures,
 * but respected by the methods in {@link AWUtil}.
 */
public interface ArrayWritable<T> {
	/**
	 * Writes the contents of this container to an array. The behavior of calling this method
	 * should be equivalent to
	 * <code>System.arraycopy(this.toArray(), offset, array, tgtOfs, num);</code>
	 * 
	 * @param offset how many elements of <i>this</i> container to skip.
	 * @param array the array in which to store the elements.
	 * @param tgtOfs the starting offset in the target array.
	 * @param num the maximum number of elements to copy.
	 */
	public void writeToArray(int offset, Object[] array, int tgtOfs, int num);
	
	
	/**
	 * The size of this container.
	 */
	public int size();
}
