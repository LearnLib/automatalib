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

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CollectionUtilTest {

    @Test
    public void testAdd() {
        final Set<Integer> set = new HashSet<>();
        final Set<Integer> elements = Set.of(2, 3, 4);

        Assert.assertTrue(CollectionUtil.add(set, elements.iterator()));
        Assert.assertEquals(set, elements);
        Assert.assertFalse(CollectionUtil.add(set, elements.iterator()));
        Assert.assertEquals(set, elements);
    }
}
