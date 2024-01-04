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
package net.automatalib.util.automaton.conformance;

import java.util.Comparator;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StrictPriorityQueueTest {

    @Test
    public void testQueue() {
        final StrictPriorityQueue<Integer> queue =
                new StrictPriorityQueue<>(Comparator.comparingInt(o -> o), Integer::sum);

        // ascending order
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(queue.offer(i));
        }

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(queue.poll(), i);
        }

        // descending order
        for (int i = 9; i >= 0; i--) {
            Assert.assertTrue(queue.offer(i));
        }

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(queue.poll(), i);
        }

    }

    @Test
    public void testStrictness() {
        final StrictPriorityQueue<Integer> queue =
                new StrictPriorityQueue<>(Comparator.comparingInt(o -> o), Integer::sum);

        Assert.assertTrue(queue.offer(1));
        Assert.assertFalse(queue.offer(1));

        Assert.assertEquals(queue.size(), 1);
        Assert.assertEquals(queue.peek(), 2);

        Assert.assertEquals(queue.poll(), 2);
        Assert.assertTrue(queue.isEmpty());
        Assert.assertNull(queue.poll());
        Assert.assertNull(queue.peek());
    }
}
