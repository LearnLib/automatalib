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

import org.testng.Assert;
import org.testng.annotations.Test;

public class ArrayStorageTest {

    @Test
    public void testThrowsOutOfBounds() {

        final ArrayStorage<Integer> a = new ArrayStorage<>(0);

        // make sure actual array is larger than the size of the storage
        a.ensureCapacity(1);
        a.ensureCapacity(2);
        a.ensureCapacity(4);
        a.ensureCapacity(5);

        Assert.assertThrows(IndexOutOfBoundsException.class, () -> a.get(6));
        Assert.assertThrows(IndexOutOfBoundsException.class, () -> a.set(6, 6));
    }

    @Test
    public void testCopyConstructor() {

        final ArrayStorage<Integer> a = new ArrayStorage<>(0);

        // make sure actual array is larger than the size of the storage
        a.ensureCapacity(1);
        a.ensureCapacity(2);
        a.ensureCapacity(4);
        a.ensureCapacity(5);

        final ArrayStorage<Integer> b = new ArrayStorage<>(a);

        testDistinctEquality(a, b);
    }

    @Test
    public void testEqualsWithDifferentInternalSizes() {

        final ArrayStorage<Integer> a = new ArrayStorage<>(0);
        final ArrayStorage<Integer> b = new ArrayStorage<>(5);

        // make sure actual array is larger than the size of the storage
        a.ensureCapacity(1);
        a.ensureCapacity(2);
        a.ensureCapacity(4);
        a.ensureCapacity(5);

        testDistinctEquality(a, b);
    }

    private static void testDistinctEquality(ArrayStorage<Integer> a, ArrayStorage<Integer> b) {

        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());

        a.set(2, 2);

        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(a.hashCode(), b.hashCode());

        b.set(2, 2);

        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());

    }
}
