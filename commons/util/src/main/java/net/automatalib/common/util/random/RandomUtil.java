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
package net.automatalib.common.util.random;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class RandomUtil {

    private RandomUtil() {
        // prevent instantiation
    }

    public static <T> @Nullable T choose(Random rand, T[] array) {
        int len = array.length;
        if (len == 0) {
            return null;
        }
        int idx = rand.nextInt(array.length);
        return array[idx];
    }

    public static <T> @Nullable T choose(Random rand, List<? extends T> list) {
        int size = list.size();
        if (size == 0) {
            return null;
        }
        int idx = rand.nextInt(size);
        return list.get(idx);
    }

    /**
     * Sample a specified number of distinct integers from a specified range.
     * <p>
     * The implementation is based on Floyd's <a href="https://doi.org/10.1145/30401.315746">Algorithm F2</a>. Note that
     * this algorithm ensures equal probability of each integer within in the specified range to appear in the returned
     * array but no equal probability of their order.
     *
     * @param rand
     *         the random instance for generating numbers
     * @param num
     *         number of integers to sample
     * @param min
     *         lower bound (inclusive) of sampled values
     * @param max
     *         upper bound (exclusive) of samples values
     *
     * @return an array of distinct integers sampled from the specified range
     */
    public static int[] distinctIntegers(Random rand, int num, int min, int max) {
        int range = max - min;
        int size = Math.min(num, range);

        int[] result = new int[size];
        BitSet cache = new BitSet(range);
        int idx = 0;

        for (int j = range - size; j < range; j++) {
            int t = rand.nextInt(j + 1);
            int elem = cache.get(t) ? j : t;

            cache.set(elem);
            result[idx++] = elem + min;
        }

        return result;
    }

    public static int[] distinctIntegers(Random rand, int num, int max) {
        return distinctIntegers(rand, num, 0, max);
    }

    public static <T> List<T> sample(Random rand, List<? extends T> list, int num) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(num);
        int size = list.size();
        for (int i = 0; i < num; i++) {
            int idx = rand.nextInt(size);
            result.add(list.get(idx));
        }
        return result;
    }

    /**
     * Sample a specified number of elements from specified list.
     * <p>
     * The implementation is based on Floyd's <a href="https://doi.org/10.1145/30401.315746">Algorithm F2</a>. Note that
     * this algorithm ensures equal probability of each element within in the specified list to appear in the returned
     * list but no equal probability of their order.
     *
     * @param rand
     *         the random instance for generating numbers
     * @param list
     *         the list to sample elements from
     * @param num
     *         number of integers to sample
     * @param <T>
     *         element type
     *
     * @return a list of distinct elements sampled from the specified list
     */
    public static <T> List<T> sampleUnique(Random rand, List<? extends T> list, int num) {
        int elems = list.size();

        if (elems == 0) {
            return Collections.emptyList();
        }

        int[] indices = distinctIntegers(rand, num, elems);
        List<T> result = new ArrayList<>(indices.length);

        for (int index : indices) {
            result.add(list.get(index));
        }

        return result;
    }
}
