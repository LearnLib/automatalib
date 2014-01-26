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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.commons.util.comparison.CmpUtil;

/**
 * A {@link PriorityQueue} implementation using a binary heap.
 *  
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <E> element class.
 */
public class BinaryHeap<E>
		extends AbstractSmartCollection<E> implements SmartDynamicPriorityQueue<E>,
			CapacityManagement {
	
	private static final int DEFAULT_INITIAL_CAPACITY = 10;
	
	/**
	 * Class for entries in a priority queue. Entry objects are returned by the
	 * {@link SmartDynamicPriorityQueue#insert(Comparable)} method and are passed to the
	 * {@link SmartDynamicPriorityQueue#keyChanged(Reference)} method. The usage of entry objects
	 * eliminates the necessity of an extra element to index mapping. 
	 * 
	 * @author Malte Isberner
	 *
	 * @param <E> element class.
	 */
	private static final class Reference<E>
			implements ElementReference {
		private int index;
		private E element;
	
		/**
		 * Constructor.
		 * 
		 * @param index the index of the entry inside the queue.
		 * @param element the element stored in this entry.
		 */
		protected Reference(int index, E element) {
			this.element = element;
			this.index = index;
		}
	}
	
	private class ReferenceIterator implements Iterator<ElementReference> {
		
		private int current;

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return (current < size);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public ElementReference next() {
			if(current >= size) {
				throw new NoSuchElementException();
			}
			return (ElementReference)entries.array[current++];
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			BinaryHeap.this.remove(--current);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <E> Reference<E> asHeapRef(ElementReference ref) {
		if(ref.getClass() != Reference.class)
			throw new InvalidReferenceException("Reference is of wrong class '"
					+ ref.getClass().getName()
					+ "', should be " + Reference.class.getName() + ".");
		return (Reference<E>)ref;
	}
	
	
	/*
	 * Retrieves, for a given child index, its parent index.
	 */
	private static int parent(int child) {
		return child/2;
	}
	
	/*
	 * Retrieves the index of the left child of a given parent index.
	 */
	private static int leftChild(int parent) {
		return 2*parent;
	}
	
	/*
	 * Retrieves the index of the right child of a given parent index.
	 */
	private static int rightChild(int parent) {
		return 2*parent + 1;
	}
	
	/*
	 * Checks if the specified index has a parent.
	 */
	private static boolean hasParent(int idx) {
		return idx > 0;
	}
	
	
	public static <E extends Comparable<E>> BinaryHeap<E> create() {
		return new BinaryHeap<>(DEFAULT_INITIAL_CAPACITY, CmpUtil.<E>naturalOrderingComparator());
	}
	
	public static <E extends Comparable<E>> BinaryHeap<E> create(int initialCapacity) {
		return new BinaryHeap<>(initialCapacity, CmpUtil.<E>naturalOrderingComparator());
	}
	
	public static <E extends Comparable<E>> BinaryHeap<E> create(Collection<? extends E> initValues) {
		return new BinaryHeap<>(0, initValues, CmpUtil.<E>naturalOrderingComparator());
	}
	
	public static <E extends Comparable<E>> BinaryHeap<E> create(int initialCapacity, Collection<? extends E> initValues) {
		return new BinaryHeap<>(initialCapacity, initValues, CmpUtil.<E>naturalOrderingComparator());
	}
	
	public static <E> BinaryHeap<E> createCmp(Comparator<? super E> comparator) {
		return new BinaryHeap<>(DEFAULT_INITIAL_CAPACITY, comparator);
	}
	
	public static <E> BinaryHeap<E> createCmp(Comparator<? super E> comparator, int initialCapacity) {
		return new BinaryHeap<>(initialCapacity, comparator);
	}
	
	public static <E> BinaryHeap<E> createCmp(Comparator<? super E> comparator, Collection<? extends E> initValues) {
		return new BinaryHeap<>(0, initValues, comparator);
	}
	
	public static <E> BinaryHeap<E> createCmp(Comparator<? super E> comparator, int initialCapacity, Collection<? extends E> initValues) {
		return new BinaryHeap<>(initialCapacity, initValues, comparator);
	}
	
	
	
	
	// Entry storage.
	private ResizingObjectArray entries;
	// Number of entries in the queue.
	private int size = 0;
	
	private final Comparator<? super E> comparator;
	
	/*
	 * Checks whether the entry at the specified index has at least one child.
	 */
	private boolean hasChildren(int idx) {
		return idx*2 < size;
	}
	
	/*
	 * Checks whether the entry at the specified index has two children.
	 */
	private boolean hasRightChild(int idx) {
		return idx*2+1 < size;
	}

	/*
	 * Removes the element at the specified index from the heap. This is
	 * done by simulating a key decrease to -infinity and then performing
	 * extractMin.
	 */
	private void remove(int index) {
		forceToTop(index);
		extractMin();
	}

	/*
	 * Compares the referenced elements.
	 */
	private int compare(Reference<E> e1, Reference<E> e2) {
		return comparator.compare(e1.element, e2.element);
	}
	
	/*
	 * Move an element upwards inside the heap, until it has a parent with a key
	 * less or equal to its own.
	 */
	@SuppressWarnings("unchecked")
	private void upHeap(int idx) {
		Reference<E> e = (Reference<E>)entries.array[idx];
		
		while(hasParent(idx)) {
			int pidx = parent(idx);
			Reference<E> p = (Reference<E>)entries.array[pidx];
			if(compare(e, p) < 0) {
				entries.array[pidx] = e;
				entries.array[idx] = p;
				p.index = idx;
				idx = parent(idx);
			}
			else
				break;		
		}
		e.index = idx;
	}
	
	/*
	 * Move an element downwards inside the heap, until all of its children have
	 * a key greater or equal to its own.
	 */
	@SuppressWarnings("unchecked")
	private void downHeap(int idx) {
		Reference<E> e = (Reference<E>)entries.array[idx];
		
		while(hasChildren(idx)) {
			int cidx = leftChild(idx);
			Reference<E> c = (Reference<E>)entries.array[cidx];
			
			if(hasRightChild(idx)) {
				int rcidx = rightChild(idx);
				Reference<E> rc = (Reference<E>)entries.array[rcidx];
				if(compare(rc, c) < 0) {
					cidx = rcidx;
					c = rc;
				}
			}
			
			if(compare(e, c) <= 0)
				break;
			
			entries.array[cidx] = e;
			entries.array[idx] = c;
			c.index = idx;
			idx = cidx;
		}
		
		e.index = idx;
	}
	
	@SuppressWarnings("unchecked")
	private void forceToTop(int idx) {
		Reference<E> e = (Reference<E>)entries.array[idx];
		
		while(hasParent(idx)) {
			int pidx = parent(idx);
			Reference<E> p = (Reference<E>)entries.array[pidx];
			entries.array[pidx] = e;
			entries.array[idx] = p;
			p.index = idx;
			idx = parent(idx);	
		}
		e.index = idx;
	}
	
	
	private void buildHeap(int numElements) {
		size = numElements;
		for(int i = numElements/2; i >= 0; i--)
			downHeap(i);
	}
	

	
	protected BinaryHeap(int initialCapacity, Comparator<? super E> comparator) {
		this.entries = new ResizingObjectArray(initialCapacity);
		this.comparator = comparator;
	}
	
	protected BinaryHeap(int initCapacity, Collection<? extends E> initValues, Comparator<? super E> comparator) {
		this(initCapacity < initValues.size() ? initValues.size() : initCapacity, comparator);
		int i = 0;
		for(E e : initValues)
			entries.array[i++] = new Reference<>(0, e);
		buildHeap(initValues.size());
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.udo.ls5.util.PriorityQueue#extractMin()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public E extractMin() {
		E min = ((Reference<E>)entries.array[0]).element;
		entries.array[0] = entries.array[--size];
		entries.array[size] = null;
		
		if(size > 0)
			downHeap(0);
		
		return min;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.udo.ls5.util.PriorityQueue#keyChanged(edu.udo.ls5.util.PriorityQueue.Entry)
	 */
	public void keyChanged(int index) {
		upHeap(index);
		downHeap(index);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.udo.ls5.util.PriorityQueue#peekMin()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public E peekMin() {
		return ((Reference<E>)entries.array[0]).element;
	}
	
	@Override
	public Reference<E> referencedAdd(E elem) {
		ensureCapacity(size+1);
		
		Reference<E> entry = new Reference<>(size, elem);
		entries.array[size] = entry;
		upHeap(size++);
		
		return entry;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartPriorityQueue#keyChanged(de.ls5.smartcollections.ElementReference)
	 */
	@Override
	public void keyChanged(ElementReference ref) {
		keyChanged(asHeapRef(ref).index);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#get(de.ls5.smartcollections.ElementReference)
	 */
	@Override
	public E get(ElementReference ref) {
		return BinaryHeap.<E>asHeapRef(ref).element;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#referenceIterator()
	 */
	@Override
	public Iterator<ElementReference> referenceIterator() {
		return new ReferenceIterator();
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#remove(de.ls5.smartcollections.ElementReference)
	 */
	@Override
	public void remove(ElementReference ref) {
		remove(asHeapRef(ref).index);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#replace(de.ls5.smartcollections.ElementReference, java.lang.Object)
	 */
	@Override
	public void replace(ElementReference ref, E newElement) {
		Reference<E> heapRef = asHeapRef(ref);
		heapRef.element = newElement;
		keyChanged(ref);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.CapacityManagement#ensureCapacity(int)
	 */
	@Override
	public boolean ensureCapacity(int minCapacity) {
		return entries.ensureCapacity(minCapacity);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.CapacityManagement#ensureAdditionalCapacity(int)
	 */
	@Override
	public boolean ensureAdditionalCapacity(int additionalCapacity) {
		return ensureCapacity(size+additionalCapacity);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.CapacityManagement#hintNextCapacity(int)
	 */
	@Override
	public void hintNextCapacity(int nextCapacityHint) {
		entries.hintNextCapacity(nextCapacityHint);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#deepClear()
	 */
	@Override
	public void deepClear() {
		entries.setAll(null);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#quickClear()
	 */
	@Override
	public void quickClear() {
		size = 0;
	}
	
	
}
