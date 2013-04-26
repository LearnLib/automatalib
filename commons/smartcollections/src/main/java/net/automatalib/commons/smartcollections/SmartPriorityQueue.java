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


/**
 * Priority queue interface. Note that this class deliberately does not
 * specify whether the inserted elements come with their own key (i.e.
 * implement {@link Comparable} or can be compared using a comparator),
 * or have external keys attached.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <E> element class
 */
public interface SmartPriorityQueue<E> extends SmartCollection<E> {

	/**
	 * Retrieves, but does not remove the element with the minimum key
	 * in the priority queue. If there are several elements with minimal key
	 * values, one of them is chosen arbitrarily.
	 * @return an element with a minimal key.
	 */
	public abstract E peekMin();

	/**
	 * Retrieves and remove the element with the minimum key in the priority
	 * queue. If there are several elements with minimal key values, one of
	 * them is chosen arbitrarily.
	 * @return the element with the previously minimal key.
	 */
	public abstract E extractMin();

}