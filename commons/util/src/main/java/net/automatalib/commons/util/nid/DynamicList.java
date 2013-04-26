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
package net.automatalib.commons.util.nid;

import java.util.AbstractList;
import java.util.Iterator;

import net.automatalib.commons.util.array.SimpleResizingArray;


public class DynamicList<T extends MutableNumericID> extends
		AbstractList<T> {
	
	private final SimpleResizingArray storage
		= new SimpleResizingArray();
	
	private int size = 0;
	

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			
			int index = 0;
			
			@Override
			public boolean hasNext() {
				return (index < size);
			}

			@Override
			public T next() {
				return get(index++);
			}

			@Override
			public void remove() {
				DynamicList.this.remove(--index);
			}
			
		};
	}

	@Override
	public int size() {
		return size;
	}
	
	@Override
	public boolean isEmpty() {
		return (size == 0);
	}
	
	@Override
	public boolean add(T elem) {
		storage.ensureCapacity(size+1);
		storage.array[size] = elem;
		elem.setId(size);
		size++;
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public void swap(int a, int b) {
		if(a == b)
			return;
		if(a < 0 || a >= size)
			throw new IndexOutOfBoundsException("Invalid index " + a);
		if(b < 0 || b >= size)
			throw new IndexOutOfBoundsException("Invalid index " + b);
		Object tmp = storage.array[a];
		storage.array[a] = storage.array[b];
		storage.array[b] = tmp;
		((T)storage.array[a]).setId(a);
		((T)storage.array[b]).setId(b);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T get(int index) {
		if(index < 0 || index >= size)
			throw new IndexOutOfBoundsException("Invalid index " + index);
		return (T)storage.array[index];
	}
	
	@SuppressWarnings("unchecked")
	public T safeGet(int index) {
		if(index < 0 || index >= size)
			return null;
		return (T)storage.array[index];
	}
	
	
	@Override
	public boolean remove(Object elem) {
		return remove(elem, null);
	}
	
	public boolean remove(Object elem, IDChangeNotifier<T> tracker) {
		if(!(elem instanceof MutableNumericID))
			return false;
		MutableNumericID idElem = (MutableNumericID)elem;
		int idx = idElem.getId();
		T myElem = safeGet(idx);
		if(elem != myElem)
			return false;
		
		T last = safeGet(size-1);
		size--;
		
		if(idx != size) {
			storage.array[idx] = last;
			last.setId(idx);
			if(tracker != null)
				tracker.notifyListeners(last, idx, size);
		}
		storage.array[size] = null;
		myElem.setId(-1);
		
		return true;
	}
	
	public T remove(int index, IDChangeNotifier<T> tracker) {
		T elem = get(index);
		
		T last = safeGet(--size);
		
		if(index != size) {
			storage.array[index] = last;
			last.setId(index);
			if(tracker != null)
				tracker.notifyListeners(last, index, size);
		}
		storage.array[size] = null;
		elem.setId(-1);
		
		return elem;
	}
	
	@Override
	public void clear() {
		for(int i = 0; i < size; i++)
			storage.array[i] = null;
		size = 0;
	}

}
