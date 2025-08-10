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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.conformance.KWayStateCoverTestsIterator.CombinationMethod;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class KWayStateCoverTestsIteratorTest {

    private static final Alphabet<Character> ALPHABET = Alphabets.characters('a', 'c');

    @DataProvider(name = "methods")
    public static Object[][] getMethods() {
        return new Object[][] {{CombinationMethod.COMBINATIONS}, {CombinationMethod.PERMUTATIONS}};
    }

    @Test
    public void testEmptyAutomaton() {
        final CompactDFA<Character> dfa = new CompactDFA<>(ALPHABET);
        final List<Word<Character>> tests = IteratorUtil.list(new KWayStateCoverTestsIterator<>(dfa, ALPHABET));

        Assert.assertTrue(tests.isEmpty());
    }

    @Test(dataProvider = "methods")
    public void testSingleStateAutomaton(CombinationMethod method) {
        final CompactDFA<Character> dfa = new CompactDFA<>(ALPHABET);

        final int initial = dfa.addIntInitialState();
        for (int i = 0; i < ALPHABET.size(); i++) {
            dfa.setTransition(initial, i, initial);
        }

        final int length = KWayStateCoverTestsIterator.DEFAULT_R_WALK_LEN;
        final List<Word<Character>> tests = IteratorUtil.list(new KWayStateCoverTestsIterator<>(dfa,
                                                                                                ALPHABET,
                                                                                                new Random(42),
                                                                                                length,
                                                                                                KWayStateCoverTestsIterator.DEFAULT_K,
                                                                                                method));

        // check that the first 'length' queries are the randomly generated ones.
        Assert.assertTrue(tests.size() >= length);
        for (Word<Character> t : tests.subList(0, length)) {
            Assert.assertEquals(t.size(), length);
        }
    }

    @Test(dataProvider = "methods")
    public void testNoInitialStateAutomaton(CombinationMethod method) {
        final CompactDFA<Character> dfa = RandomAutomata.randomDFA(new Random(42), 10, ALPHABET);

        dfa.setInitialState(null);
        final List<Word<Character>> tests = IteratorUtil.list(new KWayStateCoverTestsIterator<>(dfa,
                                                                                                ALPHABET,
                                                                                                new Random(42),
                                                                                                KWayStateCoverTestsIterator.DEFAULT_R_WALK_LEN,
                                                                                                KWayStateCoverTestsIterator.DEFAULT_K,
                                                                                                method));

        Assert.assertTrue(tests.isEmpty());
    }

    @Test(dataProvider = "methods")
    public void testPartialAutomaton(CombinationMethod method) {
        // @formatter:off
        final CompactDFA<Character> dfa = AutomatonBuilders.newDFA(ALPHABET)
                                                           .from("s0").on('a').to("s1")
                                                           .from("s1").on('b').to("s2")
                                                           .withInitial("s0")
                                                           .withAccepting("s2", "s3")
                                                           .create();
        // @formatter:on

        final List<Word<Character>> tests = IteratorUtil.list(new KWayStateCoverTestsIterator<>(dfa,
                                                                                                ALPHABET,
                                                                                                new Random(42),
                                                                                                KWayStateCoverTestsIterator.DEFAULT_R_WALK_LEN,
                                                                                                KWayStateCoverTestsIterator.DEFAULT_K,
                                                                                                method));
        verifyEachStateVisited(dfa, tests, Set.of(0, 1, 2));
    }

    @Test(dataProvider = "methods")
    public void testRandomAutomaton(CombinationMethod method) {
        final CompactMealy<Character, Integer> mealy =
                RandomAutomata.randomMealy(new Random(42), 10, ALPHABET, Alphabets.integers(0, 2));

        final List<Word<Character>> tests = IteratorUtil.list(new KWayStateCoverTestsIterator<>(mealy,
                                                                                                ALPHABET,
                                                                                                new Random(42),
                                                                                                KWayStateCoverTestsIterator.DEFAULT_R_WALK_LEN,
                                                                                                KWayStateCoverTestsIterator.DEFAULT_K,
                                                                                                method));

        verifyEachStateVisited(mealy, tests);
    }

    @Test(dataProvider = "methods")
    public void testKeylockAutomaton(CombinationMethod method) {
        final CompactDFA<Character> mealy = generateKeylockAutomaton(ALPHABET);

        final List<Word<Character>> tests = IteratorUtil.list(new KWayStateCoverTestsIterator<>(mealy,
                                                                                                ALPHABET,
                                                                                                new Random(42),
                                                                                                KWayStateCoverTestsIterator.DEFAULT_R_WALK_LEN,
                                                                                                KWayStateCoverTestsIterator.DEFAULT_K,
                                                                                                method));

        verifyEachStateVisited(mealy, tests);
    }

    static <S, I> void verifyEachStateVisited(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                              List<Word<I>> tests) {
        verifyEachStateVisited(automaton, tests, new HashSet<>(automaton.getStates()));
    }

    static <S, I> void verifyEachStateVisited(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                              List<Word<I>> tests,
                                              Set<S> expected) {
        final Set<S> visited = new HashSet<>(HashUtil.capacity(automaton.size()));

        final S init = automaton.getInitialState();
        Assert.assertNotNull(init);

        visited.add(init);

        for (Word<I> t : tests) {
            S iter = init;
            for (I i : t) {
                S succ = automaton.getSuccessor(iter, i);
                if (succ == null) {
                    break;
                }
                visited.add(succ);
                iter = succ;
            }
        }

        Assert.assertEquals(visited, expected);
    }

    static <I> CompactDFA<I> generateKeylockAutomaton(Alphabet<I> alphabet) {

        final CompactDFA<I> result = new CompactDFA<>(alphabet);

        int iter = result.addIntInitialState();

        for (int i = 0; i < 10 - 1; i++) {
            for (int j = 1; j < alphabet.size(); j++) {
                result.setTransition(iter, j, iter);
            }
            int next = result.addIntState();
            result.setTransition(iter, 0, next);
            iter = next;
        }

        result.setAccepting(iter, true);
        for (int i = 0; i < alphabet.size(); i++) {
            result.setTransition(iter, i, iter);
        }

        return result;
    }
}
