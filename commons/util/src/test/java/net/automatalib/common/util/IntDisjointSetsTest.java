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
package net.automatalib.common.util;

import java.util.function.IntFunction;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IntDisjointSetsTest {

    @DataProvider(name = "impl")
    private static Object[][] dfaConfigs() {
        final IntFunction<? extends IntDisjointSets> uf = UnionFind::new;
        final IntFunction<? extends IntDisjointSets> rem = UnionFindRemSP::new;

        return new Object[][] {{uf}, {rem}};
    }

    @Test(dataProvider = "impl")
    public void testIntDisjointSets(IntFunction<? extends IntDisjointSets> constructor) {

        final int size = 10;
        final IntDisjointSets uf = constructor.apply(size);

        Assert.assertEquals(uf.size(), size);

        for (int i = 0; i < size; i++) {
            Assert.assertEquals(uf.find(i), i);
        }

        final int s1 = 3;
        final int s2 = 7;
        final int s3 = 1;
        final int s4 = 9;
        final int s5 = 5;

        Assert.assertTrue(uf.union(s1, s2));
        Assert.assertFalse(uf.union(s1, s2));
        Assert.assertFalse(uf.union(s2, s1));

        final int id1 = uf.find(s1);

        Assert.assertEquals(uf.find(s1), id1);
        Assert.assertEquals(uf.find(s2), id1);
        Assert.assertTrue(uf.equivalent(s1, s2));
        Assert.assertTrue(uf.equivalent(s2, s1));
        Assert.assertFalse(uf.equivalent(s1, s3));
        Assert.assertFalse(uf.equivalent(s3, s1));
        Assert.assertFalse(uf.equivalent(s2, s3));
        Assert.assertFalse(uf.equivalent(s3, s2));

        Assert.assertTrue(uf.union(s2, s3));
        Assert.assertFalse(uf.union(s2, s3));
        Assert.assertFalse(uf.union(s3, s2));

        final int id2 = uf.find(s2);

        Assert.assertEquals(uf.find(s1), id2);
        Assert.assertEquals(uf.find(s2), id2);
        Assert.assertEquals(uf.find(s3), id2);
        Assert.assertTrue(uf.equivalent(s1, s2));
        Assert.assertTrue(uf.equivalent(s2, s3));
        Assert.assertTrue(uf.equivalent(s3, s1));

        final int id3 = uf.link(id2, s4);

        Assert.assertEquals(uf.link(s4, id2), id3);

        Assert.assertEquals(uf.find(s1), id3);
        Assert.assertEquals(uf.find(s2), id3);
        Assert.assertEquals(uf.find(s3), id3);
        Assert.assertEquals(uf.find(s4), id3);
        Assert.assertTrue(uf.equivalent(s1, s2));
        Assert.assertTrue(uf.equivalent(s2, s3));
        Assert.assertTrue(uf.equivalent(s3, s4));
        Assert.assertTrue(uf.equivalent(s4, s1));

        Assert.assertNotEquals(uf.find(s1), uf.find(s5));
        Assert.assertFalse(uf.equivalent(s1, s5));

        for (int i = 0; i < size; i++) {
            uf.union(i, s1);
        }

        final int globalId = uf.find(s1);
        for (int i = 0; i < size; i++) {
            Assert.assertEquals(uf.find(i), globalId);
        }
    }
}
