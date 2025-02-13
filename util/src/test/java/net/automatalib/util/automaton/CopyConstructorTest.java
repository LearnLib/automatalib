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
package net.automatalib.util.automaton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.automaton.transducer.impl.CompactSST;
import net.automatalib.util.automaton.copy.AutomatonCopyMethod;
import net.automatalib.util.automaton.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automaton.fsa.NFAs;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A test for various copy constructors of automaton types. Note that this test better suites to the "core" module,
 * however, the "util" module makes this test a lot simpler due to its available convenience methods for generating
 * random automata and testing equivalence.
 */
public class CopyConstructorTest {

    private static final Random RANDOM = new Random(42);
    private static final int SIZE = 10;
    private static final Alphabet<Character> INPUT_ALPHABET = Alphabets.characters('a', 'c');
    private static final Alphabet<Character> OUTPUT_ALPHABET = Alphabets.characters('1', '3');
    private static final List<Word<Character>> SST_PROPERTIES =
            Arrays.asList(Word.fromString("Hello"), Word.fromString("World"));

    @Test
    public void testDFA() {
        final CompactDFA<Character> dfa = RandomAutomata.randomDFA(RANDOM, SIZE, INPUT_ALPHABET);
        testCopyConstructor(dfa, INPUT_ALPHABET, CompactDFA::new);
    }

    @Test
    public void testMealy() {
        final CompactMealy<Character, Character> mealy =
                RandomAutomata.randomMealy(RANDOM, SIZE, INPUT_ALPHABET, OUTPUT_ALPHABET);
        testCopyConstructor(mealy, INPUT_ALPHABET, CompactMealy::new);
    }

    @Test
    public void testMoore() {
        final CompactMoore<Character, Character> moore =
                RandomAutomata.randomMoore(RANDOM, SIZE, INPUT_ALPHABET, OUTPUT_ALPHABET);
        testCopyConstructor(moore, INPUT_ALPHABET, CompactMoore::new);
    }

    @Test
    public void testNFA() {
        final CompactDFA<Character> dfa = RandomAutomata.randomDFA(RANDOM, SIZE, INPUT_ALPHABET);
        final CompactNFA<Character> nfa = new CompactNFA<>(INPUT_ALPHABET);

        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.BFS, dfa, INPUT_ALPHABET, nfa);

        testNonDetCopyConstructor(nfa, INPUT_ALPHABET, CompactNFA::new);
    }

    @Test
    public void testSST() {
        final CompactSST<Character, Character> sst = new CompactSST<>(INPUT_ALPHABET);

        RandomAutomata.randomDeterministic(RANDOM, SIZE, INPUT_ALPHABET, Collections.emptyList(), SST_PROPERTIES, sst);
        testCopyConstructor(sst, INPUT_ALPHABET, CompactSST::new);
    }

    private <I, A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>> void testCopyConstructor(A automaton,
                                                                                                   Alphabet<I> alphabet,
                                                                                                   Function<A, A> copyConstructor) {
        final A copy = copyConstructor.apply(automaton);
        Assert.assertTrue(Automata.testEquivalence(automaton, copy, alphabet));
    }

    private <I, A extends NFA<?, I>> void testNonDetCopyConstructor(A automaton,
                                                                    Alphabet<I> alphabet,
                                                                    Function<A, A> copyConstructor) {

        final A copy = copyConstructor.apply(automaton);
        final CompactDFA<I> detA = NFAs.determinize(automaton, alphabet);
        final CompactDFA<I> detCopy = NFAs.determinize(copy, alphabet);
        Assert.assertTrue(Automata.testEquivalence(detA, detCopy, alphabet));
    }

}
