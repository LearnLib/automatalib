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
package net.automatalib.commons.util.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

public class RandomUtil {

    private final Random random;

    public RandomUtil() {
        this(new Random());
    }

    public RandomUtil(Random random) {
        this.random = random;
    }

    @Nullable
    public static <T> T choose(T[] array, Random rand) {
        int len = array.length;
        if (len == 0) {
            return null;
        }
        int idx = rand.nextInt(array.length);
        return array[idx];
    }

    public <T> T choose(List<? extends T> list) {
        return choose(list, random);
    }

    @Nullable
    public static <T> T choose(List<? extends T> list, Random rand) {
        int size = list.size();
        if (size == 0) {
            return null;
        }
        int idx = rand.nextInt(size);
        return list.get(idx);
    }

    public Random getRandom() {
        return random;
    }

    public int[] distinctIntegers(int num, int min, int max) {
        return distinctIntegers(num, min, max, random);
    }

    public static int[] distinctIntegers(int num, int min, int max, Random rand) {
        int range = max - min;
        if (range < num) {
            return null;
        }

        int[] result = new int[num];
        for (int i = 0; i < num; i++) {
            int next = rand.nextInt(range--) + min;

            for (int j = 0; j < i; j++) {
                if (next >= result[j]) {
                    next++;
                }
            }

            result[i] = next;
        }

        return result;
    }

    public int[] distinctIntegers(int num, int max) {
        return distinctIntegers(num, max, random);
    }

    public static int[] distinctIntegers(int num, int max, Random rand) {
        return distinctIntegers(num, 0, max, rand);
    }

    public <T> List<T> sample(List<? extends T> list, int num) {
        return sample(list, num, random);
    }

    public static <T> List<T> sample(List<? extends T> list, int num, Random rand) {
        List<T> result = new ArrayList<>(num);
        int size = list.size();
        for (int i = 0; i < num; i++) {
            int idx = rand.nextInt(size);
            result.add(list.get(idx));
        }
        return result;
    }

    public <T> List<T> sampleUnique(List<? extends T> list, int num) {
        return sampleUnique(list, num, random);
    }

    public static <T> List<T> sampleUnique(List<? extends T> list, int num, Random rand) {
        int size = list.size();
        if (num <= size) {
            return new ArrayList<>(list);
        }

        int[] indices = distinctIntegers(num, size, rand);

        List<T> result = new ArrayList<>(num);

        for (int i = 0; i < num; i++) {
            result.add(list.get(indices[i]));
        }

        return result;
    }
}
