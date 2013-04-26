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
package net.automatalib.commons.smartcollections;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * A priority queue interface.
 * 
 * A priority queue is a queue which supports removal of the element with
 * the minimal key value (wrt. natural ordering or an explicitly specified
 * {@link Comparator}).
 * 
 * This interface extends the functionality of the standard
 * {@link PriorityQueue} in the way that it allows dynamic behavior: The
 * ordering of the elements in the queue is allowed to change. The only
 * restriction is that whenever the key which is used for comparison changes,
 * the method {@link #keyChanged(ElementReference)} has to be called with
 * the reference of the respective element. 
 * 
 *  
 * @author Malte Isberner
 *
 * @param <E> element class.
 */
public interface SmartDynamicPriorityQueue<E>
		extends SmartPriorityQueue<E> {
	
	/**
	 * Notifies the implementation that the key of an element has changed.
	 * 
	 * @param reference the reference for the element whose key has changed.
	 */
	public void keyChanged(ElementReference reference);
}
