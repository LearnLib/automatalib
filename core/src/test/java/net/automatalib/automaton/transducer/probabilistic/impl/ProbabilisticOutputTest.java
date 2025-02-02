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
package net.automatalib.automaton.transducer.probabilistic.impl;

import java.util.Arrays;
import java.util.List;

import net.automatalib.automaton.transducer.probabilistic.ProbabilisticOutput;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProbabilisticOutputTest {

    @Test
    public void testFastProbMealy() {

        List<ProbabilisticOutput<Character>> outputs = Arrays.asList(new ProbabilisticOutput<>(0.5f, 'a'),
                                                                     new ProbabilisticOutput<>(0.5f, 'b'),
                                                                     new ProbabilisticOutput<>(0.25f, 'a'),
                                                                     new ProbabilisticOutput<>(0.25f, 'b'));

        for (int i = 0; i < outputs.size(); i++) {
            final ProbabilisticOutput<Character> iOut = outputs.get(i);
            for (int j = 0; j < outputs.size(); j++) {
                final ProbabilisticOutput<Character> jOut = outputs.get(j);
                Assert.assertEquals(iOut.equals(jOut), i == j);
                Assert.assertEquals(iOut.hashCode() == jOut.hashCode(), i == j);
            }
        }

        final ProbabilisticOutput<Character> po = outputs.get(0);
        Assert.assertEquals(po, new ProbabilisticOutput<>(0.5f, 'a'));
        Assert.assertFalse(po.equals(new Object()));
    }
}
