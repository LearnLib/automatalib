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

import java.util.Arrays;
import java.util.List;

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
}
