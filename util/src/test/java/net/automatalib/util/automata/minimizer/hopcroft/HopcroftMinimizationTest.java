/* Copyright (C) 2013-2018 TU Dortmund
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

import java.util.Collection;

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
import org.testng.annotations.Test;

@Test
public class HopcroftMinimizationTest {

    @Test
    public void createTestDFA1() {
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

        testMinimizeDFA(new TestDFA<>(alphabet, dfa, 5, false));
    }

    @Test
    public void createTestDFA2() {
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

        testMinimizeDFA(new TestDFA<>(alphabet, dfa, 4, true));
    }

    @Test
    public void createTestDFA3() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'b');
        final char input1 = 'a';
        final char input2 = 'b';

        // @formatter:off
        final DFA<?, Character> dfa = AutomatonBuilders.newDFA(alphabet)
                .withInitial("s0")
                .from("s0")
                   .on(input1).to("s2")
                   .on(input2).to("s1")
                .from("s1")
                   .on(input1).loop()
                   .on(input2).loop()
                .from("s2")
                   .on(input1).to("s1")
                   .on(input2).to("s3")
                .from("s3")
                   .on(input1).to("s4")
                   .on(input2).to("s1")
                .from("s4")
                   .on(input1).to("s5")
                   .on(input2).to("s6")
                .from("s5")
                   .on(input1, input2).loop()
                .from("s6")
                   .on(input1).to("s4")
                   .on(input2).to("s5")
                .withAccepting("s0", "s1", "s2", "s3")
                .create();
        // @formatter:on

        testMinimizeDFA(new TestDFA<>(alphabet, dfa, 5, true));
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
