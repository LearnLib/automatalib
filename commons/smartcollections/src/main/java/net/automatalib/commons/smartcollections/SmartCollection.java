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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An extended collection interface.
 * 
 * This interface overcomes various shortcomings of the {@link Collection}
 * interface from the Java Collections Framework, and also introduces
 * other features not present in other libraries (such as the Apache
 * Commons Collections Library).
 * 
 * Efficiently operating on collections data structures is often hampered
 * by the insufficient interface provided by the standard Java collections.
 * 
 * For example, linked lists allow constant time removal if the element
 * to be removed is known. However, using {@link List#remove(int)} requires
 * linear time in the provided parameter (and thus, in the worst case, linear
 * time in the size of the list). Removal in constant time is possible when
 * iterating manually using the {@link Iterator#remove()} method, but this
 * is not only inconvenient, but also does not work if one wants to remove
 * the elements later, because {@link Iterator}s can't be cloned, and
 * additionally are invalidated by other modifications of the underlying
 * collection during their existence.
 * 
 * This collection interface introduces a <i>reference</i> concept: References
 * (represented by the marker interface {@link ElementReference}) to the
 * elements allow efficient (in terms of what the data structure itself
 * supports) operations on the elements, if the reference to the respective
 * element is known. References can be acquired right at the point when an
 * element is added to the collection (using {@link #referencedAdd(Object)}),
 * by explicitly searching for an element (using {@link #find(Object)}) or
 * during iteration (using the {@link #referenceIterator()} resp.
 * {@link #references()} method). 
 * 
 * The validity of references is retained through all operations on the
 * collection, except for those that cause removal of the respective elements. 
 * 
 * @author Malte Isberner 
 *
 * @param <E> element class
 */
@ParametersAreNonnullByDefault
public interface SmartCollection<E> extends Collection<E> {
	
	/**
	 * Retrieves an element by its reference.
	 * 
	 * If the reference belongs to another collection, the behavior
	 * is undefined.
	 * 
	 * @param ref the element's reference.
	 * @return the element.
	 */
	@Nullable
	public E get(ElementReference ref);
	
	/**
	 * Adds an element to the collection, returning a reference to the
	 * newly added element. If the collection does not support containing
	 * the same element multiple times, a reference to the previously
	 * existing element is returned.
	 * 
	 * @param elem the element to be added.
	 * @return a reference to this element in the collection.
	 */
	@Nonnull
	public ElementReference referencedAdd(@Nullable E elem);
	
	/**
	 * Removes an element (by its reference) from the collection.
	 * 
	 * If the reference does not belong to this collection, the behavior
	 * is undefined.
	 * 
	 * @param elem the reference to the element to be removed.
	 */
	public void remove(ElementReference elem);
	
	/**
	 * Retrieves an arbitrary element from the collection. If the collection
	 * is empty, a {@link NoSuchElementException} is thrown
	 * 
	 * @return an arbitrary element from the collection
	 */
	@Nullable
	public E choose() throws NoSuchElementException;
	
	/**
	 * Retrieves the reference to an arbitrary element from the collection.
	 * If the collection is empty, a {@link NoSuchElementException}
	 * is thrown.
	 * 
	 * @return the reference to an arbitrary element in the collection
	 */
	@Nonnull
	public ElementReference chooseRef();
	
	/**
	 * This function is deprecated and should not be used, in favor of
	 * the removal by reference {@link #remove(ElementReference)}.
	 * @see Collection#remove(Object)
	 */
	@Deprecated
	@Override
	public boolean remove(@Nullable Object element);
	
	/**
	 * Retrieves an iterator for iterating over the references of elements
	 * in this collection.
	 * @return the reference iterator.
	 */
	@Nonnull
	public Iterator<ElementReference> referenceIterator();
	
	/**
	 * This is a method provided for convenience, which allows iterating
	 * over the element references using a <i>foreach</i>-style
	 * <code>for</code>-loop.
	 * 
	 * @return an {@link Iterable} with the above {@link #referenceIterator()}
	 * as its iterator.
	 */
	@Nonnull
	public Iterable<ElementReference> references();
	
	/**
	 * Adds all elements from a given iterable. Note that this may be
	 * inefficient, compared to adding a {@link Collection}, because the
	 * number of elements to be added is not known a priori.
	 * 
	 * @param iterable the iterable of elements to add.
	 */
	public void addAll(Iterable<? extends E> iterable);
	
	/**
	 * Adds all elements from the specified array.
	 * @param <T> array element class, may be a subclass of <code>E</code>.
	 * @param array the array of elements to be added.
	 */
	public <T extends E> void addAll(T[] array);
	
	/**
	 * Replaces the element referenced by the given reference with
	 * the specified element.
	 * 
	 * @param ref the reference of the element to be replaced.
	 * @param newElement the replacement.
	 */
	public void replace(ElementReference ref, @Nullable E newElement);
	
	/**
	 * Retrieves the reference for a given element. If the element is not
	 * contained in the collection, <code>null</code> is returned.
	 * 
	 * @param element the element to search for.
	 * @return the reference to this element, or <code>null</code>.
	 */
	public ElementReference find(@Nullable Object element);
	
	/**
	 * Quickly clears this collection. This method is supposed to perform
	 * the minimum amount of effort such that this collection is emptied,
	 * disregarding all other side-effects such as referencing or garbage
	 * collection issues.
	 * 
	 * Depending on the implementation, this may be just the same as
	 * {@link Collection#clear()}. However, this could also have side-effects
	 * like hampering the garbage collection or such.
	 * 
	 * After calling this method, even a call of the normal
	 * {@link Collection#clear()} is not guaranteed to fix all these issues.
	 * This can only be achieved by the method {@link #deepClear()} below.
	 */
	public void quickClear();
	
	/**
	 * Thoroughly clears the collection, fixing all issues that may have been
	 * caused by a call of the above {@link #quickClear()}. 
	 */
	public void deepClear();
}
