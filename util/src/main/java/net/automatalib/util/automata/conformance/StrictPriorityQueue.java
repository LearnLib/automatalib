/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.util.automata.conformance;

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.automatalib.commons.util.array.ResizingObjectArray;


/**
 * A priority queue which enforces that no two elements that it contains
 * are equal wrt. the specified comparator (i.e., {@link Comparator#compare(Object, Object)}
 * does not return {@code 0} for two distinct elements).
 * <p>
 * If an element is inserted which, according to the {@link Comparator}, is already present,
 * the specified {@link MergeOperation}'s {@link MergeOperation#merge(Object, Object)} method
 * is invoked to determine the replacement element.
 * <p>
 * The name derives from the fact that subsequent calls to {@link #extractMin()} will yield
 * a <i>strictly</i> growing sequence of elements.
 * <p>
 * This class does not disallow {@code null} values, but the supplied {@link Comparator}
 * has to support them.
 * 
 * @author Malte Isberner
 *
 * @param <E> element type
 */
public class StrictPriorityQueue<E> extends AbstractQueue<E> {
	
	/**
	 * The merge operation two perform on two equally-ranked elements.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <E> element type
	 */
	public static interface MergeOperation<E> {
		/**
		 * Merges the old element and the new element into a replacement element.
		 * <p>
		 * Implementations can assume that {@code cmp.compare(oldObject, newObject) == 0}
		 * holds for the comparator {@code cmp} specified in
		 * {@link StrictPriorityQueue#StrictPriorityQueue(Comparator, MergeOperation)}. In turn,
		 * they must guarantee that also {@code cmp.compare(result, oldObject) == 0} holds
		 * for the return value {@code result}.
		 * 
		 * @param oldObject the old element
		 * @param newObject the new element
		 * @return the replacement element
		 */
		public E merge(E oldObject, E newObject);
	}
	
	private final ResizingObjectArray storage = new ResizingObjectArray();
	private int size = 0;
	private final Comparator<? super E> comparator;
	private final MergeOperation<E> mergeOp; 
	
	
	/**
	 * Constructor.
	 * @param comparator the comparator used to compare elements
	 * @param mergeOp the merge operation to perform for equally-ranked elements
	 */
	public StrictPriorityQueue(Comparator<? super E> comparator, MergeOperation<E> mergeOp) {
		this.comparator = comparator;
		this.mergeOp = mergeOp;
	}
	
	/**
	 * Inserts an element into the queue.
	 * @param object the element to insert
	 * @return {@code true} if a new element has been inserted (i.e., the size has grown),
	 * {@code false} otherwise (i.e., an existing element has been replaced)
	 */
	public boolean insert(E object) {
		storage.ensureCapacity(size + 1);
		storage.array[size++] = object;
		if(!upHeap()) {
			size--;
			return false;
		}
		return true;
	}
	
	/**
	 * Retrieves, but does not remove the element at the head of the queue (i.e., the minimum
	 * element in the queue).
	 * <p>
	 * Note: Unlike {@link #peek()}, this method throws a {@link NoSuchElementException}
	 * in case of an empty priority queue.
	 * @return the minimum element in the queue
	 */
	@SuppressWarnings("unchecked")
	public E peekMin() {
		if(size == 0) {
			throw new NoSuchElementException();
		}
		return (E)storage.array[0];
	}
	
	/**
	 * Retrieves and removes the element at the head of the queue (i.e., the minimum element
	 * in the queue).
	 * <p>
	 * Note: Unlike {@link #poll()}, this method throws a {@link NoSuchElementException}
	 * in case of an empty priority queue.
	 * @return the minimum element in the queue
	 */
	@SuppressWarnings("unchecked")
	public E extractMin() {
		if(size == 0) {
			throw new NoSuchElementException();
		}
		E result = (E)storage.array[0];
		size--;
		if(size > 0) {
			storage.array[0] = storage.array[size];
			downHeap();
		}
		storage.array[size] = 0;
		
		return result;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return size > 0;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	@Override
	public String toString() {
		if(size == 0) {
			return "[]";
		}
		StringBuilder result = new StringBuilder();
		result.append("[").append(storage.array[0]);
		for(int i = 1; i < size; i++) {
			result.append(',');
			result.append(storage.array[i]);
		}
		result.append("]");
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	@Override
	public boolean offer(E e) {
		return insert(e);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Queue#poll()
	 */
	@Override
	public E poll() {
		if(size == 0) {
			return null;
		}
		return extractMin();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Queue#peek()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public E peek() {
		if(size == 0) {
			return null;
		}
		return (E)storage.array[0];
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			private int idx = 0;
			@Override
			public boolean hasNext() {
				return (idx < size);
			}
			@Override
			@SuppressWarnings("unchecked")
			public E next() {
				return (E)storage.array[idx++];
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return size;
	}
	
	/**
	 * Sifts the topmost element down into the heap until the
	 * heap condition is restored.
	 */
	@SuppressWarnings("unchecked")
	private void downHeap() {
		E elem = (E)storage.array[0];
		int currIdx = 0;
		
		while(2*currIdx < size) {
			int leftChildIdx = 2*currIdx;
			E leftChild = (E)storage.array[leftChildIdx];
			if(comparator.compare(elem, leftChild) > 0) {
				storage.array[currIdx] = leftChild;
				storage.array[leftChildIdx] = elem;
				currIdx = leftChildIdx;
			}
			else if(2*currIdx + 1 < size) {
				int rightChildIdx = 2*currIdx + 1;
				E rightChild = (E)storage.array[rightChildIdx];
				if(comparator.compare(elem, rightChild) > 0) {
					storage.array[currIdx] = rightChild;
					storage.array[rightChildIdx] = elem;
					currIdx = rightChildIdx;
				}
				else {
					return;
				}
			}
			else {
				return;
			}
		}
	}
	
	/**
	 * Moves the last element upwards in the heap until the heap condition
	 * is restored.
	 * @return {@code true} if the element has been inserted, {@code false} if it
	 * has been merged with an existing element.
	 */
	@SuppressWarnings("unchecked")
	private boolean upHeap() {
		int currIdx = size - 1;
		E elem = (E)storage.array[currIdx];
		
		int steps = 0;
		
		while(currIdx > 0) {
			int parentIdx = currIdx / 2;
			E parent = (E)storage.array[parentIdx];
			int cmp = comparator.compare(elem, parent);
			if(cmp == 0) {
				storage.array[parentIdx] = mergeOp.merge(parent, elem);
				return false;
			}
			else if(cmp > 0) {
				break;
			}
			
			currIdx = parentIdx;
			steps++;
		}
		
		currIdx = size - 1;
		for(int i = 0; i < steps; i++) {
			int parentIdx = currIdx / 2;
			storage.array[currIdx] = storage.array[parentIdx];
			currIdx = parentIdx;
		}
		storage.array[currIdx] = elem;
		
		return true;
	}

}
