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
 * A double-ended queue (deque), allowing access, removal and insertion
 * of elements both at the beginning and the end.
 * 
 * @author Malte Isberner
 *
 * @param <E> element class.
 */
public interface SmartDeque<E> extends SmartCollection<E> {

	/**
	 * Adds an element at the beginning of the sequence.
	 * @param element the element to be added.
	 * @return the reference to the newly added element.
	 */
	public abstract ElementReference pushFront(E element);

	/**
	 * Adds an element at the end of the sequence.
	 * @param element the element to be added.
	 * @return the reference to the newly added element.
	 */
	public abstract ElementReference pushBack(E element);
	
	/**
	 * Retrieves the element at the beginning of the sequence, or
	 * <code>null</code> if the sequence is empty.
	 * @return the first element or <code>null</code>.
	 */
	public abstract E getFront();

	/**
	 * Retrieves the element at the end of the sequence, or
	 * <code>null</code> if the sequence is empty.
	 * @return the last element or <code>null</code>.
	 */
	public abstract E getBack();

	
	/**
	 * Retrieves the reference to the element at the beginning of the
	 * sequence, or <code>null</code> if the sequence is empty.
	 * @return reference to the first element or <code>null</code>.
	 */
	public abstract ElementReference getFrontReference();

	/**
	 * Retrieves the reference to the element at the end of the sequence,
	 * or <code>null</code> if the sequence is empty.
	 * @return reference to the last element or <code>null</code>.
	 */
	public abstract ElementReference getBackReference();

	
	/**
	 * Retrieves and removes the element at the beginning of the sequence.
	 * If the sequence is empty, <code>null</code> is returned.
	 * @return the previously first element or <code>null</code>. 
	 */
	public E popFront();
	
	/**
	 * Retrieves and removes the element at the beginning of the sequence.
	 * If the sequence is empty, <code>null</code> is returned.
	 * @return the previously first element or <code>null</code>. 
	 */
	public abstract E popBack();

}