/* Copyright (C) 2013-2026 TU Dortmund University
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
import java.util.Collection;
import java.util.List;

/**
 * Iterator for computing all k-combinations of a given collection. Implementation is based on <a
 * href="https://hmkcode.com/calculate-find-all-possible-combinations-of-an-array-using-java/">https://hmkcode.com/calculate-find-all-possible-combinations-of-an-array-using-java/</a>.
 *
 * @param <T>
 *         element type
 */
final class AllCombinationsIterator<T> extends AbstractSimplifiedIterator<List<T>> {

    private final int[] pointers;
    private final int n;
    private final int k;

    private int r; // index for combination array
    private int i; // index for elements array

    AllCombinationsIterator(Collection<? extends T> elements, int k) {
        if (k < 0 || k > elements.size()) {
            throw new IllegalArgumentException("k is not within its expected bounds of 0 and " + elements.size());
        }

        this.n = elements.size();
        this.k = k;

        // we only read this array
        @SuppressWarnings({"unchecked", "PMD.ClassCastExceptionWithToArray"})
        final T[] pool = (T[]) elements.toArray();
        this.pointers = new int[k];

        // always use same instance which is modified in-place
        super.nextValue = new MappedList<>(pool, this.pointers, this.k);
    }

    @Override
    protected boolean calculateNext() {
        while (r >= 0) {
            if (i <= n + r - k) { // forward step if i < (n + (r-K))
                pointers[r] = i;
                if (r == k - 1) { // if combination array is full print and increment i;
                    i++;
                    return true;
                } else { // if combination is not full yet, select next element
                    i = pointers[r] + 1;
                    r++;
                }
            } else { // backward step
                r--;
                if (r >= 0) {
                    i = pointers[r] + 1;
                }
            }
        }
        return false;
    }

    static class MappedList<T> extends AbstractList<T> {

        private final T[] pool;
        private final int[] pointers;
        private final int k;

        MappedList(T[] pool, int[] pointers, int k) {
            this.pool = pool;
            this.pointers = pointers;
            this.k = k;
        }

        @Override
        public T get(int index) {
            return pool[pointers[index]];
        }

        @Override
        public int size() {
            return k;
        }
    }
}
