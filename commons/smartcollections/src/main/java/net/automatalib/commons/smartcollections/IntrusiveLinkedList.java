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
 * An intrusive version of a linked list. When using this linked list
 * implementation, the elements themselve have to store the reference
 * to their successors and predecessors, i.e., must implement the
 * {@link LinkedListEntry} interface.
 * 
 * Note that this furthermore implies that each such element can only be
 * stored in at most <i>one</i> {@link IntrusiveLinkedList}. 
 * 
 * @author Malte Isberner 
 *
 * @param <T> element class, must implement {@link LinkedListEntry}.
 */
public class IntrusiveLinkedList<T extends LinkedListEntry<T,T>>
		extends AbstractLinkedList<T,T> {
	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.AbstractLinkedList#makeEntry(java.lang.Object)
	 */
	@Override
	protected T makeEntry(T element) {
		return element;
	}
}
