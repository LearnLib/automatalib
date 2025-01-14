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

import java.util.Iterator;
import java.util.NoSuchElementException;

class ArrayIterator<T> implements Iterator<T> {

    private final T[] delegate;
    private final int end;
    private int idx;

    ArrayIterator(T[] delegate) {
        this(delegate, 0, delegate.length);
    }

    ArrayIterator(T[] delegate, int start, int end) {
        this.delegate = delegate;
        this.idx = start;
        this.end = Math.min(delegate.length, end);
    }

    @Override
    public boolean hasNext() {
        return idx < end;
    }

    @Override
    public T next() {
        if (hasNext()) {
            return delegate[idx++];
        }

        throw new NoSuchElementException();
    }
}
