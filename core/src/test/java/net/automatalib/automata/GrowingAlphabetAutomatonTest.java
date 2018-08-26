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
package net.automatalib.automata;

import java.util.Collections;

import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMoore;
import net.automatalib.automata.transout.impl.FastProbMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class GrowingAlphabetAutomatonTest {

    private static final Alphabet<Integer> ALPHABET = Alphabets.integers(1, 2);

    private static final Word<Integer> A1 = Word.epsilon();
    private static final Word<Integer> A2 = Word.fromSymbols(1);
    private static final Word<Integer> A3 = Word.fromSymbols(1, 2);
    private static final Word<Integer> A4 = Word.fromSymbols(1, 2, 2);

    private static final Word<Integer> B1 = Word.fromSymbols(3);
    private static final Word<Integer> B2 = Word.fromSymbols(1, 3);
    private static final Word<Integer> B3 = Word.fromSymbols(1, 2, 3);
    private static final Word<Integer> B4 = Word.fromSymbols(1, 2, 3, 3);

    @Test
    public void testCompactDFA() throws Exception {
        this.testGrowableOutputAutomaton(new CompactDFA<>(ALPHABET));
    }

    private <M extends MutableAutomaton<S, Integer, T, SP, TP> & GrowableAlphabetAutomaton<Integer> & Output<Integer, D>, S, D, T, SP, TP> void testGrowableOutputAutomaton(
            final M automaton) {
        this.testGrowableAutomaton(automaton);
        this.testOutput(automaton);
    }

    private <M extends MutableAutomaton<S, Integer, T, SP, TP> & GrowableAlphabetAutomaton<Integer>, S, T, SP, TP> void testGrowableAutomaton(
            final M automaton) {

        // add states
        final S s1 = automaton.addInitialState();
        final S s2 = automaton.addState();
        final S s3 = automaton.addState();

        // set and test initial transitions
        this.testInitialTransitions(automaton, s1, s2, s3);

        // add new alphabet symbol
        automaton.addAlphabetSymbol(3);

        // set and test new transitions
        this.testNewTransitions(automaton, s1, s2, s3);
    }

    private <M extends Output<Integer, D>, D> void testOutput(final M automaton) {
        // check that any output the automaton generates does not throw an error
        automaton.computeOutput(A1);
        automaton.computeOutput(A2);
        automaton.computeOutput(A3);
        automaton.computeOutput(A4);
        automaton.computeOutput(B1);
        automaton.computeOutput(B2);
        automaton.computeOutput(B3);
        automaton.computeOutput(B4);
    }

    private <M extends MutableAutomaton<S, Integer, T, SP, TP>, S, T, SP, TP> void testInitialTransitions(final M automaton,
                                                                                                          final S s1,
                                                                                                          final S s2,
                                                                                                          final S s3) {
        // set initial transitions
        automaton.setTransitions(s1, 1, Collections.singleton(automaton.createTransition(s2, null)));
        automaton.setTransitions(s2, 2, Collections.singleton(automaton.createTransition(s3, null)));
        automaton.setTransitions(s3, 1, Collections.singleton(automaton.createTransition(s3, null)));
        automaton.setTransitions(s3, 2, Collections.singleton(automaton.createTransition(s3, null)));

        // check first set of test words
        Assert.assertEquals(automaton.getStates(A1), Collections.singleton(s1));
        Assert.assertEquals(automaton.getStates(A2), Collections.singleton(s2));
        Assert.assertEquals(automaton.getStates(A3), Collections.singleton(s3));
        Assert.assertEquals(automaton.getStates(A4), Collections.singleton(s3));
    }

    private <M extends MutableAutomaton<S, Integer, T, SP, TP>, S, T, SP, TP> void testNewTransitions(final M automaton,
                                                                                                      final S s1,
                                                                                                      final S s2,
                                                                                                      final S s3) {
        // set new transitions
        automaton.setTransitions(s1, 3, Collections.singleton(automaton.createTransition(s3, null)));
        automaton.setTransitions(s2, 3, Collections.singleton(automaton.createTransition(s3, null)));
        automaton.setTransitions(s3, 3, Collections.singleton(automaton.createTransition(s3, null)));

        // check second set of test words
        Assert.assertEquals(automaton.getStates(A1), Collections.singleton(s1));
        Assert.assertEquals(automaton.getStates(B1), Collections.singleton(s3));
        Assert.assertEquals(automaton.getStates(B2), Collections.singleton(s3));
        Assert.assertEquals(automaton.getStates(B3), Collections.singleton(s3));
        Assert.assertEquals(automaton.getStates(B4), Collections.singleton(s3));
    }

    @Test
    public void testCompactNFA() throws Exception {
        this.testGrowableOutputAutomaton(new CompactNFA<>(ALPHABET));
    }

    @Test
    public void testFastDFA() throws Exception {
        this.testGrowableOutputAutomaton(new FastDFA<>(ALPHABET));
    }

    @Test
    public void testFastNFA() throws Exception {
        this.testGrowableOutputAutomaton(new FastNFA<>(ALPHABET));
    }

    @Test
    public void testCompactMealy() throws Exception {
        this.testGrowableOutputAutomaton(new CompactMealy<>(ALPHABET));
    }

    @Test
    public void testFastMealy() throws Exception {
        this.testGrowableOutputAutomaton(new FastMealy<>(ALPHABET));
    }

    @Test
    public void testFastProbMealy() throws Exception {
        this.testGrowableAutomaton(new FastProbMealy<>(ALPHABET));
    }

    @Test
    public void testFastMoore() throws Exception {
        this.testGrowableOutputAutomaton(new FastMoore<>(ALPHABET));
    }
}
