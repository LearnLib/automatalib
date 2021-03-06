/* Copyright (C) 2013-2021 TU Dortmund
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

import java.util.AbstractSet;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link Set} of {@link Integer int}s that internally stores its elements in a {@link BitSet}. This class may also be
 * used as a(n) (im)mutable {@link Set}-view of an existing {@link BitSet}.
 *
 * @author frohme
 */
public class IntSet extends AbstractSet<Integer> {

    private final BitSet delegate;
    private final boolean immutable;

    /**
     * Default constructor for a mutable {@link IntSet}.
     */
    public IntSet() {
        this(new BitSet(), false);
    }

    /**
     * Default constructor for an immutable view of a given {@link BitSet}.
     *
     * @param delegate
     *         the backing {@link BitSet}
     */
    public IntSet(BitSet delegate) {
        this(delegate, true);
    }

    /**
     * Constructor for a(n) (immutable) view of a given {@link IntSet}.
     *
     * @param delegate
     *         the backing {@link BitSet}
     * @param immutable
     *         a flag indicating whether mutating operations should write through to the backing {@link BitSet}
     */
    public IntSet(BitSet delegate, boolean immutable) {
        this.delegate = delegate;
        this.immutable = immutable;
    }

    @Override
    public int size() {
        return delegate.cardinality();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(@Nullable Object o) {
        if (!(o instanceof Integer)) {
            return false;
        }

        return delegate.get((Integer) o);
    }

    @Override
    public Iterator<Integer> iterator() {
        return new BitSetIterator(delegate, immutable);
    }

    @Override
    public boolean add(Integer integer) {
        if (immutable) {
            throw new UnsupportedOperationException("This is a read-only set-view");
        }

        boolean containedBefore = delegate.get(integer);
        delegate.set(integer);

        return !containedBefore;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        if (immutable) {
            throw new UnsupportedOperationException("This is a read-only set-view");
        }

        if (!(o instanceof Integer)) {
            return false;
        }

        int asInt = (Integer) o;
        boolean containedBefore = delegate.get(asInt);
        delegate.clear(asInt);
        return containedBefore;
    }

    @Override
    public void clear() {
        if (immutable) {
            throw new UnsupportedOperationException("This is a read-only set-view");
        }

        delegate.clear();
    }
}
