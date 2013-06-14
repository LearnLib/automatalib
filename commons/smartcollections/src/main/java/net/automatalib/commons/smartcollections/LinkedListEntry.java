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
 * Basic interface for entries in a linked list.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <E> element class.
 * @param <T> linked list entry class.
 */
public interface LinkedListEntry<E, T extends LinkedListEntry<E,T>> 
		extends ElementReference {
	/**
	 * Retrieves the element stored at this position in the list.
	 * @return the element.
	 */
	public E getElement();
	
	/**
	 * Retrieves the previous entry in the list, or <code>null</code> if
	 * this is the first entry.
	 * @return the previous entry or <code>null</code>.
	 */
	public T getPrev();
	
	/**
	 * Retrieves the next entry in the list, or <code>null</code> if
	 * this is the last entry.
	 * @return the next entry or <code>null</code>.
	 */
	public T getNext();
	
	/**
	 * Sets the predecessor of this entry.
	 * @param prev the new predecessor.
	 */
	public void setPrev(T prev);
	
	/**
	 * Sets the successor of this entry.
	 * @param next the new successor.
	 */
	public void setNext(T next);
}
