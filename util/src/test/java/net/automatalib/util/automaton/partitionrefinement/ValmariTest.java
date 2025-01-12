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
package net.automatalib.util.automaton.partitionrefinement;

import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.fsa.NFAs;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.util.automaton.random.TabakovVardiRandomAutomata;
import net.automatalib.util.partitionrefinement.Valmari;
import net.automatalib.util.partitionrefinement.ValmariExtractors;
import net.automatalib.util.partitionrefinement.ValmariInitializers;
import net.automatalib.util.ts.TS;
import net.automatalib.util.ts.TS.TransRef;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValmariTest {

    @Test
    void testDFABisimulation() {

        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

        for (int size = 5; size < 20; size++) {
            for (int seed = 0; seed < 500; seed++) {
                final CompactDFA<Integer> src = RandomAutomata.randomDFA(new Random(seed), size, alphabet, false);
                final CompactDFA<Integer> tgt1 = HopcroftMinimizer.minimizeDFA(src);

                final Valmari valmari = ValmariInitializers.initializeNFA(src);
                valmari.computeCoarsestStablePartition();
                final CompactDFA<Integer> tgt2 =
                        ValmariExtractors.toUniversal(valmari, src, alphabet, new CompactDFA.Creator<>());
                final CompactDFA<Integer> tgt3 =
                        ValmariExtractors.toUniversal(valmari, src, alphabet, new CompactDFA.Creator<>(), false);

                final String debug = "size: " + size + ", seed; " + seed;
                Assert.assertEquals(tgt2.size(), tgt1.size(), debug);
                Assert.assertTrue(Automata.testEquivalence(tgt1, tgt2, alphabet), debug);
                Assert.assertTrue(Automata.testEquivalence(tgt1, tgt3, alphabet), debug);
            }
        }
    }

    @Test
    void testNFABisimulation() {

        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

        for (int size = 5; size < 20; size++) {
            for (int seed = 0; seed < 500; seed++) {
                final CompactNFA<Integer> src =
                        TabakovVardiRandomAutomata.generateNFA(new Random(seed), size, 1.25f, 0.5f, alphabet);

                final Valmari valmari = ValmariInitializers.initializeNFA(src, alphabet);
                valmari.computeCoarsestStablePartition();
                final CompactNFA<Integer> val = ValmariExtractors.toNFA(valmari, src, alphabet);
                final String debug = "size: " + size + ", seed; " + seed;

                final CompactDFA<Integer> srcDFA = NFAs.determinize(src, true, false);
                final CompactDFA<Integer> valDFA = NFAs.determinize(val, true, false);

                Assert.assertTrue(Automata.testEquivalence(srcDFA, valDFA, alphabet), debug);

                final CompactDFA<Integer> srcDFAmin = NFAs.determinize(src);
                final CompactDFA<Integer> valDFAmin = NFAs.determinize(val);

                Assert.assertEquals(srcDFAmin.size(), valDFAmin.size(), debug);
                Assert.assertTrue(Automata.testEquivalence(srcDFAmin, valDFAmin, alphabet), debug);
            }
        }
    }

    @Test
    void testNFABisimulationNoPrune() {

        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

        for (int size = 5; size < 20; size++) {
            for (int seed = 0; seed < 500; seed++) {
                final CompactNFA<Integer> src =
                        TabakovVardiRandomAutomata.generateNFA(new Random(seed), size, 1.25f, 0.5f, alphabet);

                final Valmari valmari = ValmariInitializers.initializeNFA(src, alphabet);
                valmari.computeCoarsestStablePartition();
                final CompactNFA<Integer> val = ValmariExtractors.toNFA(valmari, src, alphabet, false);
                final String debug = "size: " + size + ", seed; " + seed;

                final CompactDFA<Integer> srcDFA = NFAs.determinize(src, true, false);
                final CompactDFA<Integer> valDFA = NFAs.determinize(val, true, false);

                Assert.assertTrue(Automata.testEquivalence(srcDFA, valDFA, alphabet), debug);

                final CompactDFA<Integer> srcDFAmin = NFAs.determinize(src);
                final CompactDFA<Integer> valDFAmin = NFAs.determinize(val);

                Assert.assertEquals(srcDFAmin.size(), valDFAmin.size(), debug);
                Assert.assertTrue(Automata.testEquivalence(srcDFAmin, valDFAmin, alphabet), debug);
            }
        }
    }

    @Test
    void testPartialNFA() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        final CompactNFA<Integer> nfa = new CompactNFA<>(alphabet);

        // @formatter:off
        AutomatonBuilders.forNFA(nfa)
                         .from("q0").on(0).to("q1")
                         .from("q1").on(0).to("q1")
                         .from("q2").on(1).to("q3")
                         .from("q3").on(1).to("q3")
                         .withInitial("q0", "q2")
                         .withAccepting("q0", "q2")
                         .create();
        // @formatter:on

        Valmari valmari = ValmariInitializers.initializeNFA(nfa, alphabet);
        valmari.computeCoarsestStablePartition();

        CompactNFA<Integer> min = ValmariExtractors.toNFA(valmari, nfa, alphabet);

        Assert.assertEquals(nfa.size(), 4);
        Assert.assertEquals(min.size(), 4);

        final Integer sink = nfa.addState(false);

        for (TransRef<Integer, Integer, ?> t : TS.allUndefinedTransitions(nfa, nfa, alphabet)) {
            nfa.addTransition(t.state, t.input, sink);
        }

        valmari = ValmariInitializers.initializeNFA(nfa, alphabet);
        valmari.computeCoarsestStablePartition();
        min = ValmariExtractors.toNFA(valmari, nfa, alphabet);

        Assert.assertEquals(nfa.size(), 5);
        Assert.assertEquals(min.size(), 2);
    }

    @Test
    void testNFAPrune() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        final CompactNFA<Integer> nfa = new CompactNFA<>(alphabet);

        // @formatter:off
        AutomatonBuilders.forNFA(nfa)
                         .from("q0")
                            .on(0).to("q1")
                            .on(1).to("q2")
                         .from("q'0")
                            .on(0).to("q'1")
                            .on(1).to("q'2")
                         .withInitial("q0")
                         .withAccepting("q0", "q'1", "q'2")
                         .create();
        // @formatter:on

        Valmari valmari = ValmariInitializers.initializeNFA(nfa, alphabet);
        valmari.computeCoarsestStablePartition();

        CompactNFA<Integer> min = ValmariExtractors.toNFA(valmari, nfa, alphabet, false);

        Assert.assertEquals(nfa.size(), 6);
        Assert.assertEquals(min.size(), 4);

        min = ValmariExtractors.toNFA(valmari, nfa, alphabet);

        Assert.assertEquals(min.size(), 2);
    }
}
