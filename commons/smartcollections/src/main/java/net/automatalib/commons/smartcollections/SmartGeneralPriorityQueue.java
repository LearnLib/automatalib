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
import java.util.Comparator;

/**
 * A generalized priority queue which allows storing arbitrary elements that
 * don't have to be comparable, neither by their natural ordering nor by a
 * provided {@link Comparator}. Instead, keys can be assigned to the elements
 * explicitly.
 * 
 * Since this interface extends the {@link SmartCollection} (and thus also
 * the {@link Collection}) interface, it has to provide the
 * {@link SmartCollection#referencedAdd(Object)} and
 * {@link Collection#add(Object)} methods with no additional key parameters.
 * This is handled by using a <i>default key</i>, which is implicitly used 
 * for all elements inserted using the above methods. Initially, the default
 * key is <code>null</code>, whereas the <code>null</code> key is by convention
 * larger than any non-<code>null</code> key. The default key for consequent
 * insertions can be changed by calling {@link #setDefaultKey(Comparable)}. 
 * 
 * @author Malte Isberner 
 *
 * @param <E> element class.
 * @param <K> key class.
 */
public interface SmartGeneralPriorityQueue<E, K extends Comparable<K>>
	extends SmartPriorityQueue<E> {

	/**
	 * Inserts an element with the specified key.
	 * 
	 * @param elem the element to insert.
	 * @param key the key for this element.
	 * @return the reference to the inserted element.
	 */
	public abstract ElementReference add(E elem, K key);

	/**
	 * Sets the default key, which is used for elements that are inserted
	 * with no explicit key specified. 
	 * @param defaultKey the new defualt key.
	 */
	public abstract void setDefaultKey(K defaultKey);

	/**
	 * Changes the key of an element in the priority key.
	 * @param ref reference to the element whose key is to be changed.
	 * @param newKey the new key of this element.
	 */
	public abstract void changeKey(ElementReference ref, K newKey);
}
