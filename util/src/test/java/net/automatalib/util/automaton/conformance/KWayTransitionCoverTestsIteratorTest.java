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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class KWayTransitionCoverTestsIteratorTest {

    @Test
    public void testDefault() {

        Random random = new Random(42);
        Alphabet<Integer> alphabet = Alphabets.integers(0, 9);
        CompactDFA<Integer> dfa = RandomAutomata.randomDFA(random, 10, alphabet);

        KWayTransitionCoverTestsIterator<?, Integer, ?, ?> iter =
                new KWayTransitionCoverTestsIterator<>(dfa, alphabet, random);

        List<Word<Integer>> tests = IteratorUtil.list(iter);

        assertTransitionCoverage(dfa, alphabet, tests);

    }

    private <S, I, T> void assertTransitionCoverage(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton,
                                                    Collection<I> inputs,
                                                    Collection<Word<I>> tests) {

        MutableMapping<S, Set<T>> mapping = automaton.createStaticStateMapping();

        for (S s : automaton) {
            mapping.put(s, new HashSet<>());
        }

        for (Word<I> test : tests) {
            S state = automaton.getState(test.prefix(-1));
            T t = automaton.getTransition(state, test.lastSymbol());
            mapping.get(state).add(t);
        }

        for (S s : automaton) {
            Set<T> transitions = mapping.get(s);
            Assert.assertNotNull(transitions);
            Assert.assertEquals(transitions.size(), inputs.size(), Objects.toString(s));
        }

    }
}
