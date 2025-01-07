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
package net.automatalib.common.smartcollection;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IntSeqTest {

    private final int[] arr1 = {1, 2, 3, 4, 5, 6};
    private final int[] arr2 = {31, 43, 45, 1, 3445, 56};

    private final List<Integer> asList1 = IntStream.of(arr1).boxed().collect(Collectors.toList());
    private final List<Integer> asList2 = IntStream.of(arr2).boxed().collect(Collectors.toList());

    @Test
    public void testArrays() {
        Assert.assertEquals(IntSeq.of(arr1), asList1);
        Assert.assertEquals(IntSeq.of(arr2), asList2);
    }

    @Test
    public void testLists() {
        Assert.assertEquals(IntSeq.of(asList1), asList1);
        Assert.assertEquals(IntSeq.of(asList2), asList2);
    }
}
