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
package net.automatalib.util.automaton.conformance;

import java.util.List;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.collection.IteratorUtil;
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
                                                                                                     method,
                                                                                                     metric));
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
                                                                                                     method,
                                                                                                     metric));
        Assert.assertTrue(tests.isEmpty());
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
                                                                                                     method,
                                                                                                     metric));
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
                                                                                                     method,
                                                                                                     metric));
        KWayStateCoverTestsIteratorTest.verifyEachStateVisited(mealy, tests);
    }
}
