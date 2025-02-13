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

public class CharStringRange extends AbstractList<String> implements RandomAccess {

    private final IntRange delegate;

    public CharStringRange(char low, char high) {
        this(low, high, 1);
    }

    public CharStringRange(char low, char high, int step) {
        this(new IntRange(low, high, step));
    }

    public CharStringRange(IntRange delegate) {
        this.delegate = delegate;
    }

    @Override
    public String get(int index) {
        return String.valueOf(charGet(index));
    }

    public char charGet(int index) {
        return (char) delegate.intGet(index);
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public int indexOf(@Nullable Object o) {
        if (o == null || o.getClass() != String.class) {
            return -1;
        }
        return indexOf((String) o);
    }

    public int indexOf(String s) {
        if (s.length() != 1) {
            return -1;
        }
        return delegate.indexOf(s.charAt(0));
    }

    @Override
    public int lastIndexOf(@Nullable Object o) {
        return indexOf(o);
    }

    @Override
    public CharStringRangeIterator iterator() {
        return new CharStringRangeIterator(delegate.iterator());
    }

    @Override
    public CharStringRangeIterator listIterator() {
        return new CharStringRangeIterator(delegate.listIterator());
    }

    @Override
    public CharStringRangeIterator listIterator(int index) {
        return new CharStringRangeIterator(delegate.listIterator(index));
    }

    @Override
    public int size() {
        return delegate.size();
    }
}
