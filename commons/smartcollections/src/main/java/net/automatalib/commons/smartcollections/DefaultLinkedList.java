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
 * A simple linked list implementation that allows storing
 * arbitrary elements.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <E> element class.
 */
public class DefaultLinkedList<E> extends
		AbstractLinkedList<E, DefaultLinkedListEntry<E>> {
	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.AbstractLinkedList#makeEntry(java.lang.Object)
	 */
	@Override
	protected DefaultLinkedListEntry<E> makeEntry(E element) {
		return new DefaultLinkedListEntry<E>(element);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.AbstractLinkedList#replace(de.ls5.collections.ElementReference, java.lang.Object)
	 */
	@Override
	public void replace(ElementReference ref, E newElement) {
		castRef(ref).setElement(newElement);
	}
}
