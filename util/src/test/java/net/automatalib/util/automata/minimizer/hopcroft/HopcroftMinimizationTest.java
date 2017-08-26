/* Copyright (C) 2013-2017 TU Dortmund
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
package net.automatalib.util.automata.minimizer.hopcroft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Iterators;
import net.automatalib.automata.Automaton;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.automata.minimizer.hopcroft.HopcroftMinimization.PruningMode;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class HopcroftMinimizationTest {

    private List<TestDFA<?>> testDfas;

    @BeforeClass
    public void setUp() {
        testDfas = new ArrayList<>();
        testDfas.add(createTestDFA1());
        testDfas.add(createTestDFA2());
    }

    private static TestDFA<Integer> createTestDFA1() {
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

        // @formatter:off
        CompactDFA<Integer> dfa = AutomatonBuilders.newDFA(alphabet)
                .withInitial("A")
                .from("A")
                    .on(0).to("H")
                    .on(1).to("B")
                .from("B")
                    .on(0).to("H")
                    .on(1).to("A")
                .from("C")
                    .on(0).to("E")
                    .on(1).to("F")
                .from("D")
                    .on(0).to("E")
                    .on(1).to("F")
                .from("E")
                    .on(0).to("F")
                    .on(1).to("G")
                .from("F")
                    .on(0, 1).loop()
                .from("G")
                    .on(0).loop()
                    .on(1).to("F")
                .from("H")
                    .on(0, 1).to("C")
                .withAccepting("F", "G")
                .create();
        // @formatter:on

        return new TestDFA<>(alphabet, dfa, 5, false);
    }

    private static TestDFA<Integer> createTestDFA2() {
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

        // @formatter:off
        CompactDFA<Integer> dfa = AutomatonBuilders.newDFA(alphabet)
                .withInitial("A")
                .from("A")
                    .on(0).to("B")
                    .on(1).to("C")
                .from("B")
                    .on(0, 1).to("D")
                .from("C")
                    .on(0, 1).to("D")
                .from("D")
                    .on(0, 1).to("E")
                .from("E")
                    .on(0, 1).loop()
                .withAccepting("E")
                .create();
        // @formatter:on

        return new TestDFA<>(alphabet, dfa, 4, true);
    }

    @Test
    public void testDfas() {
        for (TestDFA<?> testDfa : testDfas) {
            testMinimizeDFA(testDfa);
        }
    }

    private <I> void testMinimizeDFA(TestDFA<I> testDfa) {
        testMinimizeDFA(testDfa.alphabet, testDfa.dfa, testDfa.minimalSize, testDfa.initiallyConnected);
    }

    protected <I> void testMinimizeDFA(Alphabet<I> alphabet,
                                       DFA<?, I> dfa,
                                       int expectedStateCount,
                                       boolean initiallyConnected) {
        CompactDFA<I> resultBefore = HopcroftMinimization.minimizeDFA(dfa, alphabet, PruningMode.PRUNE_BEFORE);
        Assert.assertEquals(resultBefore.size(), expectedStateCount);
        assertMinimal(resultBefore);

        CompactDFA<I> resultAfter = HopcroftMinimization.minimizeDFA(dfa, alphabet, PruningMode.PRUNE_AFTER);
        Assert.assertEquals(resultAfter.size(), expectedStateCount);
        assertMinimal(resultAfter);

        CompactDFA<I> resultUnpruned = HopcroftMinimization.minimizeDFA(dfa, alphabet, PruningMode.DONT_PRUNE);
        if (initiallyConnected) {
            Assert.assertEquals(resultUnpruned.size(), expectedStateCount);
            assertMinimal(resultUnpruned);
        } else {
            assertAllInequivalent(resultUnpruned, alphabet);
        }
    }

    protected static <S, I> void assertAllInequivalent(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                       Collection<? extends I> inputs) {
        StateIDs<S> ids = automaton.stateIDs();
        int size = automaton.size();
        for (int i = 0; i < size - 1; i++) {
            S s1 = ids.getState(i);
            for (int j = i + 1; j < size; j++) {
                S s2 = ids.getState(j);
                Assert.assertNotNull(Automata.findSeparatingWord(automaton, s1, s2, inputs));
            }
        }
    }

    protected static <I, A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & InputAlphabetHolder<I>> void assertMinimal(
            A automaton) {
        assertMinimal(automaton, automaton.getInputAlphabet());
    }

    protected static <I> void assertMinimal(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton,
                                            Collection<? extends I> inputs) {
        assertAllReachable(automaton, inputs);
        assertAllInequivalent(automaton, inputs);
    }

    protected static <I> void assertAllReachable(Automaton<?, I, ?> automaton, Collection<? extends I> inputs) {
        int numReachable = Iterators.size(Automata.bfsOrderIterator(automaton, inputs));
        Assert.assertEquals(numReachable, automaton.size());
    }

    private static class TestDFA<I> {

        public final Alphabet<I> alphabet;
        public final DFA<?, I> dfa;
        public final int minimalSize;
        public final boolean initiallyConnected;

        TestDFA(Alphabet<I> alphabet, DFA<?, I> dfa, int minimalSize, boolean initiallyConnected) {
            this.alphabet = alphabet;
            this.dfa = dfa;
            this.minimalSize = minimalSize;
            this.initiallyConnected = initiallyConnected;
        }
    }

}
