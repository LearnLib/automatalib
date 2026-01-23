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
package net.automatalib.symbol.time;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SymbolsTest {

    @Test
    public void testNonNegativeDelays() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new TimedOutput<>("output", -1));
        Assert.assertThrows(IllegalArgumentException.class, () -> new TimeStepSequence<>(0));
    }

    @Test
    public void testToStrings() {
        List<?> symbols = Arrays.asList(TimedInput.input("test"),
                                        TimedInput.step(42),
                                        TimedInput.timeout(),
                                        new TimerTimeoutSymbol<>("t"),
                                        new TimedOutput<>("out", 21),
                                        new TimedOutput<>("put"));

        final List<String> outs = symbols.stream().map(Object::toString).toList();

        Assert.assertEquals(outs, Arrays.asList("test", "wait[42]", "timeout", "to[t]", "[21]out", "put"));
    }
}
