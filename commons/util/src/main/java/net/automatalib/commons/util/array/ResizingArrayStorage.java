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

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Class that provides a resizable array storage of a certain type.
 *
 * @param <T>
 *         element class.
 *
 * @author Malte Isberner
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
     * Constructor. Creates an array storage with a default initial capacity of {@link
     * ResizingArrayStorage#DEFAULT_INITIAL_CAPACITY}.
     *
     * @param arrayClazz
     *         the class of the storage array.
     */
    public ResizingArrayStorage(Class<T[]> arrayClazz) {
        this(arrayClazz, DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructor. Creates an array with the specified initial capacity.
     *
     * @param arrayClazz
     *         the class of the storage array.
     * @param initialCapacity
     *         the initial capacity.
     */
    @SuppressWarnings("unchecked")
    public ResizingArrayStorage(Class<T[]> arrayClazz, int initialCapacity) {

        final int capacity = initialCapacity <= 0 ? DEFAULT_INITIAL_CAPACITY : initialCapacity;

        this.array = (T[]) Array.newInstance(arrayClazz.getComponentType(), capacity);
        this.arrayClazz = arrayClazz;
    }

    /**
     * Ensures that the storage has room for at least the specified number of elements.
     *
     * @param minCapacity
     *         the minimal number of elements the storage array has to provide room for.
     *
     * @return <code>true</code> iff the storage array had to be resized, <code>false</code> otherwise.
     */
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

        array = Arrays.copyOf(array, newCapacity);
        nextCapacityHint = 0;
        return true;
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

        array = Arrays.copyOf(array, maxCapacity);
        return true;
    }

    /**
     * Sets the minimum new capacity that the storage will have after the next resize.
     *
     * @param nextCapacityHint
     *         the minimum next capacity hint.
     */
    public void hintNextCapacity(int nextCapacityHint) {
        this.nextCapacityHint = nextCapacityHint;
    }

    /**
     * Sets all the elements in the array to the specified value.
     *
     * @param value
     *         the value.
     */
    public void setAll(T value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    public void swap(ResizingArrayStorage<T> other) {
        if (arrayClazz != other.arrayClazz) {
            throw new IllegalArgumentException(
                    "Cannot swap array storages of different array classes (" + arrayClazz.getSimpleName() + " vs. " +
                    other.arrayClazz.getSimpleName());
        }
        T[] arrayTmp = array;
        int hintTmp = nextCapacityHint;
        array = other.array;
        nextCapacityHint = other.nextCapacityHint;
        other.array = arrayTmp;
        other.nextCapacityHint = hintTmp;
    }
}
