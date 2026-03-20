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
package net.automatalib.mmlt;

import net.automatalib.automaton.mmlt.TimerInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimerInfoTest {

    @Test
    public void testNonNegativeInitial() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new TimerInfo<>("t", 0, "out", null));
    }

    @Test
    public void testOneShotTransformation() {
        TimerInfo<?, String> timer = new TimerInfo<>("t", 12, "out", null);
        TimerInfo<?, String> oneShot = timer.asOneShot();

        Assert.assertEquals(oneShot.name(), timer.name());
        Assert.assertEquals(oneShot.initial(), timer.initial());
        Assert.assertEquals(oneShot.outputs(), timer.outputs());
        Assert.assertEquals(oneShot.target(), timer.target());
        Assert.assertFalse(oneShot.periodic());
    }
}
