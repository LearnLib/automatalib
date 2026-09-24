/* Copyright (C) 2013-2026 TU Dortmund University
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.UniversalDeterministicAutomaton.RegularAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.function.BiIntFunction;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest.ArrayRegistry;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest.MapRegistry;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest.Pred;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest.Registry;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.util.automaton.transducer.MutableMealyMachines;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;
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

    @Test
    public void testStateEquivalenceDFA() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        final CompactDFA<Integer> dfa = RandomAutomata.randomDFA(new Random(1), AUTOMATON_SIZE_SMALL, alphabet, false);

        // for this seed, states 12 and 16 are equivalent
        final Integer init = dfa.getInitialState();
        final Integer reject = dfa.getState(2);
        final Integer s12 = dfa.getState(12);
        final Integer s16 = dfa.getState(16);

        testStateEquivalenceDFAInternal(dfa, init, s12, s16, alphabet, false);
        testStateEquivalenceDFAInternal(dfa, reject, s12, s16, alphabet, false);

        for (Integer s : dfa) {
            dfa.removeAllTransitions(s, 0);
        }

        testStateEquivalenceDFAInternal(dfa, init, s12, s16, alphabet, true);
        testStateEquivalenceDFAInternal(dfa, reject, s12, s16, alphabet, true);
    }

    @Test
    public void testStateEquivalenceMealy() throws IOException, FormatException {
        try (InputStream is = DeterministicEquivalenceTestTest.class.getResourceAsStream("/automata/partialMealy.dot")) {
            var parser = DOTParsers.mealy(DeterministicEquivalenceTestTest::parseEdges);
            var model = parser.readModel(is);

            final CompactMealy<Integer, Integer> mealy = model.model;
            final Alphabet<Integer> alphabet = model.alphabet;

            Integer l1 = mealy.getState(Word.fromLetter(0));
            Integer l2 = mealy.getState(Word.fromSymbols(0, 0));
            Integer l3 = mealy.getState(Word.fromSymbols(0, 0, 2));
            Integer r1 = mealy.getState(Word.fromLetter(1));

            // l1 <> r1
            testStateEquivalenceMealyInternal(mealy, l1, r1, alphabet, false);
            testStateEquivalenceMealyInternal(mealy, r1, l1, alphabet, false);

            // mut
            mealy.addTransition(l2, 0, l3, 10);
            MutableMealyMachines.complete(mealy, alphabet, 42);

            testStateEquivalenceMealyInternal(mealy, l1, r1, alphabet, true);
            testStateEquivalenceMealyInternal(mealy, r1, l1, alphabet, true);
        }
    }

    @Test
    public void testPartialDFA() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final CompactDFA<Integer> dfa = RandomAutomata.randomDFA(new Random(0), 20, alphabet, false);
        final CompactDFA<Integer> dfaPartial = new CompactDFA<>(dfa);

        // remove arbitrary transition
        final int state = dfaPartial.getState(15);
        final Integer transition = dfaPartial.getTransition(state, 2);
        dfaPartial.removeTransition(state, 2, transition);

        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(dfa, dfaPartial, alphabet, true));
        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(dfaPartial, dfa, alphabet, true));

        Word<Integer> sepWord1 = DeterministicEquivalenceTest.findSeparatingWord(dfa, dfaPartial, alphabet, false);
        Word<Integer> sepWord2 = DeterministicEquivalenceTest.findSeparatingWord(dfaPartial, dfa, alphabet, false);
        Assert.assertNotNull(sepWord1);
        Assert.assertNotNull(sepWord2);

        checkPartialTrace(dfaPartial, sepWord1);
        checkPartialTrace(dfaPartial, sepWord2);
    }

    /**
     * This test checks a setup from an external benchmark, in which the old (erroneous
     * {@link NearLinearEquivalenceTest near-linear}) implementation failed to find a separating word in a partial
     * automaton.
     */
    @Test
    public void testPartialIntegration() throws IOException, FormatException {
        final Word<Integer> input = Word.fromSymbols(9, 0, 9, 6, 6, 1, 6, 6, 11, 11, 8, 0, 5, 0);
        final Word<Integer> hypOut = Word.fromSymbols(2, 3, 2, 2, 2, 2, 2, 2, 5, 5, 6, 2, 7, 0);
        final Word<Integer> dagOut = Word.fromSymbols(2, 3, 2, 2, 2, 2, 2, 2, 5, 5, 6, 2, 7, 3);

        try (InputStream dagis = DeterministicEquivalenceTestTest.class.getResourceAsStream(
                "/automata/partialMealyDAG.dot");
             InputStream hypis = DeterministicEquivalenceTestTest.class.getResourceAsStream(
                     "/automata/partialMealyHyp.dot")) {

            var parser = DOTParsers.mealy(DeterministicEquivalenceTestTest::parseEdges);
            var dagM = parser.readModel(dagis);
            var hypM = parser.readModel(hypis);

            final CompactMealy<Integer, Integer> dag = dagM.model;
            final CompactMealy<Integer, Integer> hyp = hypM.model;

            Assert.assertEquals(dagM.alphabet, hypM.alphabet);

            final Alphabet<Integer> inputs = dagM.alphabet;

            Assert.assertEquals(hypOut, hyp.computeOutput(input));
            Assert.assertEquals(dagOut, dag.computeOutput(input));

            final Word<Integer> sepWord1 = DeterministicEquivalenceTest.findSeparatingWord(dag, hyp, inputs, true);

            Assert.assertNotNull(sepWord1);
            Assert.assertNotEquals(hyp.computeOutput(sepWord1), dag.computeOutput(sepWord1));

            final Word<Integer> sepWord2 = DeterministicEquivalenceTest.findSeparatingWord(hyp, dag, inputs, true);

            Assert.assertNotNull(sepWord2);
            Assert.assertNotEquals(dag.computeOutput(sepWord2), hyp.computeOutput(sepWord2));
        }
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

    private static void testIndexComputation(BiIntFunction<Registry<Integer>> constructor) {
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

    private static <I> void testForEmptySepWord(RegularAutomaton<?, I, ?, ?, ?> a1,
                                                RegularAutomaton<?, I, ?, ?, ?> a2,
                                                Collection<? extends I> inputs) {

        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(a1, a1, inputs));
        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(a2, a2, inputs));

        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(a1, a2, inputs, true));
        final Word<I> sepWord1 = DeterministicEquivalenceTest.findSeparatingWord(a1, a2, inputs);
        Assert.assertEquals(sepWord1, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord1), a2.getState(sepWord1));

        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(a2, a1, inputs, true));
        final Word<I> sepWord2 = DeterministicEquivalenceTest.findSeparatingWord(a2, a1, inputs);
        Assert.assertEquals(sepWord2, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord2), a2.getState(sepWord2));
    }

    private <I> void testStateEquivalenceDFAInternal(CompactDFA<I> dfa,
                                                     Integer inequiv,
                                                     Integer equiv1,
                                                     Integer equiv2,
                                                     Alphabet<I> alphabet,
                                                     boolean partial) {
        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(dfa, inequiv, inequiv, alphabet, partial));
        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(dfa, equiv1, equiv1, alphabet, partial));
        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(dfa, equiv2, equiv2, alphabet, partial));

        Word<I> sepWord = DeterministicEquivalenceTest.findSeparatingWord(dfa, inequiv, equiv1, alphabet, partial);
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(dfa.computeStateOutput(inequiv, sepWord), dfa.computeStateOutput(equiv1, sepWord));

        sepWord = DeterministicEquivalenceTest.findSeparatingWord(dfa, equiv1, inequiv, alphabet, partial);
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(dfa.computeStateOutput(inequiv, sepWord), dfa.computeStateOutput(equiv1, sepWord));

        sepWord = DeterministicEquivalenceTest.findSeparatingWord(dfa, inequiv, equiv2, alphabet, partial);
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(dfa.computeStateOutput(inequiv, sepWord), dfa.computeStateOutput(equiv2, sepWord));

        sepWord = DeterministicEquivalenceTest.findSeparatingWord(dfa, equiv2, inequiv, alphabet, partial);
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(dfa.computeStateOutput(inequiv, sepWord), dfa.computeStateOutput(equiv2, sepWord));

        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(dfa, equiv1, equiv2, alphabet, partial));
        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(dfa, equiv2, equiv1, alphabet, partial));

        final CompactDFA<I> doubleCheck = new CompactDFA<>(dfa);
        dfa.setInitialState(equiv1);
        doubleCheck.setInitialState(equiv2);

        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(dfa, doubleCheck, alphabet, partial));
        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(doubleCheck, dfa, alphabet, partial));
    }

    private <I, O> void testStateEquivalenceMealyInternal(CompactMealy<I, O> mealy,
                                                          Integer s1,
                                                          Integer s2,
                                                          Alphabet<I> alphabet,
                                                          boolean equalSepWord) {
        final Word<I> sepWordPartial = DeterministicEquivalenceTest.findSeparatingWord(mealy, s1, s2, alphabet, true);
        Assert.assertNotNull(sepWordPartial);
        Assert.assertNotEquals(mealy.computeStateOutput(s2, sepWordPartial),
                               mealy.computeStateOutput(s1, sepWordPartial));

        final Word<I> sepWord = DeterministicEquivalenceTest.findSeparatingWord(mealy, s1, s2, alphabet);
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(mealy.computeStateOutput(s2, sepWord), mealy.computeStateOutput(s1, sepWord));

        if (equalSepWord) {
            Assert.assertEquals(sepWord, sepWordPartial);
        } else {
            Assert.assertTrue(sepWord.size() < sepWordPartial.size());
        }
    }

    private <I, M extends RegularAutomaton<?, I, ?, ?, ?> & Output<I, ?>> void testEquivalenceInternal(M a1,
                                                                                                       M a2,
                                                                                                       Alphabet<I> alphabet,
                                                                                                       boolean equivalent) {
        // explicitly assign type to (redundant) variables, otherwise javac complains
        final RegularAutomaton<?, I, ?, ?, ?> m1 = a1;
        final RegularAutomaton<?, I, ?, ?, ?> m2 = a2;

        final Word<I> separatingWord = DeterministicEquivalenceTest.findSeparatingWord(m1, m2, alphabet);

        Assert.assertEquals(equivalent, separatingWord == null);

        if (!equivalent) {
            Assert.assertNotEquals(a1.computeOutput(separatingWord), a2.computeOutput(separatingWord));
        }
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

    private static Pair<Integer, Integer> parseEdges(Map<String, String> attr) {
        final String label = attr.get(EdgeAttrs.LABEL);

        assert label != null;

        final String[] tokens = label.split("/");

        assert tokens.length == 2;

        return Pair.of(Integer.parseInt(tokens[0].trim()), Integer.parseInt(tokens[1].trim()));
    }

}
