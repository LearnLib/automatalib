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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NearLinearEquivalenceTestTest {

    private static final Alphabet<Integer> ALPHABET;
    private static final CompactDFA<Integer> DFA_1;
    private static final CompactDFA<Integer> DFA_1_PARTIAL;
    private static final CompactDFA<Integer> DFA_2;

    private static final int AUTOMATON_SIZE = 20;

    static {
        ALPHABET = Alphabets.integers(0, 5);

        final Random r = new Random(0);
        DFA_1 = RandomAutomata.randomDFA(r, AUTOMATON_SIZE, ALPHABET, false);
        DFA_2 = RandomAutomata.randomDFA(r, AUTOMATON_SIZE, ALPHABET, false);

        DFA_1_PARTIAL = new CompactDFA<>(DFA_1);

        // remove arbitrary transition
        final Integer transition = DFA_1_PARTIAL.getTransition(15, 2);
        DFA_1_PARTIAL.removeTransition(15, 2, transition);
    }

    @Test
    public void testEqualDFAAlphabet() {
        final Word<Integer> sepWord = NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_1, ALPHABET);
        Assert.assertNull(sepWord);
    }

    @Test
    public void testEqualDFACollection() {
        final Word<Integer> sepWord =
                NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_1, new HashSet<>(ALPHABET));
        Assert.assertNull(sepWord);
    }

    @Test
    public void testDFAAlphabetPartial() {
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_1_PARTIAL, ALPHABET, true));
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(DFA_1_PARTIAL, DFA_1, ALPHABET, true));

        Word<Integer> sepWord1 = NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_1_PARTIAL, ALPHABET, false);
        Word<Integer> sepWord2 = NearLinearEquivalenceTest.findSeparatingWord(DFA_1_PARTIAL, DFA_1, ALPHABET, false);
        Assert.assertNotNull(sepWord1);
        Assert.assertNotNull(sepWord2);

        checkPartialTrace(DFA_1_PARTIAL, sepWord1);
        checkPartialTrace(DFA_1_PARTIAL, sepWord2);
    }

    @Test
    public void testDFACollectionPartial() {
        final Set<Integer> inputSet = new HashSet<>(ALPHABET);

        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_1_PARTIAL, inputSet, true));
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(DFA_1_PARTIAL, DFA_1, inputSet, true));

        Word<Integer> sepWord1 = NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_1_PARTIAL, inputSet, false);
        Word<Integer> sepWord2 = NearLinearEquivalenceTest.findSeparatingWord(DFA_1_PARTIAL, DFA_1, inputSet, false);
        Assert.assertNotNull(sepWord1);
        Assert.assertNotNull(sepWord2);

        checkPartialTrace(DFA_1_PARTIAL, sepWord1);
        checkPartialTrace(DFA_1_PARTIAL, sepWord2);
    }

    @Test
    public void testNonEqualDFAAlphabet() {
        final Word<Integer> sepWord1 = NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_2, ALPHABET);
        final Word<Integer> sepWord2 = NearLinearEquivalenceTest.findSeparatingWord(DFA_2, DFA_1, ALPHABET);

        Assert.assertNotNull(sepWord1);
        Assert.assertNotNull(sepWord2);
        Assert.assertNotEquals(DFA_1.computeOutput(sepWord1), DFA_2.computeOutput(sepWord1));
        Assert.assertNotEquals(DFA_1.computeOutput(sepWord2), DFA_2.computeOutput(sepWord2));
    }

    @Test
    public void testNonEqualDFACollection() {
        final Set<Integer> inputSet = new HashSet<>(ALPHABET);

        final Word<Integer> sepWord1 = NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_2, inputSet);
        final Word<Integer> sepWord2 = NearLinearEquivalenceTest.findSeparatingWord(DFA_2, DFA_1, inputSet);

        Assert.assertNotNull(sepWord1);
        Assert.assertNotNull(sepWord2);
        Assert.assertNotEquals(DFA_1.computeOutput(sepWord1), DFA_2.computeOutput(sepWord1));
        Assert.assertNotEquals(DFA_1.computeOutput(sepWord2), DFA_2.computeOutput(sepWord2));
    }

    @Test
    public void testEmptyDFAsAlphabet() {
        final CompactDFA<Integer> uninit = new CompactDFA<>(ALPHABET, 0);
        final CompactDFA<Integer> empty = new CompactDFA<>(ALPHABET, 1);

        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(uninit, empty, ALPHABET));
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(empty, uninit, ALPHABET));

        empty.addInitialState(false);

        testForEmptySepWord(uninit, empty, ALPHABET);
        testForEmptySepWord(empty, uninit, ALPHABET);
    }

    @Test
    public void testEmptyDFAsCollection() {
        final CompactDFA<Integer> uninit = new CompactDFA<>(ALPHABET, 0);
        final CompactDFA<Integer> empty = new CompactDFA<>(ALPHABET, 1);
        final Set<Integer> inputSet = new HashSet<>(ALPHABET);

        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(uninit, empty, inputSet));
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(empty, uninit, inputSet));

        empty.addInitialState(false);

        testForEmptySepWord(uninit, empty, inputSet);
        testForEmptySepWord(empty, uninit, inputSet);
    }

    @Test
    public void testEmptyMealiesAlphabet() {
        final CompactMealy<Integer, ?> uninit = new CompactMealy<>(ALPHABET, 0);
        final CompactMealy<Integer, ?> empty = new CompactMealy<>(ALPHABET, 1);

        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(uninit, empty, ALPHABET));
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(empty, uninit, ALPHABET));

        empty.addInitialState();

        testForEmptySepWord(uninit, empty, ALPHABET);
        testForEmptySepWord(empty, uninit, ALPHABET);
    }

    @Test
    public void testEmptyMealiesCollection() {
        final CompactMealy<Integer, ?> uninit = new CompactMealy<>(ALPHABET, 0);
        final CompactMealy<Integer, ?> empty = new CompactMealy<>(ALPHABET, 1);
        final Set<Integer> inputSet = new HashSet<>(ALPHABET);

        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(uninit, empty, inputSet));
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(empty, uninit, inputSet));

        empty.addInitialState();

        testForEmptySepWord(uninit, empty, inputSet);
        testForEmptySepWord(empty, uninit, inputSet);
    }

    /**
     * Test equivalence of very large automata which have previously resulted in integer overflows. See <a
     * href="https://github.com/LearnLib/automatalib/issues/84">Issue 84</a> for details. While the issue hasn't been
     * reported for {@link NearLinearEquivalenceTest}, the class should handle large automata as well.
     */
    @Test
    public void testIssue84() {
        final Word<Integer> sepWord = NearLinearEquivalenceTest.findSeparatingWord(TestUtil.LARGE_AUTOMATON_A,
                                                                                   TestUtil.LARGE_AUTOMATON_B,
                                                                                   TestUtil.ALPHABET);
        Assert.assertNotNull(sepWord);
        Assert.assertEquals(sepWord.length(), TestUtil.LARGE_AUTOMATON_A.size() - 1);
    }

    private static <I> void testForEmptySepWord(UniversalDeterministicAutomaton<?, I, ?, ?, ?> a1,
                                                UniversalDeterministicAutomaton<?, I, ?, ?, ?> a2,
                                                Collection<I> inputs) {
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(a1, a1, inputs));
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(a2, a2, inputs));

        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(a1, a2, inputs, true));
        final Word<I> sepWord1 = NearLinearEquivalenceTest.findSeparatingWord(a1, a2, inputs);
        Assert.assertEquals(sepWord1, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord1), a2.getState(sepWord1));

        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(a2, a1, inputs, true));
        final Word<I> sepWord2 = NearLinearEquivalenceTest.findSeparatingWord(a2, a1, inputs);
        Assert.assertEquals(sepWord2, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord2), a2.getState(sepWord2));

        // Non-alphabet (non-integer-abstraction) version
        final Set<I> inputsAsSet = new HashSet<>(inputs);
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(a1, a1, inputsAsSet));
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(a2, a2, inputsAsSet));

        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(a1, a2, inputsAsSet, true));
        final Word<I> sepWord3 = NearLinearEquivalenceTest.findSeparatingWord(a1, a2, inputsAsSet);
        Assert.assertEquals(sepWord3, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord3), a2.getState(sepWord3));

        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(a2, a1, inputsAsSet, true));
        final Word<I> sepWord4 = NearLinearEquivalenceTest.findSeparatingWord(a2, a1, inputsAsSet);
        Assert.assertEquals(sepWord4, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord4), a2.getState(sepWord4));
    }

    private static <I> void checkPartialTrace(CompactDFA<I> dfa, Word<I> trace) {

        Integer iter = dfa.getInitialState();
        for (I i : trace) {
            final Integer trans = dfa.getTransition(iter, i);
            if (trans == null) {
                return;
            }
            iter = dfa.getSuccessor(trans);
        }

        Assert.fail("We should have encountered an undefined transition");
    }
}
