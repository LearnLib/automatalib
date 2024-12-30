/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.util.automaton.fsa;

import java.util.Random;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.FiniteStateAcceptor;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.FastNFA;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.util.automaton.random.TabakovVardiRandomAutomata;
import net.automatalib.util.ts.acceptor.AcceptanceCombiner;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NFAsTest {

    private final Alphabet<Integer> testAlphabet;
    private final CompactNFA<Integer> testNfa1, testNfa2;

    public NFAsTest() {
        final Random r = new Random(42);

        this.testAlphabet = Alphabets.integers(0, 1);
        this.testNfa1 = TabakovVardiRandomAutomata.generateNFA(r, 10, 25, 5, testAlphabet);
        this.testNfa2 = TabakovVardiRandomAutomata.generateNFA(r, 10, 25, 5, testAlphabet);
    }

    @Test
    public void testAnd() {
        NFA<?, Integer> expected = dfaCombine(testNfa1, testNfa2, testAlphabet, AcceptanceCombiner.AND);
        NFA<?, Integer> actual = NFAs.and(testNfa1, testNfa2, testAlphabet);

        assertEquivalence(actual, expected, testAlphabet);
    }

    @Test
    public void testOr() {
        NFA<?, Integer> expected = dfaCombine(testNfa1, testNfa2, testAlphabet, AcceptanceCombiner.OR);
        NFA<?, Integer> actual = NFAs.or(testNfa1, testNfa2, testAlphabet);

        Assert.assertEquals(actual.size(), testNfa1.size() + testNfa2.size());
        assertEquivalence(actual, expected, testAlphabet);
    }

    @Test
    public void testReverse() {
        final CompactDFA<Integer> nfa = DFAsTest.forVector(true, true, false, false);
        final Alphabet<Integer> alphabet = nfa.getInputAlphabet();

        CompactNFA<Integer> rNFA = NFAs.reverse(nfa, alphabet);
        Assert.assertEquals(rNFA.size(), nfa.size());

        for (int i = 0; i < rNFA.size(); i++) {
            Assert.assertEquals(rNFA.isAccepting(i), i == 0);
            int iMinusOneModSize = (rNFA.size() + i - 1) % rNFA.size();
            Assert.assertEquals(rNFA.getTransitions(i), Set.of(iMinusOneModSize));
        }

        Assert.assertEquals(rNFA.getInitialStates(), Set.of(0, 1));

        // double-reverse == no reverse
        assertEquivalence(nfa, NFAs.reverse(rNFA, alphabet), alphabet);
    }

    @Test
    public void testTrim() {
        Alphabet<Integer> alphabet = Alphabets.singleton(0);
        CompactNFA<Integer> nfa = new CompactNFA<>(alphabet);

        int q0 = nfa.addInitialState(false);

        // with no accepting states, if trimmed this will have no states
        Assert.assertEquals(NFAs.trim(nfa, alphabet).size(), 0);

        // accepting state is not accessible
        int q1 = nfa.addState(true);
        Assert.assertEquals(NFAs.trim(nfa, alphabet).size(), 0);

        // accessible and co-accessible
        nfa.addTransition(q0, 0, q1);
        Assert.assertEquals(NFAs.trim(nfa, alphabet).size(), 2);

        // dead-end is not co-accessible
        int q2 = nfa.addState(false);
        nfa.addTransition(q0, 0, q2);
        Assert.assertEquals(NFAs.trim(nfa, alphabet).size(), 2);
    }

    @Test
    public void testTrimReversal() {
        // test trim equivalence of testNfa1 and its reverse
        assertEquivalence(testNfa1, NFAs.trim(testNfa1, testAlphabet), testAlphabet);
        CompactNFA<Integer> reverseNFA = NFAs.reverse(testNfa1, testAlphabet);
        assertEquivalence(reverseNFA, NFAs.trim(reverseNFA, testAlphabet), testAlphabet);
    }

    @Test
    public void testTrimAccessibility() {
        Alphabet<Integer> alphabet = Alphabets.singleton(0);
        CompactNFA<Integer> nfa = new CompactNFA<>(alphabet);

        // q0 -> q1, q1 -> q2: accessible but not co-accessible
        int q0 = nfa.addInitialState(false);
        int q1 = nfa.addState(false);
        int q2 = nfa.addState(true);
        nfa.addTransition(q0, 0, q1);
        nfa.addTransition(q0, 0, q2);

        Assert.assertEquals(NFAs.accessibleStates(nfa, alphabet), Set.of(0, 1, 2));
        Assert.assertEquals(NFAs.coaccessibleStates(nfa, alphabet), Set.of(0, 2));
        Assert.assertEquals(NFAs.trim(nfa, alphabet).size(), 2);
    }

    @Test
    public void testDeterminizeDFA() {
        determinizeDFA(new CompactDFA.Creator<>());
        determinizeDFA(FastDFA::new);
    }

    @Test
    public void testDeterminizeNFA() {
        determinizeNFA(new CompactNFA.Creator<>());
        determinizeNFA(FastNFA::new);
    }

    /*
     * Check that determinization is idempotent.
     */
    private <S, A extends MutableDFA<S, Integer> & InputAlphabetHolder<Integer>> void determinizeDFA(AutomatonCreator<A, Integer> creator) {
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

        A dfa = creator.createAutomaton(alphabet);

        RandomAutomata.randomDeterministic(new Random(42),
                                           10,
                                           alphabet,
                                           FiniteStateAcceptor.STATE_PROPERTIES,
                                           FiniteStateAcceptor.TRANSITION_PROPERTIES,
                                           dfa);

        CompactDFA<Integer> det = NFAs.determinize(dfa, true, false);

        Assert.assertEquals(det.size(), dfa.size());
        Assert.assertTrue(Automata.testEquivalence(dfa, det, alphabet));
    }

    private <S, A extends MutableNFA<S, Integer> & InputAlphabetHolder<Integer>> void determinizeNFA(AutomatonCreator<A, Integer> creator) {
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

        A nfa = creator.createAutomaton(alphabet);

        S q0 = nfa.addInitialState(false);
        S q1 = nfa.addState(true);

        nfa.addTransition(q0, 0, q0);
        nfa.addTransition(q0, 1, q0);
        nfa.addTransition(q0, 1, q1);

        Assert.assertTrue(nfa.accepts(Word.fromSymbols(0, 1, 0, 1)));
        Assert.assertFalse(nfa.accepts(Word.fromSymbols(0, 1, 0, 1, 0)));

        CompactDFA<Integer> dfa = NFAs.determinize(nfa);

        Assert.assertEquals(dfa.size(), 2);
        Assert.assertTrue(dfa.accepts(Word.fromSymbols(0, 1, 0, 1)));
        Assert.assertFalse(dfa.accepts(Word.fromSymbols(0, 1, 0, 1, 0)));
    }

    private <I> DFA<?, I> dfaCombine(NFA<?, I> nfa1, NFA<?, I> nfa2, Alphabet<I> inputs, AcceptanceCombiner acc) {
        return DFAs.combine(NFAs.determinize(nfa1, inputs), NFAs.determinize(nfa2, inputs), inputs, acc);
    }

    private <I> void assertEquivalence(NFA<?, I> nfa1, NFA<?, I> nfa2, Alphabet<I> inputs) {
        Assert.assertTrue(Automata.testEquivalence(NFAs.determinize(nfa1, inputs),
                                                   NFAs.determinize(nfa2, inputs),
                                                   inputs));
    }
}
