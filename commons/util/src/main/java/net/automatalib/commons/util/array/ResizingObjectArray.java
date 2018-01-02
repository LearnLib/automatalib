/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.commons.util.array;

import java.io.Serializable;

/**
 * Class that provides a resizable {@link Object} array storage.
 * <p>
 * Unlike {@link ResizingArrayStorage}, the array provided by this class is always of type <code>Object[]</code>. This
 * results in a higher efficiency, since there is no need for reflection when creating new arrays.
 *
 * @author Malte Isberner
 */
public final class ResizingObjectArray implements Serializable {

    /**
     * The arrays default initial capacity.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 10;

    /**
     * The storage array.
     */
    public Object[] array;

    private int nextCapacityHint;

    /**
     * Constructor. Initializes an array of the default initial capacity.
     *
     * @see #DEFAULT_INITIAL_CAPACITY
     */
    public ResizingObjectArray() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructor. Creates an array with the specified initial capacity.
     *
     * @param initialCapacity
     *         the initial capacity.
     */
    public ResizingObjectArray(int initialCapacity) {
        final int capacity = initialCapacity <= 0 ? DEFAULT_INITIAL_CAPACITY : initialCapacity;
        this.array = new Object[capacity];
    }

    /**
     * Hints the next required capacity. The next time the array is resized, it is resized to (at least) this capacity.
     *
     * @param nextCapacityHint
     *         the next capacity hint.
     */
    public void hintNextCapacity(int nextCapacityHint) {
        this.nextCapacityHint = nextCapacityHint;
    }

    public boolean ensureCapacity(int minCapacity) {
        if (minCapacity <= array.length) {
            return false;
        }

        int newCapacity = (array.length * 3) / 2 + 1;
        if (newCapacity < nextCapacityHint) {
            newCapacity = nextCapacityHint;
        }

        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }

        Object[] newArray = new Object[newCapacity];
        System.arraycopy(array, 0, newArray, 0, array.length);
        array = newArray;
        nextCapacityHint = 0;
        return true;
    }

    public boolean shrink(int maxCapacity) {
        if (maxCapacity >= array.length) {
            return false;
        }

        Object[] newArray = new Object[maxCapacity];
        System.arraycopy(array, 0, newArray, 0, maxCapacity);
        array = newArray;
        return true;
    }

    /**
     * Sets all the elements in the array to the specified value.
     *
     * @param value
     *         the value.
     */
    public void setAll(Object value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    public void swap(ResizingObjectArray other) {
        Object[] arrayTmp = array;
        int hintTmp = nextCapacityHint;
        array = other.array;
        nextCapacityHint = other.nextCapacityHint;
        other.array = arrayTmp;
        other.nextCapacityHint = hintTmp;
    }

}
