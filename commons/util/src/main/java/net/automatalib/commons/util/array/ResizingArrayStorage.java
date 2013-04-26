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
package net.automatalib.commons.util.array;


import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Class that provides a resizable array storage of a certain type.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <T> element class.
 */
public class ResizingArrayStorage<T> {
	/**
	 * The default initial capacity of the array storage.
	 */
	public static final int DEFAULT_INITIAL_CAPACITY = 10;
	
	private final Class<T[]> arrayClazz;
	public T[] array;
	private int nextCapacityHint;
	
	/**
	 * Constructor. Creates an array storage with a default initial
	 * capacity of {@link ResizingArrayStorage#DEFAULT_INITIAL_CAPACITY}.
	 * 
	 * @param arrayClazz the class of the storage array.
	 */
	public ResizingArrayStorage(Class<T[]> arrayClazz) {
		this(arrayClazz, DEFAULT_INITIAL_CAPACITY);
	}
	
	/**
	 * Constructor. Creates an array with the specified initial capacity.
	 * 
	 * @param arrayClazz the class of the storage array.
	 * @param initialCapacity the initial capacity.
	 */
	@SuppressWarnings("unchecked")
	public ResizingArrayStorage(Class<T[]> arrayClazz, int initialCapacity) {
		if(initialCapacity <= 0)
			initialCapacity = DEFAULT_INITIAL_CAPACITY;
		this.array = (T[])Array.newInstance(arrayClazz.getComponentType(),
				initialCapacity);
		this.arrayClazz = arrayClazz;
	}
	
	
	/**
	 * Ensures that the storage has room for at least the specified number
	 * of elements.
	 * 
	 * @param minCapacity the minimal number of elements the storage array
	 * has to provide room for.
	 * @return <code>true</code> iff the storage array had to be resized,
	 * <code>false</code> otherwise.
	 */
	public boolean ensureCapacity(int minCapacity) {
		if (minCapacity <= array.length)
			return false;

		int newCapacity = (array.length * 3) / 2 + 1;
		if (newCapacity < nextCapacityHint)
			newCapacity = nextCapacityHint;

		if (newCapacity < minCapacity)
			newCapacity = minCapacity;

		array = Arrays.copyOf(array, newCapacity);
		nextCapacityHint = 0;
		return true;
	}
	
	/**
	 * Shrinks the storage to the specified maximum capacity.
	 * 
	 * If the current capacity is less or equal to the specified capacity,
	 * nothing happens.
	 * 
	 * @param maxCapacity the maximal number of elements the storage array
	 * has to provide room for.
	 * @return <code>true</code> iff the storage array had to be resized,
	 * <code>false</code> otherwise.
	 */
	public boolean shrink(int maxCapacity) {
		if(maxCapacity >= array.length)
			return false;
		
		array = Arrays.copyOf(array, maxCapacity);
		return true;
	}
	
	/**
	 * Sets the minimum new capacity that the storage will have
	 * after the next resize. 
	 * @param nextCapacityHint the minimum next capacity hint.
	 */
	public void hintNextCapacity(int nextCapacityHint) {
		this.nextCapacityHint = nextCapacityHint;
	}

	/**
	 * Sets all the elements in the array to the specified value.
	 * @param value the value.
	 */
	public void setAll(T value) {
		for(int i = 0; i < array.length; i++)
			array[i] = value;
	}
	
	public void swap(ResizingArrayStorage<T> other) {
		if(arrayClazz != other.arrayClazz)
			throw new IllegalArgumentException("Cannot swap array storages of different array classes (" + arrayClazz.getSimpleName() + " vs. " + other.arrayClazz.getSimpleName());
		T[] arrayTmp = array;
		int hintTmp = nextCapacityHint;
		array = other.array;
		nextCapacityHint = other.nextCapacityHint;
		other.array = arrayTmp;
		other.nextCapacityHint = hintTmp;
	}
}
