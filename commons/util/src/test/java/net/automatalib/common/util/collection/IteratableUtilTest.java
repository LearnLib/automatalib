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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IteratableUtilTest {

    @Test
    public void testConcat() {
        final List<Integer> iter1 = Arrays.asList(1, 2);
        final List<Integer> iter2 = Arrays.asList(4, 3);

        Iterable<Integer> concat1 = IterableUtil.concat(iter1, iter2);
        Iterable<Integer> concat2 = IterableUtil.concat(iter2, iter1);

        Assert.assertEquals(concat1, Arrays.asList(1, 2, 4, 3));
        Assert.assertEquals(concat2, Arrays.asList(4, 3, 1, 2));
    }

    @Test
    public void testExhaustiveness() {

        Set<Integer> iterable = Collections.singleton(1);
        Iterator<List<Integer>> iter1 = IterableUtil.cartesianProduct(iterable).iterator();
        Iterator<List<Integer>> iter2 = IterableUtil.allTuples(iterable, 1, 1).iterator();

        // consume iterators
        IteratorUtil.size(iter1);
        IteratorUtil.size(iter2);

        Assert.assertFalse(iter1.hasNext());
        Assert.assertThrows(NoSuchElementException.class, iter1::next);

        Assert.assertFalse(iter2.hasNext());
        Assert.assertThrows(NoSuchElementException.class, iter2::next);
    }
}
