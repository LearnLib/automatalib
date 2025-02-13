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
package net.automatalib.common.util.collection;

import java.util.AbstractList;
import java.util.RandomAccess;

import org.checkerframework.checker.nullness.qual.Nullable;

public class IntRange extends AbstractList<Integer> implements RandomAccess {

    private final int start;
    private final int step;
    private final int size;

    public IntRange(int start, int end) {
        this(start, end, 1);
    }

    public IntRange(int start, int end, int step) {
        this.start = start;
        this.step = step;
        this.size = (end - start - 1) / step + 1;
    }

    @Override
    public Integer get(int index) {
        return intGet(index);
    }

    public int intGet(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return start + step * index;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public int indexOf(@Nullable Object o) {
        return o instanceof Integer ? indexOf((int) o) : -1;
    }

    public int indexOf(int i) {
        if (i < start) {
            return -1;
        }
        final int aligned = i - start;
        if (aligned % step != 0) {
            return -1;
        }
        final int normalized = aligned / step;
        if (normalized >= size) {
            return -1;
        }
        return normalized;
    }


    @Override
    public int lastIndexOf(@Nullable Object o) {
        return indexOf(o);
    }

    @Override
    public IntRangeIterator iterator() {
        return new IntRangeIterator(start, step, size);
    }

    @Override
    public IntRangeIterator listIterator() {
        return new IntRangeIterator(start, step, size);
    }

    @Override
    public IntRangeIterator listIterator(int index) {
        return new IntRangeIterator(start, step, size, index);
    }

    @Override
    public int size() {
        return size;
    }
}
