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

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ArrayIterator<T> implements ListIterator<T> {

    private final T[] array;
    private final int start, end;
    private int curr;

    public ArrayIterator(T[] array) {
        this(array, 0);
    }

    public ArrayIterator(T[] array, int start) {
        this(array, start, array.length);
    }

    public ArrayIterator(T[] array, int start, int end) {
        this(array, start, start, end);
    }

    public ArrayIterator(T[] array, int start, int curr, int end) {
        this.array = array;
        this.start = start;
        this.curr = curr;
        this.end = end;
    }

    @Override
    public boolean hasNext() {
        return curr < end;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return array[curr++];
    }

    @Override
    public boolean hasPrevious() {
        return curr > start;
    }

    @Override
    public T previous() {
        return array[--curr];
    }

    @Override
    public int nextIndex() {
        return curr - start;
    }

    @Override
    public int previousIndex() {
        return curr - start - 1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(T e) {
        array[curr - 1] = e;
    }

    @Override
    public void add(T e) {
        throw new UnsupportedOperationException();
    }

}
