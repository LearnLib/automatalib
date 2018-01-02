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
package net.automatalib.commons.util.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class IterablesTest {

    @Test
    public void testConcat() {
        final List<String> l1 = Arrays.asList("foo", "bar");
        final List<String> l2 = Arrays.asList("baz", "qux");

        final List<String> joined = new ArrayList<>(4);
        joined.addAll(l1);
        joined.addAll(l2);

        final List<String> j1 = Lists.newArrayList(IterableUtil.concat(l1, l2));
        Assert.assertEquals(joined, j1);

        final List<String> j2 = Lists.newArrayList(IterableUtil.concat(Collections.emptyList(), l2));
        Assert.assertEquals(l2, j2);

        final List<String> j3 = Lists.newArrayList(IterableUtil.concat(l1, Collections.emptyList()));
        Assert.assertEquals(l1, j3);
    }

}
