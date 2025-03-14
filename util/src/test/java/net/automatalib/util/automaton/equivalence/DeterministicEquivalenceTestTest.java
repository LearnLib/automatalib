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
package net.automatalib.util.automaton.equivalence;

import java.util.Collection;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.function.BiIntFunction;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest.ArrayRegistry;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest.MapRegistry;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest.Pred;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest.Registry;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeterministicEquivalenceTestTest {

    private static final Random RANDOM = new Random(0);
    private static final int AUTOMATON_SIZE_SMALL = 20;

    // Equivalence check switches implementation when stateSize**2 is > 10000
    private static final int AUTOMATON_SIZE_LARGE = 200;

    @Test
    public void testEquivalenceDFA() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final DFA<?, Integer> a1 = RandomAutomata.randomDFA(RANDOM, AUTOMATON_SIZE_SMALL, alphabet);
        final DFA<?, Integer> a2 = RandomAutomata.randomDFA(RANDOM, AUTOMATON_SIZE_SMALL, alphabet);

        testEquivalenceInternal(a1, a1, alphabet, true);
        testEquivalenceInternal(a1, a2, alphabet, false);
        testEquivalenceInternal(a2, a1, alphabet, false);
    }

    @Test
    public void testEquivalenceDFALarge() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final DFA<?, Integer> a1 = RandomAutomata.randomDFA(RANDOM, AUTOMATON_SIZE_LARGE, alphabet, false);
        final DFA<?, Integer> a2 = RandomAutomata.randomDFA(RANDOM, AUTOMATON_SIZE_LARGE, alphabet, false);

        testEquivalenceInternal(a1, a1, alphabet, true);
        testEquivalenceInternal(a1, a2, alphabet, false);
        testEquivalenceInternal(a2, a1, alphabet, false);
    }

    @Test
    public void testEquivalenceMealy() {
        final Alphabet<Integer> inputAlphabet = Alphabets.integers(0, 5);
        final Alphabet<Character> outputAlphabet = Alphabets.characters('a', 'f');
        final MealyMachine<?, Integer, ?, Character> a1 =
                RandomAutomata.randomMealy(RANDOM, AUTOMATON_SIZE_SMALL, inputAlphabet, outputAlphabet);
        final MealyMachine<?, Integer, ?, Character> a2 =
                RandomAutomata.randomMealy(RANDOM, AUTOMATON_SIZE_SMALL, inputAlphabet, outputAlphabet);

        testEquivalenceInternal(a1, a1, inputAlphabet, true);
        testEquivalenceInternal(a1, a2, inputAlphabet, false);
        testEquivalenceInternal(a2, a1, inputAlphabet, false);
    }

    @Test
    public void testEquivalenceMealyLarge() {
        final Alphabet<Integer> inputAlphabet = Alphabets.integers(0, 5);
        final Alphabet<Character> outputAlphabet = Alphabets.characters('a', 'f');
        final MealyMachine<?, Integer, ?, Character> a1 =
                RandomAutomata.randomMealy(RANDOM, AUTOMATON_SIZE_LARGE, inputAlphabet, outputAlphabet);
        final MealyMachine<?, Integer, ?, Character> a2 =
                RandomAutomata.randomMealy(RANDOM, AUTOMATON_SIZE_LARGE, inputAlphabet, outputAlphabet);

        testEquivalenceInternal(a1, a1, inputAlphabet, true);
        testEquivalenceInternal(a1, a2, inputAlphabet, false);
        testEquivalenceInternal(a2, a1, inputAlphabet, false);
    }

    @Test
    public void testEmptyDFAs() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final CompactDFA<Integer> uninit = new CompactDFA<>(alphabet, 0);
        final CompactDFA<Integer> empty = new CompactDFA<>(alphabet, 1);
        empty.addInitialState(false);

        testForEmptySepWord(uninit, empty, alphabet);
    }

    @Test
    public void testEmptyMealies() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final CompactMealy<Integer, ?> uninit = new CompactMealy<>(alphabet, 0);
        final CompactMealy<Integer, ?> empty = new CompactMealy<>(alphabet, 1);
        empty.addInitialState();

        testForEmptySepWord(uninit, empty, alphabet);
    }

    /**
     * Test equivalence of very large automata which have previously resulted in integer overflows. See <a
     * href="https://github.com/LearnLib/automatalib/issues/84">Issue 84</a> for details.
     */
    @Test
    public void testIssue84() {
        final Word<Integer> sepWord = DeterministicEquivalenceTest.findSeparatingWord(TestUtil.LARGE_AUTOMATON_A,
                                                                                      TestUtil.LARGE_AUTOMATON_B,
                                                                                      TestUtil.ALPHABET);
        Assert.assertNotNull(sepWord);
        Assert.assertEquals(sepWord.length(), TestUtil.LARGE_AUTOMATON_A.size() - 1);
    }

    /**
     * Follow-up issue to {@link #testIssue84()} that checks correct index computation.
     */
    @Test
    public void testIssue84Index() {
        testIndexComputation(ArrayRegistry::new);
        testIndexComputation((int size1, int size2) -> new MapRegistry<>(size1));
    }

    private static <I> void testIndexComputation(BiIntFunction<Registry<Integer>> constructor) {
        final int size1 = 3;
        final int size2 = 5;
        final Registry<Integer> registry = constructor.apply(size1, size2);

        int cntr = 0;
        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
                registry.putPred(i, j, new Pred<>(null, cntr++));
            }
        }

        cntr = 0;
        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
                final Pred<Integer> pred = registry.getPred(i, j);
                Assert.assertNotNull(pred);
                Assert.assertEquals(pred.symbol, cntr++);
            }
        }
    }

    private static <I> void testForEmptySepWord(UniversalDeterministicAutomaton<?, I, ?, ?, ?> a1,
                                                UniversalDeterministicAutomaton<?, I, ?, ?, ?> a2,
                                                Collection<? extends I> inputs) {

        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(a1, a1, inputs));
        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(a2, a2, inputs));

        final Word<I> sepWord1 = DeterministicEquivalenceTest.findSeparatingWord(a1, a2, inputs);
        Assert.assertEquals(sepWord1, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord1), a2.getState(sepWord1));

        final Word<I> sepWord2 = DeterministicEquivalenceTest.findSeparatingWord(a2, a1, inputs);
        Assert.assertEquals(sepWord2, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord2), a2.getState(sepWord2));
    }

    private <I, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, ?>> void testEquivalenceInternal(M a1,
                                                                                                                      M a2,
                                                                                                                      Alphabet<I> alphabet,
                                                                                                                      boolean equivalent) {
        final UniversalDeterministicAutomaton<?, I, ?, ?, ?> m1 = a1;
        final UniversalDeterministicAutomaton<?, I, ?, ?, ?> m2 = a2;

        final Word<I> separatingWord = DeterministicEquivalenceTest.findSeparatingWord(m1, m2, alphabet);

        Assert.assertEquals(equivalent, separatingWord == null);

        if (!equivalent) {
            Assert.assertNotEquals(a1.computeOutput(separatingWord), a2.computeOutput(separatingWord));
        }
    }
}
