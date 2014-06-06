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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;


/**
 * This class eases the implementation of the {@link SmartCollection}
 * interface. It is comparable to {@link AbstractCollection} from the
 * Java Collections Framework.
 * 
 * A class extending this abstract class has to implement the following
 * methods:
 * - {@link Collection#size()}
 * - {@link SmartCollection#get(ElementReference)}
 * - {@link SmartCollection#referenceIterator()}
 * - {@link SmartCollection#referencedAdd(Object)}
 * - {@link SmartCollection#remove(ElementReference)}
 * - {@link SmartCollection#replace(ElementReference, Object)}
 * 
 * @author Malte Isberner 
 *
 * @param <E> element class.
 */
public abstract class AbstractSmartCollection<E> extends AbstractCollection<E> implements SmartCollection<E> {
	
	/*
	 * An iterator for iterating over the concrete elements, based on the
	 * iteration over the element references.
	 */
	private class DeRefIterator implements Iterator<E> {
		// the reference iterator
		private Iterator<ElementReference> refIterator;
		
		/**
		 * Constructor.
		 * @param refIterator the reference iterator.
		 */
		public DeRefIterator(Iterator<ElementReference> refIterator) {
			this.refIterator = refIterator;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return refIterator.hasNext();
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public E next() {
			return get(refIterator.next());
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			refIterator.remove();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.SmartCollection#addAll(java.lang.Iterable)
	 */
	@Override
	public void addAll(Iterable<? extends E> iterable) {
		for(E e : iterable)
			add(e);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.SmartCollection#addAll(T[])
	 */
	@Override
	public <T extends E> void addAll(T[] array) {
		for(T t : array)
			add(t);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.SmartCollection#choose()
	 */
	@Override
	public E choose() {
		return iterator().next();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.SmartCollection#chooseRef()
	 */
	@Override
	public ElementReference chooseRef() {
		return referenceIterator().next();
	}


	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	@Override
	public boolean add(E e) {
		referencedAdd(e);
		return true;
	}

	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.SmartCollection#references()
	 */
	@Override
	public Iterable<ElementReference> references() {
		return new Iterable<ElementReference>() {
			@Override
			public Iterator<ElementReference> iterator() {
				return referenceIterator();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return new DeRefIterator(referenceIterator());
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.collections.SmartCollection#find(java.lang.Object)
	 */
	@Override
	public ElementReference find(Object element) {
		for(ElementReference ref : references()) {
			E current = get(ref);
			if(Objects.equals(current, element))
				return ref;
		}
		
		return null;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object element) {
		ElementReference ref = find(element);
		if(ref == null)
			return false;
		remove(ref);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#deepClear()
	 */
	@Override
	public void deepClear() {
		clear();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#quickClear()
	 */
	@Override
	public void quickClear() {
		clear();
	}
	
	
}
