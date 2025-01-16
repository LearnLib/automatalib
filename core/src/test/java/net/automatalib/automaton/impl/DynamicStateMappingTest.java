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
package net.automatalib.automaton.impl;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.ShrinkableAutomaton;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.util.TestUtil;
import net.automatalib.common.util.mapping.MutableMapping;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DynamicStateMappingTest {

    @Test
    public void testShrinkableNonDeterministic() {
        testShrinkableAutomaton(TestUtil.constructNFA());
    }

    @Test
    public void testShrinkableDeterministic() {
        testShrinkableAutomaton(TestUtil.constructMealy());
    }

    @Test
    public void testDeterministic() {
        testAutomaton(TestUtil.constructMealy(CompactMealy::new));
    }

    private static <S> void testShrinkableAutomaton(ShrinkableAutomaton<S, ?, ?, ?, ?> automaton) {

        final List<S> states = new ArrayList<>(automaton.getStates());

        final S s0 = states.get(0);
        final S s1 = states.get(1);
        final S s2 = states.get(2);

        final MutableMapping<S, Integer> mapping = automaton.createDynamicStateMapping();

        mapping.put(s0, 0);
        mapping.put(s1, 1);
        mapping.put(s2, 2);

        automaton.removeState(s0);

        Assert.assertThrows(RuntimeException.class, () -> mapping.get(s0));
        Assert.assertEquals(mapping.get(s1).intValue(), 1);
        Assert.assertEquals(mapping.get(s2).intValue(), 2);

        automaton.removeState(s1, s2);

        Assert.assertThrows(RuntimeException.class, () -> mapping.get(s0));
        Assert.assertThrows(RuntimeException.class, () -> mapping.get(s1));
        Assert.assertEquals(mapping.get(s2).intValue(), 2);

        automaton.removeState(s2);

        Assert.assertThrows(RuntimeException.class, () -> mapping.get(s0));
        Assert.assertThrows(RuntimeException.class, () -> mapping.get(s1));
        Assert.assertThrows(RuntimeException.class, () -> mapping.get(s2));

        final S s3 = automaton.addState();
        mapping.put(s3, 3);
        Assert.assertEquals(mapping.get(s3).intValue(), 3);
    }

    private static <S> void testAutomaton(MutableDeterministic<S, ?, ?, ?, ?> automaton) {

        final List<S> states = new ArrayList<>(automaton.getStates());

        final S s0 = states.get(0);
        final S s1 = states.get(1);
        final S s2 = states.get(2);

        final MutableMapping<S, Integer> mapping = automaton.createDynamicStateMapping();

        mapping.put(s0, 0);
        mapping.put(s1, 1);
        mapping.put(s2, 2);

        Assert.assertEquals(mapping.get(s0).intValue(), 0);
        Assert.assertEquals(mapping.get(s1).intValue(), 1);
        Assert.assertEquals(mapping.get(s2).intValue(), 2);

        final S s3 = automaton.addState();
        final S s4 = automaton.addState();
        mapping.put(s3, 3);
        mapping.put(s4, 4);

        Assert.assertEquals(mapping.get(s3).intValue(), 3);
        Assert.assertEquals(mapping.get(s4).intValue(), 4);
    }
}
