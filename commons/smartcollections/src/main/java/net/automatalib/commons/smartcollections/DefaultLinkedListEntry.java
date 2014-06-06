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
 * The default linked list entry. It provides handling of successor and
 * predecessor entries as well as storage of the actual element.
 * 
 * @author Malte Isberner 
 *
 * @param <E> element class.
 */
public class DefaultLinkedListEntry<E> 
		extends BasicLinkedListEntry<E,DefaultLinkedListEntry<E>> {
	
	// The stored element
	private E element;
	
	/**
	 * Constructor.
	 * @param element the element to be stored at this entry.
	 */
	public DefaultLinkedListEntry(E element) {
		this.element = element;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.LinkedListEntry#getElement()
	 */
	@Override
	public E getElement() {
		return element;
	}
	
	/**
	 * Sets the stored element to the specified element.
	 * @param element the new stored element.
	 */
	public void setElement(E element) {
		this.element = element;
	}
}
