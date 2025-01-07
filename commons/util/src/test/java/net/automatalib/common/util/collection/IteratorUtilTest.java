/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.common.util.collection;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IteratorUtilTest {

    @Test
    public void testBatch() {
        final Iterator<Integer> iterator = Stream.iterate(0, i -> i+1).limit(50).iterator();
        final Iterator<List<Integer>> batchIterator = IteratorUtil.batch(iterator, 20);

        Assert.assertTrue(batchIterator.hasNext());
        Assert.assertEquals(batchIterator.next(), CollectionUtil.intRange(0, 20));
        Assert.assertTrue(batchIterator.hasNext());
        Assert.assertEquals(batchIterator.next(), CollectionUtil.intRange(20, 40));
        Assert.assertTrue(batchIterator.hasNext());
        Assert.assertEquals(batchIterator.next(), CollectionUtil.intRange(40, 50));
        Assert.assertFalse(batchIterator.hasNext());
        Assert.assertThrows(NoSuchElementException.class, batchIterator::next);

        final Iterator<Integer> iterator2 = Stream.iterate(0, i -> i+1).limit(60).iterator();
        final Iterator<List<Integer>> batchIterator2 = IteratorUtil.batch(iterator2, 20);

        Assert.assertTrue(batchIterator2.hasNext());
        Assert.assertEquals(batchIterator2.next(), CollectionUtil.intRange(0, 20));
        Assert.assertTrue(batchIterator2.hasNext());
        Assert.assertEquals(batchIterator2.next(), CollectionUtil.intRange(20, 40));
        Assert.assertTrue(batchIterator2.hasNext());
        Assert.assertEquals(batchIterator2.next(), CollectionUtil.intRange(40, 60));
        Assert.assertFalse(batchIterator2.hasNext());
        Assert.assertThrows(NoSuchElementException.class, batchIterator2::next);
    }

    @Test
    public void testSingleton() {
        final Iterator<Integer> iterator = IteratorUtil.singleton(42);

        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), 42);
        Assert.assertThrows(UnsupportedOperationException.class, iterator::remove);
        Assert.assertFalse(iterator.hasNext());
        Assert.assertThrows(NoSuchElementException.class, iterator::next);
    }
}
