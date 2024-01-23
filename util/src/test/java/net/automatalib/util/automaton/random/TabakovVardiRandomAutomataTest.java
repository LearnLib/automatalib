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
package net.automatalib.util.automaton.random;

import java.util.Random;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import org.testng.Assert;
import org.testng.annotations.Test;

class TabakovVardiRandomAutomataTest {
    @Test
    void testGenerateNFA() {
        Random r = new Random(42);
        int size = 4;
        float td = 1.25f; // exactly 5 transitions per letter
        float ad = 0.5f; // exactly 2 accepting states
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        CompactNFA<Integer> compactNFA = TabakovVardiRandomAutomata.generateNFA(r, size, td, ad, alphabet);
        Assert.assertEquals(compactNFA.size(), size);
        Assert.assertEquals(compactNFA.getInitialStates().size(), 1);
        for (int s : compactNFA.getStates()) {
            if (s == 0 || s == 3) {
                // 2 accepting states
                Assert.assertTrue(compactNFA.isAccepting(s));
            } else {
                Assert.assertFalse(compactNFA.isAccepting(s));
            }
        }
        // 5 transitions for 0
        Assert.assertEquals(compactNFA.getTransitions(0, 0), Set.of(0, 3));
        Assert.assertTrue(compactNFA.getTransitions(1, 0).isEmpty());
        Assert.assertEquals(compactNFA.getTransitions(2, 0), Set.of(1, 2));
        Assert.assertEquals(compactNFA.getTransitions(3, 0), Set.of(3));

        // 5 transitions for 1
        Assert.assertEquals(compactNFA.getTransitions(0, 1), Set.of(3));
        Assert.assertEquals(compactNFA.getTransitions(1, 1), Set.of(1));
        Assert.assertEquals(compactNFA.getTransitions(2, 1), Set.of(0));
        Assert.assertEquals(compactNFA.getTransitions(3, 1), Set.of(1, 2));
    }

    @Test
    void testGenerateNFAEdgeCase() {
        Random r = new Random(42);
        int size = 2;
        float td = 0f; // 0 transitions
        float ad = 0.5f; // exactly 1 accepting state
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        CompactNFA<Integer> compactNFA = TabakovVardiRandomAutomata.generateNFA(r, size, td, ad, alphabet);
        Assert.assertEquals(compactNFA.size(), size);
        Assert.assertEquals(compactNFA.getInitialStates().size(), 1);
        Assert.assertTrue(compactNFA.isAccepting(0));
        Assert.assertFalse(compactNFA.isAccepting(1));
        Assert.assertTrue(compactNFA.getTransitions(0).isEmpty());
        Assert.assertTrue(compactNFA.getTransitions(1).isEmpty());
    }
}
