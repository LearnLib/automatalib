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
package net.automatalib.common.util.math;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MathUtilTest {

    @Test
    public void testBinom() {
        Assert.assertThrows(IllegalArgumentException.class, () -> MathUtil.binomial(5, -1));
        Assert.assertEquals(MathUtil.binomial(5, 0), 1);
        Assert.assertEquals(MathUtil.binomial(5, 1), 5);
        Assert.assertEquals(MathUtil.binomial(5, 2), 10);
        Assert.assertEquals(MathUtil.binomial(5, 3), 10);
        Assert.assertEquals(MathUtil.binomial(5, 4), 5);
        Assert.assertEquals(MathUtil.binomial(5, 5), 1);
        Assert.assertThrows(IllegalArgumentException.class, () -> MathUtil.binomial(5, 6));

        Assert.assertEquals(MathUtil.binomial(70, 35), Long.MAX_VALUE);
    }
}
