/* Copyright (C) 2013-2023 TU Dortmund
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

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class IntRangeTest extends AbstractRangeTest<Integer, IntRange> {

    public IntRangeTest() {
        super(new IntRange(20, 30, 3));
    }

    @Test
    public void testSteps() {
        Assert.assertEquals(super.range.get(2).intValue(), 26);
        Assert.assertEquals(super.range.get(3).intValue(), 29);
        Assert.assertEquals(super.range.indexOf(25), -1);
    }

}
