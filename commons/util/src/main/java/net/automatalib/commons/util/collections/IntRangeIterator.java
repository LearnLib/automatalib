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
package net.automatalib.commons.util.collections;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class IntRangeIterator implements ListIterator<Integer> {

    private final int low;
    private final int step;
    private final int size;
    private int curr;

    public IntRangeIterator(int low, int step, int size) {
        this(low, step, size, 0);
    }

    public IntRangeIterator(int low, int step, int size, int startIdx) {
        this.low = low;
        this.size = size;
        this.step = step;
        this.curr = startIdx;
    }

    public final Integer value(int idx) {
        return Integer.valueOf(intValue(idx));
    }

    public final int intValue(int idx) {
        return low + step * idx;
    }

    @Override
    public boolean hasNext() {
        return curr < size;
    }

    public int intNext() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return intValue(curr++);
    }

    @Override
    public Integer next() {
        return Integer.valueOf(intNext());
    }

    @Override
    public boolean hasPrevious() {
        return curr > 0;
    }

    @Override
    public Integer previous() {
        return Integer.valueOf(intPrevious());
    }

    public int intPrevious() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        return intValue(--curr);
    }

    @Override
    public int nextIndex() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return curr;
    }

    @Override
    public int previousIndex() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        return curr - 1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Integer e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(Integer e) {
        throw new UnsupportedOperationException();
    }

}
