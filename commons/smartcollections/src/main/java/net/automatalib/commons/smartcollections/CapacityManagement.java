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
package net.automatalib.commons.smartcollections;

/**
 * Control interface for collections supporting a capacity management, i.e., reserving
 * space in advance in order to avoid repeated reallocations.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public interface CapacityManagement {
	/**
	 * Ensures that the internal storage has room for at least
	 * the provided number of elements.
	 * @param minCapacity the minimal number of elements the storage should
	 * have room for.
	 * @return <code>true</code> iff the internal storage had to be resized,
	 * <code>false</code> otherwise.
	 */
	public boolean ensureCapacity(int minCapacity);
	
	/**
	 * Ensures that the internal storage has room for at least
	 * the provided number of <i>additional</i> elements.
	 * 
	 * Calling this method is equivalent to calling the above
	 * {@link #ensureCapacity(int)} with an argument of
	 * <code>size() + additionalCapacity</code>.
	 * 
	 * @param additionalCapacity the number of additional elements the storage
	 * should have room for. 
	 * @return <code>true</code> iff the internal storage had to be resized,
	 * <code>false</code> otherwise.
	 */
	public boolean ensureAdditionalCapacity(int additionalCapacity);
	
	/**
	 * Gives a hint regarding the capacity that should be reserved when
	 * resizing the internal storage for the next time. This method acts
	 * like a "lazy" {@link #ensureCapacity(int)}, i.e. it reserves the
	 * specified capacity at the time the next resizing of the internal
	 * storage is performed.
	 * 
	 * This method is useful when a not too imprecise upper bound on the
	 * elements that will in consequence be added is known. Since the actual
	 * number of elements added may be lower than the specified upper bound,
	 * a resizing that would have been performed by
	 * {@link #ensureCapacity(int)} might not be necessary.  
	 * 
	 * @param nextCapacityHint the next capacity hint.
	 */
	public void hintNextCapacity(int nextCapacityHint);
}
