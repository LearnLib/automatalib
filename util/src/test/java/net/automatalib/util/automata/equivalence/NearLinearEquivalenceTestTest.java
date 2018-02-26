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
package net.automatalib.util.automata.equivalence;

import java.util.HashSet;
import java.util.Random;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
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
        Word<Integer> sepWord;

        sepWord = NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_1_PARTIAL, ALPHABET, true);
        Assert.assertNull(sepWord);

        sepWord = NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_1_PARTIAL, ALPHABET, false);
        Assert.assertNotNull(sepWord);

        checkPartialTrace(DFA_1_PARTIAL, sepWord);
    }

    @Test
    public void testDFACollectionPartial() {
        Word<Integer> sepWord;

        sepWord = NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_1_PARTIAL, new HashSet<>(ALPHABET), true);
        Assert.assertNull(sepWord);

        sepWord = NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_1_PARTIAL, new HashSet<>(ALPHABET), false);
        Assert.assertNotNull(sepWord);

        checkPartialTrace(DFA_1_PARTIAL, sepWord);
    }

    @Test
    public void testNonEqualDFAAlphabet() {
        final Word<Integer> sepWord = NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_2, ALPHABET);
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(DFA_1.computeOutput(sepWord), DFA_2.computeOutput(sepWord));
    }

    @Test
    public void testNonEqualDFACollection() {
        final Word<Integer> sepWord = NearLinearEquivalenceTest.findSeparatingWord(DFA_1, DFA_2, new HashSet<>(ALPHABET));
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(DFA_1.computeOutput(sepWord), DFA_2.computeOutput(sepWord));
    }

    private static <I> void checkPartialTrace(CompactDFA<I> dfa, Word<I> trace) {

        Integer iter = dfa.getInitialState();
        for (final I i : trace) {
            final Integer trans = dfa.getTransition(iter, i);
            if (trans == null) {
                return;
            }
            iter = dfa.getSuccessor(trans);
        }

        Assert.fail("We should have encountered an undefined transition");
    }
}
