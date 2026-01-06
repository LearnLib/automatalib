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
package net.automatalib.util.automaton.conformance;

import java.util.List;
import java.util.Random;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.GenerationMethod;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.OptimizationMetric;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class KWayTransitionCoverTestsIteratorTest {

    private static final Alphabet<Character> ALPHABET = Alphabets.characters('a', 'c');
    private static final int AUTOMATON_SIZE = 10;

    @DataProvider(name = "config")
    public static Object[][] getConfig() {
        final Object[][] result = new Object[GenerationMethod.values().length * OptimizationMetric.values().length][];
        int idx = 0;

        for (GenerationMethod method : GenerationMethod.values()) {
            for (OptimizationMetric metric : OptimizationMetric.values()) {
                result[idx++] = new Object[] {method, metric};
            }
        }

        return result;
    }

    @Test
    public void testSizeOfSetDifference() {
        final Set<Character> s1 = Set.of('A', 'B', 'C');
        final Set<Character> s2 = Set.of('A', 'B');
        final Set<Character> s3 = Set.of('A');
        final Set<Character> s4 = Set.of('X', 'Y', 'Z');
        final Set<Character> s5 = Set.of('A', 'X');

        Assert.assertEquals(KWayTransitionCoverTestsIterator.sizeOfSetDifference(s1, s2), 1);
        Assert.assertEquals(KWayTransitionCoverTestsIterator.sizeOfSetDifference(s2, s1), 0);

        Assert.assertEquals(KWayTransitionCoverTestsIterator.sizeOfSetDifference(s1, s3), 2);
        Assert.assertEquals(KWayTransitionCoverTestsIterator.sizeOfSetDifference(s3, s1), 0);

        Assert.assertEquals(KWayTransitionCoverTestsIterator.sizeOfSetDifference(s1, s4), 3);
        Assert.assertEquals(KWayTransitionCoverTestsIterator.sizeOfSetDifference(s4, s1), 3);

        Assert.assertEquals(KWayTransitionCoverTestsIterator.sizeOfSetDifference(s1, s5), 2);
        Assert.assertEquals(KWayTransitionCoverTestsIterator.sizeOfSetDifference(s5, s1), 1);

        Assert.assertEquals(KWayTransitionCoverTestsIterator.sizeOfSetDifference(s4, s5), 2);
        Assert.assertEquals(KWayTransitionCoverTestsIterator.sizeOfSetDifference(s5, s4), 1);
    }

    @Test
    public void testEmptyAutomaton() {
        final CompactDFA<Character> dfa = new CompactDFA<>(ALPHABET);
        final List<Word<Character>> tests = IteratorUtil.list(new KWayTransitionCoverTestsIterator<>(dfa, ALPHABET));

        Assert.assertTrue(tests.isEmpty());
    }

    @Test(dataProvider = "config")
    public void testSingleStateAutomaton(GenerationMethod method, OptimizationMetric metric) {
        final CompactDFA<Character> dfa = new CompactDFA<>(ALPHABET);

        final int initial = dfa.addIntInitialState();
        for (int i = 0; i < ALPHABET.size(); i++) {
            dfa.setTransition(initial, i, initial);
        }

        final List<Word<Character>> tests = IteratorUtil.list(new KWayTransitionCoverTestsIterator<>(dfa,
                                                                                                     ALPHABET,
                                                                                                     new Random(42),
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_R_WALK_LEN,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_NUM_GEN_PATHS,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_MAX_PATH_LENGTH,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_MAX_NUM_STEPS,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_K,
                                                                                                     metric,
                                                                                                     method));
        KWayStateCoverTestsIteratorTest.verifyEachStateVisited(dfa, tests);
    }

    @Test(dataProvider = "config")
    public void testNoInitialStateAutomaton(GenerationMethod method, OptimizationMetric metric) {
        final CompactDFA<Character> dfa = RandomAutomata.randomDFA(new Random(42), AUTOMATON_SIZE, ALPHABET);

        dfa.setInitialState(null);
        final List<Word<Character>> tests = IteratorUtil.list(new KWayTransitionCoverTestsIterator<>(dfa,
                                                                                                     ALPHABET,
                                                                                                     new Random(42),
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_R_WALK_LEN,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_NUM_GEN_PATHS,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_MAX_PATH_LENGTH,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_MAX_NUM_STEPS,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_K,
                                                                                                     metric,
                                                                                                     method));
        Assert.assertTrue(tests.isEmpty());
    }

    @Test(dataProvider = "config")
    public void testPartialAutomaton(GenerationMethod method, OptimizationMetric metric) {
        // @formatter:off
        final CompactDFA<Character> dfa = AutomatonBuilders.newDFA(ALPHABET)
                                                           .from("s0").on('a').to("s1")
                                                           .from("s1").on('b').to("s2")
                                                           .withInitial("s0")
                                                           .withAccepting("s2", "s3")
                                                           .create();
        // @formatter:on

        final List<Word<Character>> tests = IteratorUtil.list(new KWayTransitionCoverTestsIterator<>(dfa,
                                                                                                     ALPHABET,
                                                                                                     new Random(42),
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_R_WALK_LEN,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_NUM_GEN_PATHS,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_MAX_PATH_LENGTH,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_MAX_NUM_STEPS,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_K,
                                                                                                     metric,
                                                                                                     method));
        KWayStateCoverTestsIteratorTest.verifyEachStateVisited(dfa, tests, Set.of(0, 1, 2));
    }

    @Test(dataProvider = "config")
    public void testRandomAutomaton(GenerationMethod method, OptimizationMetric metric) {
        final CompactMealy<Character, Integer> mealy =
                RandomAutomata.randomMealy(new Random(42), AUTOMATON_SIZE, ALPHABET, Alphabets.integers(0, 2));

        final List<Word<Character>> tests = IteratorUtil.list(new KWayTransitionCoverTestsIterator<>(mealy,
                                                                                                     ALPHABET,
                                                                                                     new Random(42),
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_R_WALK_LEN,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_NUM_GEN_PATHS,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_MAX_PATH_LENGTH,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_MAX_NUM_STEPS,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_K,
                                                                                                     metric,
                                                                                                     method));
        KWayStateCoverTestsIteratorTest.verifyEachStateVisited(mealy, tests);
    }

    @Test(dataProvider = "config")
    public void testKeylockAutomaton(GenerationMethod method, OptimizationMetric metric) {
        final CompactDFA<Character> mealy = KWayStateCoverTestsIteratorTest.generateKeylockAutomaton(ALPHABET);

        final List<Word<Character>> tests = IteratorUtil.list(new KWayTransitionCoverTestsIterator<>(mealy,
                                                                                                     ALPHABET,
                                                                                                     new Random(42),
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_R_WALK_LEN,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_NUM_GEN_PATHS,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_MAX_PATH_LENGTH,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_MAX_NUM_STEPS,
                                                                                                     KWayTransitionCoverTestsIterator.DEFAULT_K,
                                                                                                     metric,
                                                                                                     method));
        KWayStateCoverTestsIteratorTest.verifyEachStateVisited(mealy, tests);
    }

    @Test
    public void testLimits() {
        final Random random = new Random(42);
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 6);
        final CompactDFA<Integer> dfa = RandomAutomata.randomDFA(random, 10, alphabet, false);
        final int randomWalkLen = 50;

        final List<Word<Integer>> rWalkTests = IteratorUtil.list(new KWayTransitionCoverTestsIterator<>(dfa,
                                                                                                        alphabet,
                                                                                                        random,
                                                                                                        randomWalkLen,
                                                                                                        KWayTransitionCoverTestsIterator.DEFAULT_NUM_GEN_PATHS,
                                                                                                        KWayTransitionCoverTestsIterator.DEFAULT_MAX_PATH_LENGTH,
                                                                                                        KWayTransitionCoverTestsIterator.DEFAULT_MAX_NUM_STEPS,
                                                                                                        KWayTransitionCoverTestsIterator.DEFAULT_K,
                                                                                                        OptimizationMetric.QUERIES,
                                                                                                        GenerationMethod.PREFIX));

        for (Word<Integer> t : rWalkTests) {
            Assert.assertTrue(t.size() > randomWalkLen, t.toString());
        }

        final int maxPathLength = 10;
        final List<Word<Integer>> maxPathTests = IteratorUtil.list(new KWayTransitionCoverTestsIterator<>(dfa,
                                                                                                          alphabet,
                                                                                                          random,
                                                                                                          maxPathLength *
                                                                                                          maxPathLength,
                                                                                                          KWayTransitionCoverTestsIterator.DEFAULT_NUM_GEN_PATHS,
                                                                                                          maxPathLength,
                                                                                                          KWayTransitionCoverTestsIterator.DEFAULT_MAX_NUM_STEPS,
                                                                                                          KWayTransitionCoverTestsIterator.DEFAULT_K,
                                                                                                          OptimizationMetric.QUERIES,
                                                                                                          GenerationMethod.RANDOM));

        for (Word<Integer> t : maxPathTests) {
            final int length = t.size();
            /*
             * In case the random generation can no longer find any meaningful test sequences it re-generates candidates
             * via the prefix method which uses the randomWalkLength. We make this value reasonably large to check that
             * test sequences were only generated from these two possibilities.
             */
            Assert.assertTrue(length <= maxPathLength || length > maxPathLength * maxPathLength, t.toString());
        }

        final int maxNumSteps = 10;
        final List<Word<Integer>> maxNumStepsTests = IteratorUtil.list(new KWayTransitionCoverTestsIterator<>(dfa,
                                                                                                              alphabet,
                                                                                                              random,
                                                                                                              KWayTransitionCoverTestsIterator.DEFAULT_R_WALK_LEN,
                                                                                                              KWayTransitionCoverTestsIterator.DEFAULT_NUM_GEN_PATHS,
                                                                                                              maxPathLength,
                                                                                                              maxNumSteps,
                                                                                                              KWayTransitionCoverTestsIterator.DEFAULT_K,
                                                                                                              OptimizationMetric.STEPS,
                                                                                                              GenerationMethod.RANDOM));

        final int numSteps = maxNumStepsTests.stream().mapToInt(Word::size).sum();
        Assert.assertTrue(numSteps >= maxNumSteps, maxNumStepsTests.toString());
        Assert.assertTrue(numSteps <= maxNumSteps + maxPathLength, maxNumStepsTests.toString());
    }
}
