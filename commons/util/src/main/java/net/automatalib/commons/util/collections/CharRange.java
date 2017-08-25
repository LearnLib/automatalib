/* Copyright (C) 2013-2017 TU Dortmund
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

import java.util.AbstractList;
import java.util.RandomAccess;

import net.automatalib.commons.util.array.ArrayWritable;

public class CharRange extends AbstractList<Character> implements ArrayWritable<Character>, RandomAccess {

    private final IntRange delegate;

    public CharRange(char low, char high) {
        this(low, high, 1);
    }

    public CharRange(char low, char high, int step) {
        this(new IntRange(low, high, step));
    }

    public CharRange(IntRange delegate) {
        this.delegate = delegate;
    }

    /*
     * (non-Javadoc)
     * @see java.util.AbstractList#get(int)
     */
    @Override
    public Character get(int index) {
        return Character.valueOf(charGet(index));
    }

    public char charGet(int index) {
        int i = delegate.intGet(index);
        return (char) i;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#indexOf(java.lang.Object)
     */
    @Override
    public int indexOf(Object o) {
        if (o == null || o.getClass() != Character.class) {
            return -1;
        }
        return indexOf(((Character) o).charValue());
    }

    public int indexOf(char c) {
        return delegate.indexOf(c);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#lastIndexOf(java.lang.Object)
     */
    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#iterator()
     */
    @Override
    public CharRangeIterator iterator() {
        return new CharRangeIterator(delegate.iterator());
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#listIterator()
     */
    @Override
    public CharRangeIterator listIterator() {
        return new CharRangeIterator(delegate.listIterator());
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#listIterator(int)
     */
    @Override
    public CharRangeIterator listIterator(int index) {
        return new CharRangeIterator(delegate.listIterator(index));
    }

    /*
     * (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return delegate.size();
    }

    /* (non-Javadoc)
     * @see net.automatalib.commons.util.array.ArrayWritable#writeToArray(int, java.lang.Object[], int, int)
     */
    @Override
    public void writeToArray(int offset, Object[] array, int tgtOfs, int num) {
        int si = offset;
        int ti = tgtOfs;
        for (int i = 0; i < num; i++) {
            array[ti++] = charGet(si++);
        }
    }

    public char charValue(int i) {
        return (char) delegate.intValue(i);
    }

}
