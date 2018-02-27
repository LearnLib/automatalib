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
package net.automatalib.util.partitionrefinement;

import java.util.function.Function;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.partitionrefinement.PaigeTarjanInitializers.AutomatonInitialPartitioning;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class PaigeTarjanTest {

    @Test
    public void testCompleteDFA() {

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final CompactDFA<Character> dfa = new CompactDFA<>(alphabet);

        // states q1, q2 are equivalent
        final Integer q1 = dfa.addInitialState(true);
        final Integer q2 = dfa.addState(true);
        final Integer q3 = dfa.addState(false);

        dfa.setTransition(q1, (Character) 'a', q2);
        dfa.setTransition(q1, (Character) 'b', q3);
        dfa.setTransition(q1, (Character) 'c', q1);
        dfa.setTransition(q2, (Character) 'a', q1);
        dfa.setTransition(q2, (Character) 'b', q3);
        dfa.setTransition(q2, (Character) 'c', q2);
        dfa.setTransition(q3, (Character) 'a', q3);
        dfa.setTransition(q3, (Character) 'b', q3);
        dfa.setTransition(q3, (Character) 'c', q3);

        testDFAInternal(dfa, 2, 2);
    }

    @Test
    public void testPartialDFA() {

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final CompactDFA<Character> dfa = new CompactDFA<>(alphabet);

        // states q1, q2 are equivalent, q3 is unreachable
        final Integer q1 = dfa.addInitialState(true);
        final Integer q2 = dfa.addState(true);
        final Integer q3 = dfa.addState(true);

        dfa.setTransition(q1, (Character) 'a', q1);
        dfa.setTransition(q1, (Character) 'c', q2);
        dfa.setTransition(q2, (Character) 'a', q2);
        dfa.setTransition(q2, (Character) 'c', q1);
        dfa.setTransition(q3, (Character) 'b', q3);

        testDFAInternal(dfa, 1, 2);
    }

    private static void testDFAInternal(CompactDFA<Character> dfa,
                                        int expectedPrunedStates,
                                        int expectedUnprunedStates) {

        final Alphabet<Character> alphabet = dfa.getInputAlphabet();
        final PaigeTarjan pt = new PaigeTarjan();

        PaigeTarjanInitializers.initDeterministic(pt, dfa, alphabet, dfa::isAccepting, false);

        pt.initWorklist(false);
        pt.computeCoarsestStablePartition();

        final CompactDFA<Character> prunedResult = PaigeTarjanExtractors.toDeterministic(pt,
                                                                                         new CompactDFA.Creator<>(),
                                                                                         alphabet,
                                                                                         dfa,
                                                                                         dfa.stateIDs(),
                                                                                         dfa::getStateProperty,
                                                                                         null,
                                                                                         true);
        checkDFA(prunedResult, expectedPrunedStates);

        final CompactDFA<Character> unprunedResult = PaigeTarjanExtractors.toDeterministic(pt,
                                                                                           new CompactDFA.Creator<>(),
                                                                                           alphabet,
                                                                                           dfa,
                                                                                           dfa.stateIDs(),
                                                                                           dfa::getStateProperty,
                                                                                           null,
                                                                                           false);
        checkDFA(unprunedResult, expectedUnprunedStates);
    }

    private static void checkDFA(CompactDFA<Character> dfa, int expectedStates) {
        Assert.assertEquals(dfa.size(), expectedStates);
        Assert.assertEquals(dfa.getInitialState(), dfa.getState(Word.fromLetter('a')));
        Assert.assertEquals(dfa.getInitialState(), dfa.getState(Word.fromCharSequence("acaaca")));
        Assert.assertTrue(dfa.accepts(Word.fromCharSequence("aca")));
        Assert.assertFalse(dfa.accepts(Word.fromCharSequence("ab")));
    }

    @Test
    public void testCompleteMealy() {

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final CompactMealy<Character, Character> dfa = new CompactMealy<>(alphabet);

        // states q1, q2 are equivalent
        final Integer q1 = dfa.addInitialState();
        final Integer q2 = dfa.addState();
        final Integer q3 = dfa.addState();

        dfa.setTransition(q1, (Character) 'a', q2, (Character) 'x');
        dfa.setTransition(q1, (Character) 'b', q3, (Character) 'y');
        dfa.setTransition(q1, (Character) 'c', q1, (Character) 'x');
        dfa.setTransition(q2, (Character) 'a', q1, (Character) 'x');
        dfa.setTransition(q2, (Character) 'b', q3, (Character) 'y');
        dfa.setTransition(q2, (Character) 'c', q2, (Character) 'x');
        dfa.setTransition(q3, (Character) 'a', q3, (Character) 'x');
        dfa.setTransition(q3, (Character) 'b', q3, (Character) 'x');
        dfa.setTransition(q3, (Character) 'c', q3, (Character) 'x');

        testMealyInternal(dfa, 2, 2);
    }

    @Test
    public void testPartialMealy() {

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final CompactMealy<Character, Character> mealy = new CompactMealy<>(alphabet);

        // states q1, q2 are equivalent, q3 is unreachable
        final Integer q1 = mealy.addInitialState();
        final Integer q2 = mealy.addState();
        final Integer q3 = mealy.addState();

        mealy.setTransition(q1, (Character) 'a', q1, (Character) 'x');
        mealy.setTransition(q1, (Character) 'c', q2, (Character) 'x');
        mealy.setTransition(q2, (Character) 'a', q2, (Character) 'x');
        mealy.setTransition(q2, (Character) 'c', q1, (Character) 'x');
        mealy.setTransition(q3, (Character) 'b', q3, (Character) 'x');

        testMealyInternal(mealy, 1, 2);
    }

    private static void testMealyInternal(CompactMealy<Character, Character> mealy,
                                          int expectedPrunedStates,
                                          int expectedUnprunedStates) {

        final Alphabet<Character> alphabet = mealy.getInputAlphabet();
        final Function<? super Integer, ?> initialClassification =
                (s) -> AutomatonInitialPartitioning.BY_TRANSITION_PROPERTIES.initialClassifier(mealy).apply(s);

        final PaigeTarjan pt = new PaigeTarjan();
        PaigeTarjanInitializers.initDeterministic(pt, mealy, alphabet, initialClassification, 'z');

        pt.initWorklist(false);
        pt.computeCoarsestStablePartition();

        final CompactMealy<Character, Character> prunedResult = PaigeTarjanExtractors.toDeterministic(pt,
                                                                                                      new CompactMealy.Creator<>(),
                                                                                                      alphabet,
                                                                                                      mealy,
                                                                                                      mealy.stateIDs(),
                                                                                                      null,
                                                                                                      mealy::getTransitionProperty,
                                                                                                      true);
        checkMealy(prunedResult, expectedPrunedStates);

        final CompactMealy<Character, Character> unprunedResult = PaigeTarjanExtractors.toDeterministic(pt,
                                                                                                        new CompactMealy.Creator<>(),
                                                                                                        alphabet,
                                                                                                        mealy,
                                                                                                        mealy.stateIDs(),
                                                                                                        null,
                                                                                                        mealy::getTransitionProperty,
                                                                                                        false);
        checkMealy(unprunedResult, expectedUnprunedStates);
    }

    private static void checkMealy(CompactMealy<Character, Character> mealy, int expectedStates) {
        Assert.assertEquals(mealy.size(), expectedStates);
        Assert.assertEquals(mealy.getInitialState(), mealy.getState(Word.fromLetter('a')));
        Assert.assertEquals(mealy.getInitialState(), mealy.getState(Word.fromCharSequence("acaaca")));
        Assert.assertEquals(mealy.computeOutput(Word.fromCharSequence("aca")), Word.fromCharSequence("xxx"));
    }
}
