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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CollectionUtilTest {

    private static final List<Character> COLLECTION = Arrays.asList('A', 'B', 'C', 'D');

    @Test
    public void testAdd() {
        final Set<Integer> set = new HashSet<>();
        final Set<Integer> elements = Set.of(2, 3, 4);

        Assert.assertTrue(CollectionUtil.add(set, elements.iterator()));
        Assert.assertEquals(set, elements);
        Assert.assertFalse(CollectionUtil.add(set, elements.iterator()));
        Assert.assertEquals(set, elements);
    }

    @Test
    public void testIllegalPermutations() {
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> CollectionUtil.allPermutationsIterator(COLLECTION, -1));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> CollectionUtil.allPermutationsIterator(COLLECTION, 5));
    }

    @Test
    public void testAllTwoPermutations() {
        final int k = 2;
        final Iterator<List<Character>> lists = CollectionUtil.allPermutationsIterator(COLLECTION, k);
        final List<String> tuples = new ArrayList<>();

        while (lists.hasNext()) {
            StringBuilder sb = new StringBuilder(k);
            lists.next().forEach(sb::append);
            Assert.assertTrue(tuples.add(sb.toString()));
        }

        Assert.assertEquals(tuples,
                            Arrays.asList("AB", "AC", "AD", "BA", "BC", "BD", "CA", "CB", "CD", "DA", "DB", "DC"));
    }

    @Test
    public void testAllThreePermutations() {
        final int k = 3;
        final Iterator<List<Character>> lists = CollectionUtil.allPermutationsIterator(COLLECTION, k);
        final List<String> tuples = new ArrayList<>();

        while (lists.hasNext()) {
            StringBuilder sb = new StringBuilder(k);
            lists.next().forEach(sb::append);
            Assert.assertTrue(tuples.add(sb.toString()));
        }

        Assert.assertEquals(tuples,
                            Arrays.asList("ABC",
                                          "ABD",
                                          "ACB",
                                          "ACD",
                                          "ADB",
                                          "ADC",
                                          "BAC",
                                          "BAD",
                                          "BCA",
                                          "BCD",
                                          "BDA",
                                          "BDC",
                                          "CAB",
                                          "CAD",
                                          "CBA",
                                          "CBD",
                                          "CDA",
                                          "CDB",
                                          "DAB",
                                          "DAC",
                                          "DBA",
                                          "DBC",
                                          "DCA",
                                          "DCB"));
    }

    @Test
    public void testAllFourPermutations() {
        final int n = COLLECTION.size();
        final Iterator<List<Character>> lists = CollectionUtil.allPermutationsIterator(COLLECTION);
        final List<String> tuples = new ArrayList<>();

        while (lists.hasNext()) {
            StringBuilder sb = new StringBuilder(n);
            lists.next().forEach(sb::append);
            Assert.assertTrue(tuples.add(sb.toString()));
        }

        final List<String> expected = new ArrayList<>();
        // brute-force 4 dimensional tuples
        for (int i1 = 0; i1 < n; i1++) {
            for (int i2 = 0; i2 < n; i2++) {
                if (i1 != i2) {
                    for (int i3 = 0; i3 < n; i3++) {
                        if (i3 != i1 && i3 != i2) {
                            for (int i4 = 0; i4 < n; i4++) {
                                if (i4 != i1 && i4 != i2 && i4 != i3) {
                                    final StringBuilder sb = new StringBuilder();
                                    sb.append(COLLECTION.get(i1))
                                      .append(COLLECTION.get(i2))
                                      .append(COLLECTION.get(i3))
                                      .append(COLLECTION.get(i4));
                                    expected.add(sb.toString());
                                }
                            }
                        }
                    }
                }
            }
        }

        Assert.assertEquals(tuples, expected);
    }

    @Test
    public void testIllegalCombinations() {
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> CollectionUtil.allCombintationsIterator(COLLECTION, -1));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> CollectionUtil.allCombintationsIterator(COLLECTION, 5));
    }

    @Test
    public void testAllTwoCombinations() {
        final int k = 2;
        final Iterator<List<Character>> lists = CollectionUtil.allCombintationsIterator(COLLECTION, k);
        final List<String> tuples = new ArrayList<>();

        while (lists.hasNext()) {
            StringBuilder sb = new StringBuilder(k);
            lists.next().forEach(sb::append);
            Assert.assertTrue(tuples.add(sb.toString()));
        }

        Assert.assertEquals(tuples, Arrays.asList("AB", "AC", "AD", "BC", "BD", "CD"));
    }

    @Test
    public void testAllThreeCombinations() {
        final int k = 3;
        final Iterator<List<Character>> lists = CollectionUtil.allCombintationsIterator(COLLECTION, k);
        final List<String> tuples = new ArrayList<>();

        while (lists.hasNext()) {
            StringBuilder sb = new StringBuilder(k);
            lists.next().forEach(sb::append);
            Assert.assertTrue(tuples.add(sb.toString()));
        }

        Assert.assertEquals(tuples, Arrays.asList("ABC", "ABD", "ACD", "BCD"));
    }

    @Test
    public void testAllFourCombinations() {
        final int k = 4;
        final Iterator<List<Character>> lists = CollectionUtil.allCombintationsIterator(COLLECTION, k);
        final List<String> tuples = new ArrayList<>();

        while (lists.hasNext()) {
            StringBuilder sb = new StringBuilder(k);
            lists.next().forEach(sb::append);
            Assert.assertTrue(tuples.add(sb.toString()));
        }

        Assert.assertEquals(tuples, Collections.singleton("ABCD"));
    }
}
