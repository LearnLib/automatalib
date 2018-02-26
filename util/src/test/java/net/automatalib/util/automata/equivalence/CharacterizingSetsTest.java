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
package net.automatalib.util.automata.equivalence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class CharacterizingSetsTest {

    private static final Random RANDOM = new Random(0);
    private static final int AUTOMATON_SIZE = 20;
    private static final Alphabet<Integer> INPUT_ALPHABET = Alphabets.integers(0, 5);
    private static final Alphabet<Character> OUTPUT_ALPHABET = Alphabets.characters('a', 'f');

    private static final CompactDFA<Integer> DFA = RandomAutomata.randomDFA(RANDOM, AUTOMATON_SIZE, INPUT_ALPHABET);
    private static final CompactMealy<Integer, Character> MEALY =
            RandomAutomata.randomMealy(RANDOM, AUTOMATON_SIZE, INPUT_ALPHABET, OUTPUT_ALPHABET);

    @Test
    public void characterizingDFATest() {
        final List<Word<Integer>> characterizingSet = Automata.characterizingSet(DFA, INPUT_ALPHABET);

        final Set<Set<Integer>> distinguishedStates = checkCharacterizingSet(DFA, characterizingSet);
        isPairwiseDistinguished(DFA, distinguishedStates);
    }

    @Test
    public void characterizingDFASingleTest() {
        final Integer state = DFA.getState(AUTOMATON_SIZE / 2);
        final List<Word<Integer>> characterizingSet = new ArrayList<>();
        CharacterizingSets.findCharacterizingSet(DFA, INPUT_ALPHABET, state, characterizingSet);

        final Set<Set<Integer>> distinguishedStates = checkCharacterizingSet(DFA, characterizingSet);
        isSingleDistinguished(DFA, distinguishedStates, state);
    }

    @Test
    public void characterizingMealyTest() {
        final List<Word<Integer>> characterizingSet = Automata.characterizingSet(MEALY, INPUT_ALPHABET);

        final Set<Set<Integer>> distinguishedStates = checkCharacterizingSet(MEALY, characterizingSet);
        isPairwiseDistinguished(MEALY, distinguishedStates);
    }

    @Test
    public void characterizingMealySingleTest() {
        final Integer state = MEALY.getState(AUTOMATON_SIZE / 2);
        final List<Word<Integer>> characterizingSet = new ArrayList<>();
        CharacterizingSets.findCharacterizingSet(MEALY, INPUT_ALPHABET, state, characterizingSet);

        final Set<Set<Integer>> distinguishedStates = checkCharacterizingSet(MEALY, characterizingSet);
        isSingleDistinguished(MEALY, distinguishedStates, state);
    }

    private <S, I, T, SP, TP> Set<Set<S>> checkCharacterizingSet(UniversalDeterministicAutomaton<S, I, T, SP, TP> automaton,
                                                                 Collection<Word<I>> characterizingSet) {

        final Set<Set<S>> distinguishedStates = new HashSet<>();

        for (final Word<I> w : characterizingSet) {
            final Set<S> currentlyDistinguishedStates = new HashSet<>();

            for (final S s : automaton.getStates()) {
                if (isDistinguished(automaton, w, currentlyDistinguishedStates, s)) {
                    currentlyDistinguishedStates.add(s);
                }
            }

            distinguishedStates.add(currentlyDistinguishedStates);
        }

        return distinguishedStates;
    }

    private <S, I, T, SP, TP> boolean isDistinguished(UniversalDeterministicAutomaton<S, I, T, SP, TP> automaton,
                                                      Word<I> trace,
                                                      Set<S> distinguishedStates,
                                                      S stateToCheck) {
        if (distinguishedStates.isEmpty()) {
            return true;
        }

        for (final S s : distinguishedStates) {

            S baseIter = stateToCheck;
            S checkIter = s;

            for (final I i : trace) {
                T baseTrans = automaton.getTransition(baseIter, i);
                T checkTrans = automaton.getTransition(checkIter, i);

                // update values for next iteration
                baseIter = automaton.getSuccessor(baseTrans);
                checkIter = automaton.getSuccessor(checkTrans);

                final TP baseTP = automaton.getTransitionProperty(baseTrans);
                final TP checkTP = automaton.getTransitionProperty(checkTrans);

                final SP baseSP = automaton.getStateProperty(baseIter);
                final SP checkSP = automaton.getStateProperty(checkIter);

                if ((baseTP != checkTP) || (baseTP != null && !baseTP.equals(checkTP)) || (baseSP != checkSP) ||
                    (baseSP != null && !baseSP.equals(checkSP))) {
                    return true;
                }
            }
        }

        return false;
    }

    private <S, I, T, SP, TP> void isPairwiseDistinguished(UniversalDeterministicAutomaton<S, I, T, SP, TP> automaton,
                                                           Set<Set<S>> distinguishedStates) {

        for (final S s : automaton.getStates()) {
            for (final S t : automaton.getStates()) {

                boolean distinguishedByOneWord = false;

                for (final Set<S> d : distinguishedStates) {
                    if (d.contains(s) && d.contains(t)) {
                        distinguishedByOneWord = true;
                        break;
                    }
                }

                Assert.assertTrue(distinguishedByOneWord, "States " + s + ',' + t + " are not distinguished");
            }
        }
    }

    private <S, I, T, SP, TP> void isSingleDistinguished(UniversalDeterministicAutomaton<S, I, T, SP, TP> automaton,
                                                         Set<Set<S>> distinguishedStates,
                                                         S state) {
        for (final S s : automaton.getStates()) {
            if (s.equals(state)) {
                continue;
            }

            boolean distinguishedByOneWord = false;

            for (final Set<S> d : distinguishedStates) {
                if (d.contains(state)) {
                    distinguishedByOneWord = true;
                    break;
                }
            }

            Assert.assertTrue(distinguishedByOneWord, "States " + s + ',' + state + " are not distinguished");
        }
    }
}
