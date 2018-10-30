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
package net.automatalib.commons.smartcollections;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Class that provides a resizable {@code int} array storage.
 *
 * @author Malte Isberner
 */
public final class ResizingIntArray implements CapacityManagement, Serializable {

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
     *
     * @see #DEFAULT_INITIAL_CAPACITY
     */
    public ResizingIntArray() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructor. Creates an array with the specified initial capacity.
     *
     * @param initialCapacity
     *         the initial capacity.
     */
    public ResizingIntArray(int initialCapacity) {
        final int capacity = initialCapacity <= 0 ? DEFAULT_INITIAL_CAPACITY : initialCapacity;
        this.array = new int[capacity];
    }

    @Override
    public boolean ensureCapacity(int minCapacity) {
        if (minCapacity <= array.length) {
            return false;
        }

        final int newCapacity = ArrayUtil.computeNewCapacity(array.length, minCapacity, nextCapacityHint);

        array = Arrays.copyOf(array, newCapacity);
        nextCapacityHint = 0;
        return true;
    }

    @Override
    public boolean ensureAdditionalCapacity(int additionalCapacity) {
        return ensureCapacity(array.length + additionalCapacity);
    }

    @Override
    public void hintNextCapacity(int nextCapacityHint) {
        this.nextCapacityHint = nextCapacityHint;
    }

    /**
     * Shrinks the storage to the specified maximum capacity.
     * <p>
     * If the current capacity is less or equal to the specified capacity, nothing happens.
     *
     * @param maxCapacity
     *         the maximal number of elements the storage array has to provide room for.
     *
     * @return <code>true</code> iff the storage array had to be resized, <code>false</code> otherwise.
     */
    public boolean shrink(int maxCapacity) {
        if (maxCapacity >= array.length) {
            return false;
        }

        int[] newArray = new int[maxCapacity];
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
    public void setAll(int value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
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
