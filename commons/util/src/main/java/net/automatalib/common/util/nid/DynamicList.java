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
package net.automatalib.common.util.nid;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

import net.automatalib.common.util.array.ArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DynamicList<T extends MutableNumericID> extends AbstractList<T> {

    private final ArrayStorage<T> storage;
    private int size;

    public DynamicList() {
        this.size = 0;
        this.storage = new ArrayStorage<>();
    }

    public DynamicList(List<? extends T> initial) {
        this.size = initial.size();
        this.storage = new ArrayStorage<>(size);

        int idx = 0;
        for (T t : initial) {
            storage.set(idx, t);
            t.setId(idx);
            idx++;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean remove(@Nullable Object elem) {
        return remove(elem, null);
    }

    @SuppressWarnings("nullness") // setting 'null' is fine, because we also decrease the size
    public boolean remove(@Nullable Object elem, @Nullable IDChangeNotifier<T> tracker) {
        if (!(elem instanceof MutableNumericID)) {
            return false;
        }
        MutableNumericID idElem = (MutableNumericID) elem;
        int idx = idElem.getId();
        T myElem = safeGet(idx);
        if (elem != myElem) {
            return false;
        }

        T last = storage.get(--size);

        if (idx != size) {
            storage.set(idx, last);
            last.setId(idx);
            if (tracker != null) {
                tracker.notifyListeners(last, idx, size);
            }
        }
        storage.set(size, null);
        myElem.setId(-1);

        return true;
    }

    @SuppressWarnings("nullness") // setting 'null' is fine, because we also decrease the size
    public T remove(int index, IDChangeNotifier<T> tracker) {
        T elem = get(index);

        T last = storage.get(--size);

        if (index != size) {
            storage.set(index, last);
            last.setId(index);
            if (tracker != null) {
                tracker.notifyListeners(last, index, size);
            }
        }
        storage.set(size, null);
        elem.setId(-1);

        return elem;
    }

    public @Nullable T safeGet(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return storage.get(index);
    }

    @Override
    public boolean add(T elem) {
        storage.ensureCapacity(size + 1);
        storage.set(size, elem);
        elem.setId(size);
        size++;
        return true;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Invalid index " + index);
        }
        return storage.get(index);
    }

    @SuppressWarnings("nullness") // setting 'null' is fine, because we also decrease the size
    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            storage.set(i, null);
        }
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            int index;

            @Override
            public boolean hasNext() {
                return index < size;
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

    public void swap(int a, int b) {
        if (a == b) {
            return;
        }
        if (a < 0 || a >= size) {
            throw new IndexOutOfBoundsException("Invalid index " + a);
        }
        if (b < 0 || b >= size) {
            throw new IndexOutOfBoundsException("Invalid index " + b);
        }
        T tmp = storage.get(a);
        storage.set(a, storage.get(b));
        storage.set(b, tmp);
        storage.get(a).setId(a);
        storage.get(b).setId(b);
    }
}
