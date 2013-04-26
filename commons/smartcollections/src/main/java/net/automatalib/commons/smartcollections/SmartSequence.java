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
 * Sequence interface. A sequence is a collection where elements are
 * stored in a specific order, and elements can be inserted in between.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <E> element class.
 */
public interface SmartSequence<E> extends SmartCollection<E> {
	
	
	/**
	 * Retrieves the reference to the preceding element, or <code>null</code> if the
	 * given reference references the first element in the list. 
	 * @param ref the reference
	 * @return the reference to the preceding element
	 */
	public ElementReference pred(ElementReference ref);
	
	/**
	 * Retrieves the reference to the succeeding element, or <code>null</code> if the
	 * given reference references the last element in the list.
	 * @param ref the reference
	 * @return the reference to the succeeding element
	 */
	public ElementReference succ(ElementReference ref);
	
	/**
	 * Inserts the given element <i>before</i> the element referenced by
	 * the specified reference.
	 * @param element the element to be added.
	 * @param ref reference to the element before which the new element
	 * is to be inserted.
	 * @return reference to the newly added element.
	 */
	public ElementReference insertBefore(E element, ElementReference ref);
	
	/**
	 * Inserts the given element <i>after</i> the element referenced by
	 * the specified reference.
	 * @param element the element to be added.
	 * @param ref reference to the element after which the new element
	 * is to be inserted.
	 * @return reference to the newly added element.
	 */
	public ElementReference insertAfter(E element, ElementReference ref);
}
