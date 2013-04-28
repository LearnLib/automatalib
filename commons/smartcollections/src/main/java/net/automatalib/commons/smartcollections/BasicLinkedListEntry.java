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
 * Abstract base class for entries in a linked list. Takes care for handling
 * predecessor and successor, but not storage of the element itself.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <E> element class.
 * @param <T> linked list entry class.
 */
public abstract class BasicLinkedListEntry<E, T extends BasicLinkedListEntry<E,T>>
		implements LinkedListEntry<E,T> {
	// predecessor and successor
	private T prev, next;
	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.LinkedListEntry#getPrev()
	 */
	@Override
	public T getPrev() {
		return prev;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.LinkedListEntry#getNext()
	 */
	@Override
	public T getNext() {
		return next;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.LinkedListEntry#setPrev(de.ls5.collections.LinkedListEntry)
	 */
	@Override
	public void setPrev(T prev) {
		this.prev = prev;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.LinkedListEntry#setNext(de.ls5.collections.LinkedListEntry)
	 */
	@Override
	public void setNext(T next) {
		this.next = next;
	}
}
