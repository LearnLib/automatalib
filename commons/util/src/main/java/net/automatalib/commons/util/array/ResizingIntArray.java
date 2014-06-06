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
package net.automatalib.commons.util.array;


/**
 * Class that provides a resizable <tt>int</tt> array storage.
 * 
 * @author Malte Isberner
 *
 */
public final class ResizingIntArray {
	
	/**
	 * The arrays default initial capacity.
	 */
	public static final int DEFAULT_INITIAL_CAPACITY = 10;
	
	/**
	 * The storage array.
	 */
	public int[] array;
	
	private int nextCapacityHint;
	
	
	/**
	 * Constructor. Initializes an array of the default initial capacity.
	 * @see #DEFAULT_INITIAL_CAPACITY
	 */
	public ResizingIntArray() {
		this(DEFAULT_INITIAL_CAPACITY);
	}
	
	/**
	 * Constructor. Creates an array with the specified initial capacity.
	 * 
	 * @param initialCapacity the initial capacity.
	 */
	public ResizingIntArray(int initialCapacity) {
		if(initialCapacity <= 0)
			initialCapacity = DEFAULT_INITIAL_CAPACITY;
		this.array = new int[initialCapacity];
	}
	
	/**
	 * Hints the next required capacity. The next time the array is resized, it
	 * is resized to (at least) this capacity.
	 * @param nextCapacityHint the next capacity hint.
	 */
	public void hintNextCapacity(int nextCapacityHint) {
		this.nextCapacityHint = nextCapacityHint;
	}
	
	
	public boolean ensureCapacity(int minCapacity) {
		if (minCapacity <= array.length)
			return false;

		int newCapacity = (array.length * 3) / 2 + 1;
		if (newCapacity < nextCapacityHint)
			newCapacity = nextCapacityHint;

		if (newCapacity < minCapacity)
			newCapacity = minCapacity;

		int[] newArray = new int[newCapacity];
		System.arraycopy(array, 0, newArray, 0, array.length);
		array = newArray;
		nextCapacityHint = 0;
		return true;
	}
	
	public boolean shrink(int maxCapacity) {
		if(maxCapacity >= array.length)
			return false;
		
		int[] newArray = new int[maxCapacity];
		System.arraycopy(array, 0, newArray, 0, maxCapacity);
		array = newArray;
		return true;
	}

	/**
	 * Sets all the elements in the array to the specified value.
	 * @param value the value.
	 */
	public void setAll(int value) {
		for(int i = 0; i < array.length; i++)
			array[i] = value;
	}
	
	public void swap(ResizingIntArray other) {
		int[] arrayTmp = array;
		int hintTmp = nextCapacityHint;
		array = other.array;
		nextCapacityHint = other.nextCapacityHint;
		other.array = arrayTmp;
		other.nextCapacityHint = hintTmp;
	}
	
	
}
