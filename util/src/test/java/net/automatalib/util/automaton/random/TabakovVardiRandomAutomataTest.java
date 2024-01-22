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
        Assert.assertEquals(size, compactNFA.size());
        Assert.assertEquals(1, compactNFA.getInitialStates().size());
        for (int s : compactNFA.getStates()) {
            if (s == 0 || s == 3) {
                // 2 accepting states
                Assert.assertTrue(compactNFA.isAccepting(s));
            } else {
                Assert.assertFalse(compactNFA.isAccepting(s));
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int s : compactNFA.getStates()) {
            sb.append(compactNFA.getTransitions(s, 0));
        }
        // 5 transitions for 0
        Assert.assertEquals("[0, 3][][1, 2][3]", sb.toString());

        sb = new StringBuilder();
        for (int s : compactNFA.getStates()) {
            sb.append(compactNFA.getTransitions(s, 1));
        }
        // 5 transitions for 1
        Assert.assertEquals("[3][1][0][1, 2]", sb.toString());
    }

    @Test
    void testGenerateNFAEdgeCase() {
        Random r = new Random(42);
        int size = 2;
        float td = 0f; // 0 transitions
        float ad = 0.5f; // exactly 1 accepting state
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        CompactNFA<Integer> compactNFA = TabakovVardiRandomAutomata.generateNFA(r, size, td, ad, alphabet);
        Assert.assertEquals(size, compactNFA.size());
        Assert.assertEquals(1, compactNFA.getInitialStates().size());
        Assert.assertTrue(compactNFA.isAccepting(0));
        Assert.assertFalse(compactNFA.isAccepting(1));
        Assert.assertEquals(0, compactNFA.getTransitions(0).size());
        Assert.assertEquals(0, compactNFA.getTransitions(1).size());
    }
}
