/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
package net.automatalib.common.util.array;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A type-safe wrapper around a simple {@code Object[]} array. This class is mainly useful when in need for heavily
 * generic index-based data storage. While it is compatible with {@link List}s, it does not support
 * {@link List#add(Object) dynamic growth} but {@link #ensureCapacity(int) explicit memory allocation}. As a result,
 * elements can be {@link #set(int, Object) set} at non-contiguous positions.
 *
 * @param <T>
 *         the type of stored elements
 */
public final class ArrayStorage<T> extends AbstractList<T> implements RandomAccess {

    /**
     * The default initial capacity of the array storage.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 10;

    private int size;
    private T[] storage;

    /**
     * Constructor. Creates an array storage with a default initial capacity of
     * {@link ArrayStorage#DEFAULT_INITIAL_CAPACITY}.
     */
    public ArrayStorage() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructor. Creates an array with the specified initial capacity.
     *
     * @param size
     *         the initial capacity.
     */
    public ArrayStorage(int size) {
        this(new @Nullable Object[size]);
    }

    /**
     * Constructor. (Shallowly) copies the elements from the given collections into the array storage.
     *
     * @param collection
     *         the other storage whose data should be (shallowly) cloned
     */
    public ArrayStorage(Collection<? extends T> collection) {
        this(collection.toArray());
    }

    @SuppressWarnings("unchecked")
    private ArrayStorage(@Nullable Object[] storage) {
        this.size = storage.length;
        this.storage = (T[]) storage;
    }

    /**
     * Ensures that the storage container can store at least the given amount of elements. Note that the implementation
     * may allocate more memory (to speed up subsequent increments) but elements can only ever be accessed until the
     * requested position.
     *
     * @param minCapacity
     *         the minimum capacity required
     *
     * @return {@code true} if the internal storage was re-allocated, {@code false} otherwise
     */
    public boolean ensureCapacity(int minCapacity) {
        size = Math.max(size, minCapacity);

        if (minCapacity <= storage.length) {
            return false;
        }

        final int newCapacity = ArrayUtil.computeNewCapacity(storage.length, minCapacity, 0);

        storage = Arrays.copyOf(storage, newCapacity);
        return true;
    }

    /**
     * Sets all the elements in the array to the specified value.
     *
     * @param value
     *         the value.
     */
    public void setAll(T value) {
        Arrays.fill(storage, 0, size, value);
    }

    /**
     * Swaps the contents with the given storage.
     *
     * @param that
     *         the container to swap contents with
     */
    public void swap(ArrayStorage<T> that) {
        final T[] tmpStorage = this.storage;
        final int tmpSize = this.size;

        this.storage = that.storage;
        this.size = that.size;

        that.storage = tmpStorage;
        that.size = tmpSize;
    }

    @Override
    public T get(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return storage[index];
    }

    @Override
    public T set(int index, T element) {
        final T oldValue = get(index);
        storage[index] = element;
        return oldValue;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    @SuppressWarnings("return")
    public Object[] toArray() {
        return Arrays.copyOf(storage, size);
    }

    @Override
    public Iterator<T> iterator() {
        return ArrayUtil.iterator(storage, 0, size);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArrayStorage)) {
            return false;
        }

        final ArrayStorage<?> that = (ArrayStorage<?>) o;

        if (this.size != that.size) {
            return false;
        }

        for (int i = 0; i < this.size; i++) {
            if (!Objects.equals(this.storage[i], that.storage[i])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        for (int i = 0; i < this.size; i++) {
            result = prime * result + Objects.hashCode(this.storage[i]);
        }

        return result;
    }
}
