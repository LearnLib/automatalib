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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A {@link SmartGeneralPriorityQueue} implementation that is backed by a
 * {@link SmartDynamicPriorityQueue}.
 * 
 * The default {@link SmartDynamicPriorityQueue} to be used is a
 * {@link BinaryHeap}, but every other implementation of this interface
 * may be used. The backing queue is specified in the constructor.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <E> element class.
 * @param <K> key class.
 */
public class BackedGeneralPriorityQueue<E, K extends Comparable<K>> extends AbstractSmartCollection<E>
		implements SmartGeneralPriorityQueue<E,K> {
	
	private static final int DEFAULT_INITIAL_CAPACITY = 10;
	
	private static class Entry<E,K extends Comparable<K>> implements Comparable<Entry<E,K>> {
		public E element;
		public K key;
		
		public Entry(E element, K key) {
			this.element = element;
			this.key = key;
		}

		@Override
		public int compareTo(Entry<E,K> o) {
			return key.compareTo(o.key);
		}
	}
	
	
	private static class ElementIterator<E> implements Iterator<E> {
		private final Iterator<? extends Entry<E,?>> entryIterator;
		
		public ElementIterator(Iterator<? extends Entry<E,?>> entryIterator) {
			this.entryIterator = entryIterator;
		}

		@Override
		public boolean hasNext() {
			return entryIterator.hasNext();
		}

		@Override
		public E next() {
			return entryIterator.next().element;
		}

		@Override
		public void remove() {
			entryIterator.remove();
		}
		
	}
	
	private final SmartDynamicPriorityQueue<Entry<E,K>> backingQueue;
	private K defaultKey;
	
	public BackedGeneralPriorityQueue() {
		this(DEFAULT_INITIAL_CAPACITY);
	}
	
	public BackedGeneralPriorityQueue(int initialCapacity) {
		this.backingQueue = BinaryHeap.create(initialCapacity);
	}
	
	public BackedGeneralPriorityQueue(List<? extends E> init, List<K> keys) {
		List<Entry<E,K>> entries = new ArrayList<>(init.size());
		
		Iterator<? extends E> elemIt = init.iterator();
		Iterator<K> keyIt = keys.iterator();
		
		while(elemIt.hasNext()) {
			K key = (keyIt.hasNext()) ? keyIt.next() : null;
			entries.add(new Entry<E,K>(elemIt.next(), key));
		}
		
		this.backingQueue = BinaryHeap.create(entries);
	}
	
	@SuppressWarnings("unchecked")
	public BackedGeneralPriorityQueue(Class<? extends SmartDynamicPriorityQueue<?>> backingClazz) {
		SmartDynamicPriorityQueue<?> backing;
		try {
			backing = backingClazz.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Cannot instantiate backing "
					+ "priority queue of type " + backingClazz.getName()
					+ ": " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Cannot instantiate backing "
					+ "priority queue of type " + backingClazz.getName()
					+ ": " + e.getMessage(), e);
		}
		this.backingQueue = (SmartDynamicPriorityQueue<Entry<E,K>>)backing;
	}
	
	/**
	 * Constructor. Explicitly initializes this queue with a given backing
	 * queue. Note that the provided queue must be empty and must not be used
	 * in any other way after being passed to the constructor.
	 * @param backingQueue the backing queue.
	 */
	@SuppressWarnings("unchecked")
	public BackedGeneralPriorityQueue(SmartDynamicPriorityQueue<?> backingQueue) {
		if(!backingQueue.isEmpty())
			throw new IllegalArgumentException("Backing priority queue must "
					+ "be empty upon initialization!");
		this.backingQueue = (SmartDynamicPriorityQueue<Entry<E,K>>)backingQueue;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.AbstractSmartCollection#choose()
	 */
	@Override
	public E choose() {
		Entry<E,K> entry = backingQueue.choose();
		if(entry == null)
			return null;
		return entry.element;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.AbstractSmartCollection#chooseRef()
	 */
	@Override
	public ElementReference chooseRef() {
		return backingQueue.chooseRef();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.AbstractSmartCollection#find(java.lang.Object)
	 */
	@Override
	public ElementReference find(Object element) {
		for(ElementReference ref : backingQueue.references()) {
			Entry<E,K> entry = backingQueue.get(ref);
			if(Objects.equals(entry.element, element))
				return ref;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#get(de.ls5.smartcollections.ElementReference)
	 */
	@Override
	public E get(ElementReference ref) {
		Entry<E,K> entry = backingQueue.get(ref);
		return entry.element;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#referenceIterator()
	 */
	@Override
	public Iterator<ElementReference> referenceIterator() {
		return backingQueue.referenceIterator();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#referencedAdd(java.lang.Object)
	 */
	@Override
	public ElementReference referencedAdd(E elem) {
		return add(elem, defaultKey);
	}
	
	/* (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartGeneralPriorityQueue#add(E, K)
	 */
	@Override
	public ElementReference add(E elem, K key) {
		Entry<E,K> entry = new Entry<>(elem, key);
		return backingQueue.referencedAdd(entry);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#remove(de.ls5.smartcollections.ElementReference)
	 */
	@Override
	public void remove(ElementReference ref) {
		backingQueue.remove(ref);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#replace(de.ls5.smartcollections.ElementReference, java.lang.Object)
	 */
	@Override
	public void replace(ElementReference ref, E newElement) {
		Entry<E,K> entry = backingQueue.get(ref);
		entry.element = newElement;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#clear()
	 */
	@Override
	public void clear() {
		backingQueue.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return backingQueue.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.AbstractSmartCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return new ElementIterator<>(backingQueue.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return backingQueue.size();
	}
	
	
	/* (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartGeneralPriorityQueue#setDefaultPriority(int)
	 */
	@Override
	public void setDefaultKey(K defaultKey) {
		this.defaultKey = defaultKey;
	}
	
	/* (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartGeneralPriorityQueue#changePriority(de.ls5.smartcollections.ElementReference, int)
	 */
	@Override
	public void changeKey(ElementReference ref, K newKey) {
		Entry<E,K> entry = backingQueue.get(ref);
		entry.key = newKey;
		backingQueue.keyChanged(ref);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartPriorityQueue#extractMin()
	 */
	@Override
	public E extractMin() {
		Entry<E,K> min = backingQueue.extractMin();
		return min.element;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartPriorityQueue#peekMin()
	 */
	@Override
	public E peekMin() {
		Entry<E,K> min = backingQueue.peekMin();
		return min.element;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.commons.smartcollections.AbstractSmartCollection#deepClear()
	 */
	@Override
	public void deepClear() {
		backingQueue.deepClear();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.commons.smartcollections.AbstractSmartCollection#quickClear()
	 */
	@Override
	public void quickClear() {
		backingQueue.quickClear();
	}
	
	
}
