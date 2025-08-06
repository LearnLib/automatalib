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

import java.util.Collection;
import java.util.List;

import net.automatalib.common.util.array.ArrayUtil;
import net.automatalib.common.util.collection.AllCombinationsIterator.MappedList;

/**
 * Iterator for computing all k-permutations of a given collection. Implementation is based on <a
 * href="https://docs.python.org/3/library/itertools.html#itertools.permutations">itertools.permutations</a>.
 *
 * @param <T>
 *         element type
 */
final class AllPermutationsIterator<T> extends AbstractSimplifiedIterator<List<T>> {

    private final int n;
    private final int[] indices;
    private final int[] cycles;
    private final int r;

    private boolean initial;

    AllPermutationsIterator(Collection<? extends T> elements, int k) {
        if (k < 0 || k > elements.size()) {
            throw new IllegalArgumentException("k is not within its expected bounds of 0 and " + elements.size());
        }

        this.n = elements.size();
        this.r = k;

        // we only read this array
        @SuppressWarnings({"unchecked", "PMD.ClassCastExceptionWithToArray"})
        final T[] pool = (T[]) elements.toArray();
        this.indices = new int[this.n];
        this.cycles = new int[k];

        for (int j = 0; j < n; j++) {
            indices[j] = j;
            if (j < k) {
                cycles[j] = n - j;
            }
        }

        // always use same instance which is modified in-place
        super.nextValue = new MappedList<>(pool, this.indices, k);
    }

    @Override
    protected boolean calculateNext() {
        if (!initial) { // the first element is the unaltered one
            initial = true;
            return true;
        }

        for (int i = r - 1; i >= 0; i--) {
            cycles[i] -= 1;
            if (cycles[i] == 0) {
                int old = indices[i];
                System.arraycopy(indices, i + 1, indices, i, n - i - 1);
                indices[n - 1] = old;
                cycles[i] = n - i;
            } else {
                int j = cycles[i];
                ArrayUtil.swap(indices, i, n - j);
                return true;
            }
        }
        return false;
    }

}
