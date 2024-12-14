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
package net.automatalib.common.smartcollection;

import java.util.NoSuchElementException;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class BinaryHeapTest {

    @Test
    public void testHeapOps() {
        final BinaryHeap<Integer> heap = BinaryHeap.create();

        final ElementReference ref = heap.referencedAdd(42);
        Assert.assertNotNull(ref);

        Assert.assertTrue(heap.offer(23));
        Assert.assertTrue(heap.offer(37));

        Assert.assertEquals(heap.size(), 3);
        Assert.assertFalse(heap.isEmpty());
        Assert.assertFalse(heap.contains(13));

        Assert.assertEquals(heap.peek(), 23);

        Assert.assertTrue(heap.add(13));
        Assert.assertEquals(heap.size(), 4);

        Assert.assertEquals(heap.peek(), 13);
        Assert.assertEquals(heap.poll(), 13);
        Assert.assertEquals(heap.size(), 3);
        Assert.assertEquals(heap.peek(), 23);

        Assert.assertEquals(heap.get(ref), 42);
        heap.replace(ref, 5);

        Assert.assertEquals(heap.size(), 3);
        Assert.assertEquals(heap.peek(), 5);
        heap.remove(ref);
        Assert.assertEquals(heap.size(), 2);
        Assert.assertEquals(heap.peek(), 23);

        Assert.assertTrue(heap.offer(40));
        Assert.assertEquals(heap.size(), 3);
        Assert.assertEquals(heap.peek(), 23);
        Assert.assertEquals(heap.poll(), 23);
        Assert.assertEquals(heap.size(), 2);

        Assert.assertEquals(heap.peek(), 37);
        Assert.assertEquals(heap.poll(), 37);
        Assert.assertEquals(heap.size(), 1);

        heap.deepClear();

        Assert.assertEquals(heap.size(), 0);
        Assert.assertNull(heap.peek());
        Assert.assertThrows(NoSuchElementException.class, heap::element);
        Assert.assertTrue(heap.isEmpty());
    }

}
