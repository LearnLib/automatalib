/* Copyright (C) 2013-2019 TU Dortmund
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

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.automata.transducers.impl.FastMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.partitionrefinement.PaigeTarjanInitializers.AutomatonInitialPartitioning;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class PaigeTarjanTest {

    @Test
    public void testCompleteCompactDFA() {
        testCompleteDFA(new CompactDFA.Creator<>());
    }

    @Test
    public void testCompleteFastDFA() {
        testCompleteDFA(FastDFA<Character>::new);
    }

    @Test
    public void testPartialCompactDFA() {
        testPartialDFA(new CompactDFA.Creator<>());
    }

    @Test
    public void testPartialFastDFA() {
        testPartialDFA(FastDFA<Character>::new);
    }

    @Test
    public void testCompleteCompactMealy() {
        testCompleteMealy(new CompactMealy.Creator<>());
    }

    @Test
    public void testCompleteFastMealy() {
        testCompleteMealy(FastMealy<Character, Character>::new);
    }

    @Test
    public void testPartialCompactMealy() {
        testPartialMealy(new CompactMealy.Creator<>());
    }

    @Test
    public void testPartialFastMealy() {
        testPartialMealy(FastMealy<Character, Character>::new);
    }

    @Test
    public void testSharedSinkSignature() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'b');
        final CompactMealy<Character, Character> mealy = new CompactMealy<>(alphabet);

        final Integer q1 = mealy.addInitialState();
        final Integer q2 = mealy.addState();
        final Integer q3 = mealy.addState();

        mealy.setTransition(q1, (Character) 'a', q2, (Character) 'x');
        mealy.setTransition(q1, (Character) 'b', q3, (Character) 'y');

        final Function<? super Integer, Object> initialClassification =
                AutomatonInitialPartitioning.BY_FULL_SIGNATURE.initialClassifier(mealy, alphabet);

        final PaigeTarjan ptSep = new PaigeTarjan();
        final StateIDs<Integer> idsSep =
                PaigeTarjanInitializers.initDeterministic(ptSep, mealy, alphabet, initialClassification, null);

        ptSep.initWorklist(false);
        ptSep.computeCoarsestStablePartition();

        final CompactMealy<Character, Character> resultSep = PaigeTarjanExtractors.toDeterministic(ptSep,
                                                                                                   CompactMealy::new,
                                                                                                   alphabet,
                                                                                                   mealy,
                                                                                                   idsSep,
                                                                                                   null,
                                                                                                   mealy::getTransitionProperty,
                                                                                                   false);

        Assert.assertEquals(resultSep.size(), 2);
        Assert.assertTrue(Automata.testEquivalence(mealy, resultSep, alphabet));

        final PaigeTarjan ptShared = new PaigeTarjan();
        final StateIDs<Integer> idsShared = PaigeTarjanInitializers.initDeterministic(ptShared,
                                                                                      mealy,
                                                                                      alphabet,
                                                                                      initialClassification,
                                                                                      initialClassification.apply(2));

        ptShared.initWorklist(false);
        ptShared.computeCoarsestStablePartition();

        final CompactMealy<Character, Character> resultShared = PaigeTarjanExtractors.toDeterministic(ptShared,
                                                                                                      CompactMealy::new,
                                                                                                      alphabet,
                                                                                                      mealy,
                                                                                                      idsShared,
                                                                                                      null,
                                                                                                      mealy::getTransitionProperty,
                                                                                                      false);

        Assert.assertEquals(resultShared.size(), 2);
        Assert.assertTrue(Automata.testEquivalence(mealy, resultShared, alphabet));
    }

    private <S> void testCompleteDFA(AutomatonCreator<? extends MutableDFA<S, Character>, Character> creator) {

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final MutableDFA<S, Character> dfa = creator.createAutomaton(alphabet);

        // states q1, q2 are equivalent
        final S q1 = dfa.addInitialState(true);
        final S q2 = dfa.addState(true);
        final S q3 = dfa.addState(false);

        dfa.setTransition(q1, 'a', q2);
        dfa.setTransition(q1, 'b', q3);
        dfa.setTransition(q1, 'c', q1);
        dfa.setTransition(q2, 'a', q1);
        dfa.setTransition(q2, 'b', q3);
        dfa.setTransition(q2, 'c', q2);
        dfa.setTransition(q3, 'a', q3);
        dfa.setTransition(q3, 'b', q3);
        dfa.setTransition(q3, 'c', q3);

        testDFAInternal(dfa, alphabet, creator, 2, 2);
    }

    private <S> void testPartialDFA(AutomatonCreator<? extends MutableDFA<S, Character>, Character> creator) {

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final MutableDFA<S, Character> dfa = creator.createAutomaton(alphabet);

        // states q1, q2 are equivalent, q3 is unreachable
        final S q1 = dfa.addInitialState(true);
        final S q2 = dfa.addState(true);
        final S q3 = dfa.addState(true);

        dfa.setTransition(q1, 'a', q1);
        dfa.setTransition(q1, 'c', q2);
        dfa.setTransition(q2, 'a', q2);
        dfa.setTransition(q2, 'c', q1);
        dfa.setTransition(q3, 'b', q3);

        // there is currently no support for unpruned, partial automata. Unreachable states are always thrown away
        testDFAInternal(dfa, alphabet, creator, 1, 1);
    }

    private static <S> void testDFAInternal(MutableDFA<S, Character> dfa,
                                            Alphabet<Character> alphabet,
                                            AutomatonCreator<? extends MutableDFA<S, Character>, Character> creator,
                                            int expectedPrunedStates,
                                            int expectedUnprunedStates) {

        final PaigeTarjan pt = new PaigeTarjan();

        final StateIDs<S> stateIds =
                PaigeTarjanInitializers.initDeterministic(pt, dfa, alphabet, dfa::isAccepting, false);

        pt.initWorklist(false);
        pt.computeCoarsestStablePartition();

        final MutableDFA<S, Character> prunedResult = PaigeTarjanExtractors.toDeterministic(pt,
                                                                                            creator,
                                                                                            alphabet,
                                                                                            dfa,
                                                                                            stateIds,
                                                                                            dfa::getStateProperty,
                                                                                            null,
                                                                                            true);
        checkDFA(dfa, prunedResult, alphabet, expectedPrunedStates);

        final MutableDFA<S, Character> unprunedResult = PaigeTarjanExtractors.toDeterministic(pt,
                                                                                              creator,
                                                                                              alphabet,
                                                                                              dfa,
                                                                                              stateIds,
                                                                                              dfa::getStateProperty,
                                                                                              null,
                                                                                              false);
        checkDFA(dfa, unprunedResult, alphabet, expectedUnprunedStates);
    }

    private static <I> void checkDFA(DFA<?, I> original,
                                     DFA<?, I> minimized,
                                     Alphabet<I> alphabet,
                                     int expectedStates) {
        Assert.assertEquals(minimized.size(), expectedStates);
        Assert.assertTrue(Automata.testEquivalence(original, minimized, alphabet));
    }

    private <S, T> void testCompleteMealy(AutomatonCreator<? extends MutableMealyMachine<S, Character, T, Character>, Character> creator) {

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final MutableMealyMachine<S, Character, T, Character> mealy = creator.createAutomaton(alphabet);

        // states q1, q2 are equivalent, q4 is unreachable
        final S q1 = mealy.addInitialState();
        final S q2 = mealy.addState();
        final S q3 = mealy.addState();
        final S q4 = mealy.addState();

        mealy.setTransition(q1, 'a', q2, 'x');
        mealy.setTransition(q1, 'b', q3, 'y');
        mealy.setTransition(q1, 'c', q1, 'x');
        mealy.setTransition(q2, 'a', q1, 'x');
        mealy.setTransition(q2, 'b', q3, 'y');
        mealy.setTransition(q2, 'c', q2, 'x');
        mealy.setTransition(q3, 'a', q3, 'x');
        mealy.setTransition(q3, 'b', q3, 'x');
        mealy.setTransition(q3, 'c', q3, 'x');
        mealy.setTransition(q4, 'a', q4, 'z');
        mealy.setTransition(q4, 'b', q4, 'z');

        // there is currently no support for unpruned, partial automata. Unreachable states are always thrown away
        testMealyInternal(mealy, alphabet, creator, 2, 2);
    }

    private <S, T> void testPartialMealy(AutomatonCreator<? extends MutableMealyMachine<S, Character, T, Character>, Character> creator) {

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'd');

        // build binary tree (partial due to 4 input symbols), whose leaves end in a sink and add an unreachable state.
        // s3 and s5 are equivalent, s8 unreachable
        // @formatter:off
        final MutableMealyMachine<S, Character, T, Character> mealy =
                AutomatonBuilders.forMealy(creator.createAutomaton(alphabet))
                                 .withInitial(0)
                                 .from(0).on('b').withOutput('4').to(1)
                                 .from(0).on('a').withOutput('4').to(2)
                                 .from(1).on('a').withOutput('3').to(3)
                                 .from(1).on('d').withOutput('3').to(4)
                                 .from(2).on('b').withOutput('3').to(5)
                                 .from(2).on('d').withOutput('3').to(6)
                                 .from(3).on('c', 'd').withOutput('0').to(7)
                                 .from(4).on('a', 'c').withOutput('0').to(7)
                                 .from(5).on('c', 'd').withOutput('0').to(7)
                                 .from(6).on('b', 'c').withOutput('0').to(7)
                                 .from(8).on('a', 'b', 'c', 'd').withOutput('8').loop()
                                 .create();
        // @formatter:on

        // there is currently no support for unpruned, partial automata. Unreachable states are always thrown away
        testMealyInternal(mealy, alphabet, creator, 7, 7);
    }

    private static <S, T> void testMealyInternal(MutableMealyMachine<S, Character, T, Character> mealy,
                                                 Alphabet<Character> alphabet,
                                                 AutomatonCreator<? extends MutableMealyMachine<S, Character, T, Character>, Character> creator,
                                                 int expectedPrunedStates,
                                                 int expectedUnprunedStates) {

        final Function<? super S, Object> initialClassification =
                AutomatonInitialPartitioning.BY_TRANSITION_PROPERTIES.initialClassifier(mealy, alphabet);

        final PaigeTarjan pt = new PaigeTarjan();

        final StateIDs<S> stateIds =
                PaigeTarjanInitializers.initDeterministic(pt, mealy, alphabet, initialClassification, 'z');

        pt.initWorklist(false);
        pt.computeCoarsestStablePartition();

        final MutableMealyMachine<S, Character, ?, Character> prunedResult = PaigeTarjanExtractors.toDeterministic(pt,
                                                                                                                   creator,
                                                                                                                   alphabet,
                                                                                                                   mealy,
                                                                                                                   stateIds,
                                                                                                                   null,
                                                                                                                   mealy::getTransitionProperty,
                                                                                                                   true);
        checkMealy(mealy, prunedResult, alphabet, expectedPrunedStates);

        final MutableMealyMachine<S, Character, ?, Character> unprunedResult = PaigeTarjanExtractors.toDeterministic(pt,
                                                                                                                     creator,
                                                                                                                     alphabet,
                                                                                                                     mealy,
                                                                                                                     stateIds,
                                                                                                                     null,
                                                                                                                     mealy::getTransitionProperty,
                                                                                                                     false);
        checkMealy(mealy, unprunedResult, alphabet, expectedUnprunedStates);
    }

    private static <I> void checkMealy(MealyMachine<?, I, ?, ?> original,
                                       MealyMachine<?, I, ?, ?> minimized,
                                       Alphabet<I> alphabet,
                                       int expectedStates) {
        Assert.assertEquals(minimized.size(), expectedStates);
        Assert.assertTrue(Automata.testEquivalence(original, minimized, alphabet));
    }
}