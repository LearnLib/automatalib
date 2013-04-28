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

import java.util.Iterator;


/**
 * Abstract base class for linked lists.
 * 
 * This class implements the base functionality for dealing with
 * linked lists of elements implementing the {@link LinkedListEntry}
 * interface. It provides the logic for the basic operations (esp.
 * the (re-/un-)linking of elements), but not how entries into the
 * lists are created. Therefore, it can be used by both 
 * intrusive and non-intrusive linked lists.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <E> element type
 * @param <T> linked list entry type
 * 
 * @see IntrusiveLinkedList
 * @see DefaultLinkedList
 */
public abstract class AbstractLinkedList<E, T extends LinkedListEntry<E, T>>
extends AbstractSmartCollection<E> implements
SmartSequence<E> {

	
	// head element (may be null if list is empty)
	private T head;
	// last element (may be null if list is empty)
	private T last;
	// number of elements in the list
	private int size;

	/**
	 * Iterator that follows the linked structure of the
	 * elements.
	 * 
	 * @author Malte Isberner <malte.isberner@cs.uni-dortmund.de>
	 */
	private class LinkedListEntryIterator
	implements Iterator<T> {
		// current entry
		private T current;

		/*
		 * Constructor.
		 */
		public LinkedListEntryIterator(T head) {
			this.current = head;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return (current != null);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next() {
			T e = current;
			current = current.getNext();
			return e;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			T next = current.getNext();
			removeEntry(current);
			current = next;
		}
	}
	
	private class ElementIterator
	implements Iterator<E> {
		// current entry
		private T current;

		/*
		 * Constructor.
		 */
		public ElementIterator(T head) {
			this.current = head;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return (current != null);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public E next() {
			E e = current.getElement();
			current = current.getNext();
			return e;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			T next = current.getNext();
			removeEntry(current);
			current = next;
		}
	}



	/**
	 * Adds an entry at the beginning of the list.
	 * @param e the entry to add.
	 */
	protected void pushFrontEntry(T e) {
		e.setPrev(null);
		e.setNext(head);
		if (head != null)
			head.setPrev(e);
		else
			last = e;
		head = e;
		size++;
	}

	/**
	 * Adds an entry at the end of the list.
	 * @param e the entry to add.
	 */
	protected void pushBackEntry(T e) {
		e.setNext(null);
		e.setPrev(last);
		if (last != null)
			last.setNext(e);
		else
			head = e;
		last = e;
		size++;
	}

	/**
	 * Retrieves the first entry in the list, or <code>null</code>
	 * if the list is empty.
	 * @return the first entry or <code>null</code>.
	 */
	protected T getFrontEntry() {
		return head;
	}

	/**
	 * Retrieves the last entry in the list, or <code>null</code>
	 * if the list is empty.
	 * @return the first entry or <code>null</code>.
	 */
	protected T getBackEntry() {
		return last;
	}

	/**
	 * Removes and returns the first entry in the list. If the
	 * list is empty, it remains unmodified and <code>null</code>
	 * is returned.
	 * @return the previously first entry in the list, or
	 * <code>null</code>.
	 */
	protected T popFrontEntry() {
		if (head == null)
			return null;
		T next = head.getNext();
		if (next != null)
			next.setPrev(null);
		else
			last = null;
		T e = head;
		head = next;
		e.setNext(null);
		size--;
		return e;
	}

	/**
	 * Removes and returns the last entry in the list. If the
	 * list is empty, it remains unmodified and <code>null</code>
	 * is returned.
	 * @return the previously first entry in the list, or
	 * <code>null</code>.
	 */
	protected T popBackEntry() {
		if (last == null)
			return null;
		T prev = last.getPrev();
		if (prev != null)
			prev.setNext(null);
		else
			head = null;
		T e = last;
		last = prev;
		e.setPrev(null);
		size--;
		return e;
	}

	/**
	 * Inserts a new entry <i>before</i> a given one.
	 * @param e the entry to add.
	 * @param insertPos the entry before which to add the new one.
	 */
	protected void insertBeforeEntry(T e, T insertPos) {
		T oldPrev = insertPos.getPrev();
		e.setNext(insertPos);
		e.setPrev(oldPrev);
		insertPos.setPrev(e);
		if (oldPrev != null)
			oldPrev.setNext(e);
		else
			head = e;
		size++;
	}

	/**
	 * Inserts a new entry <i>after</i> a given one.
	 * @param e the entry to add.
	 * @param insertPos the entry before which to add the new one.
	 */
	protected void insertAfterEntry(T e, T insertPos) {
		T oldNext = insertPos.getNext();
		e.setNext(oldNext);
		e.setPrev(insertPos);
		insertPos.setNext(e);
		if (oldNext != null)
			oldNext.setPrev(e);
		else
			last = e;
		size++;
	}

	/**
	 * Removes an entry from the list.
	 * @param entry the entry to remove.
	 */
	protected void removeEntry(T entry) {
		T prev = entry.getPrev();
		T next = entry.getNext();
		if (prev != null)
			prev.setNext(next);
		else
			head = next;
		if (next != null)
			next.setPrev(prev);
		else
			last = prev;
		size--;
	}

	/**
	 * Replaces an entry in the list.
	 * @param oldEntry the entry to be replaced. 
	 * @param newEntry the replacement entry.
	 */
	protected void replaceEntry(T oldEntry, T newEntry) {
		T prev = oldEntry.getPrev();
		T next = newEntry.getNext();
		if(prev != null)
			prev.setNext(newEntry);
		else
			head = newEntry;
		if(next != null)
			next.setPrev(newEntry);
		else
			last = newEntry;
	}


	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return (head == null);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#clear()
	 */
	@Override
	public void clear() {
		head = last = null;
		size = 0;
	}

	/**
	 * Deprecated. Use {@link #concat(AbstractLinkedList)}.
	 */
	@Deprecated
	public void addCompletely(AbstractLinkedList<? extends E, ? extends T> other) {
		concat(other);
	}

	/**
	 * Concatenates two linked lists. All elements of the specified list
	 * (which will be empty afterwards) are added at the end of this list.
	 * This operation runs in constant time. 
	 * @param other the list to append,
	 */
	public void concat(AbstractLinkedList<? extends E,? extends T> other) {
		if (other.isEmpty())
			return;

		if (isEmpty()) {
			head = other.head;
			last = other.last;
		} else {
			last.setNext(other.head);
			other.head.setPrev(last);
			last = other.last;
		}
		size += other.size;
		other.clear();
	}



	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.AbstractSmartCollection#choose()
	 */
	@Override
	public E choose() {
		return head.getElement();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.AbstractSmartCollection#chooseRef()
	 */
	@Override
	public ElementReference chooseRef() {
		return head;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#get(de.ls5.smartcollections.ElementReference)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public E get(ElementReference ref) {
		return ((T)ref).getElement();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#referenceIterator()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Iterator<ElementReference> referenceIterator() {
		return (Iterator<ElementReference>)(Iterator<?>)new LinkedListEntryIterator(head);
	}
	
	@Override
	public Iterator<E> iterator() {
		return new ElementIterator(head);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#referencedAdd(java.lang.Object)
	 */
	@Override
	public ElementReference referencedAdd(E elem) {
		T entry = makeEntry(elem);
		pushBackEntry(entry);
		return entry;
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartCollection#remove(de.ls5.smartcollections.ElementReference)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void remove(ElementReference elem) {
		removeEntry((T)elem);
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
	 * @see de.ls5.smartcollections.SmartCollection#replace(de.ls5.smartcollections.ElementReference, java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void replace(ElementReference ref, E newElement) {
		T newEntry = makeEntry(newElement);
		replaceEntry((T)ref, newEntry);
	}


	/**
	 * Retrieves the last element in the list. If the list is empty,
	 * a {@link NullPointerException} may be thrown.
	 * @return the last element in the list.
	 */
	public E getBack() {
		return last.getElement();
	}

	/**
	 * Retrieves a reference to the last element in the list. If the list is
	 * empty, <code>null</code> is returned.
	 * @return a reference to the last element, or <code>null</code>.
	 */
	public ElementReference getBackReference() {
		return last;
	}

	/**
	 * Retrieves the first element in the list. If the list is empty,
	 * a {@link NullPointerException} may be thrown.
	 * @return the first element in the list.
	 */
	public E getFront() {
		return head.getElement();
	}

	/**
	 * Retrieves a reference to the first element in the list. If the list is
	 * empty, <code>null</code> is returned.
	 * @return a reference to the first element, or <code>null</code>.
	 */
	public ElementReference getFrontReference() {
		return head;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartSequence#insertAfter(java.lang.Object, de.ls5.smartcollections.ElementReference)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ElementReference insertAfter(E element, ElementReference ref) {
		T entry = makeEntry(element);
		insertAfterEntry(entry, (T)ref);
		return entry;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartSequence#insertBefore(java.lang.Object, de.ls5.smartcollections.ElementReference)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ElementReference insertBefore(E element, ElementReference ref) {
		T entry = makeEntry(element);
		insertBeforeEntry(entry, (T)ref);
		return entry;
	}

	/**
	 * Retrieves and removes the last element in the list. If the list is
	 * empty, a {@link NullPointerException} may be thrown.
	 * @return the formerly last element in the list.
	 */
	public E popBack() {
		return popBackEntry().getElement();
	}

	/**
	 * Retrieves and removes the first element in the list. If the list is
	 * empty, a {@link NullPointerException} may be thrown.
	 * @return the formerly first element in the list.
	 */
	public E popFront() {
		return popFrontEntry().getElement();
	}

	/**
	 * Adds an element at the end of the list.
	 * @param element the element to add.
	 * @return a reference to the newly added element.
	 */
	public ElementReference pushBack(E element) {
		T entry = makeEntry(element);
		pushBackEntry(entry);
		return entry;
	}

	/**
	 * Adds an element at the beginning of the list.
	 * @param element the element to add.
	 * @return a reference to the newly added element.
	 */
	public ElementReference pushFront(E element) {
		T entry = makeEntry(element);
		pushFrontEntry(entry);
		return entry;
	}

	/**
	 * Creates (if necessary) a {@link LinkedListEntry} for the given element.
	 * For intrusive linked lists, e.g., the argument itself is returned.
	 * @param element the element for which to retrieve an entry.
	 * @return the entry for the given element.
	 */
	protected abstract T makeEntry(E element);


	/**
	 * Helper function for casting a general {@link ElementReference}
	 * to the specific linked list entry type.
	 * @param ref the reference.
	 * @return the argument cast to the entry type.
	 */
	@SuppressWarnings("unchecked")
	protected T castRef(ElementReference ref) {
		return (T)ref;
	}
	

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartSequence#pred(de.ls5.smartcollections.ElementReference)
	 */
	@Override
	public ElementReference pred(ElementReference ref) {
		return castRef(ref).getPrev();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.SmartSequence#succ(de.ls5.smartcollections.ElementReference)
	 */
	@Override
	public ElementReference succ(ElementReference ref) {
		return castRef(ref).getNext();
	}

	/**
	 * Swaps the contents of two linked lists with the same entry types.
	 * This method runs in constant time.
	 * @param other the other list to swap contents with.
	 */
	public void swap(AbstractLinkedList<E,T> other) {
		int sizeTmp = this.size;
		T headTmp = this.head;
		T lastTmp = this.last;
		this.size = other.size;
		this.head = other.head;
		this.last = other.last;
		other.size = sizeTmp;
		other.head = headTmp;
		other.last = lastTmp;
	}

}
