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
import java.util.AbstractList;
import java.util.RandomAccess;
import java.util.function.Supplier;

import lombok.EqualsAndHashCode;

/**
 * A thin wrapper around a simple {@code Object[]} array. Mainly used (and useful) for heavily generic and array-based
 * data storage. Extends/Implements some convenient classes/interfaces.
 *
 * @param <T>
 *         the type of stored elements
 *
 * @author frohme
 */
@EqualsAndHashCode(callSuper = false)
public class ArrayStorage<T> extends AbstractList<T> implements RandomAccess, Serializable, Cloneable {

    private final Object[] storage;

    public ArrayStorage(int size) {
        this.storage = new Object[size];
    }

    public ArrayStorage(int size, Supplier<T> supplier) {
        this.storage = new Object[size];
        for (int i = 0; i < size; i++) {
            storage[i] = supplier.get();
        }
    }

    private ArrayStorage(Object[] storage) {
        this.storage = storage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) storage[index];
    }

    @Override
    public T set(int index, T element) {
        final T oldValue = get(index);
        storage[index] = element;
        return oldValue;
    }

    @Override
    public int size() {
        return storage.length;
    }

    @Override
    public ArrayStorage<T> clone() {
        return new ArrayStorage<>(storage.clone());
    }
}
