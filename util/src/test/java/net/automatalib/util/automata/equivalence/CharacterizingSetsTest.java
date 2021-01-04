/* Copyright (C) 2013-2021 TU Dortmund
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
import java.util.List;
import java.util.Objects;
import java.util.Random;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.builders.AutomatonBuilders;
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

        checkCharacterizingSet(DFA, characterizingSet);
    }

    @Test
    public void characterizingDFASingleTest() {
        final Integer state = DFA.getState(AUTOMATON_SIZE / 2);
        final List<Word<Integer>> characterizingSet = new ArrayList<>();
        CharacterizingSets.findCharacterizingSet(DFA, INPUT_ALPHABET, state, characterizingSet);

        checkCharacterizingSet(DFA, state, characterizingSet);
    }

    @Test
    public void characterizingMealyTest() {
        final List<Word<Integer>> characterizingSet = Automata.characterizingSet(MEALY, INPUT_ALPHABET);

        checkCharacterizingSet(MEALY, characterizingSet);
    }

    @Test
    public void characterizingMealySingleTest() {
        final Integer state = MEALY.getState(AUTOMATON_SIZE / 2);
        final List<Word<Integer>> characterizingSet = new ArrayList<>();
        CharacterizingSets.findCharacterizingSet(MEALY, INPUT_ALPHABET, state, characterizingSet);

        checkCharacterizingSet(MEALY, state, characterizingSet);
    }

    /*
     * See https://github.com/LearnLib/automatalib/issues/36
     */
    @Test
    public void issue36Test() {
        final Alphabet<String> inputs = Alphabets.fromArray("a", "b");

        // @formatter:off
        final CompactMealy<String, Object> machine = AutomatonBuilders.newMealy(inputs)
                .withInitial("0")
                .from("0")
                    .on("a").withOutput("a").to("1")
                    .on("b").withOutput("b").to("0")
                .from("1")
                    .on("a").withOutput("b").to("2")
                    .on("b").withOutput("b").to("3")
                .from("2")
                    .on("a").withOutput("b").to("1")
                    .on("b").withOutput("b").to("0")
                .from("3")
                    .on("a").withOutput("a").to("0")
                    .on("b").withOutput("b").to("0")
                .create();
        // @formatter:on

        final List<Word<String>> characterizingSet = Automata.characterizingSet(machine, inputs);
        checkCharacterizingSet(machine, characterizingSet);
    }

    private <S, I> void checkCharacterizingSet(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                               Collection<Word<I>> characterizingSet) {
        for (final S s : automaton) {
            checkCharacterizingSet(automaton, s, characterizingSet);
        }
    }

    private <S, I, T, SP, TP> void checkCharacterizingSet(UniversalDeterministicAutomaton<S, I, T, SP, TP> automaton,
                                                          S state,
                                                          Collection<Word<I>> characterizingSet) {

        outer:
        for (final S s : automaton) {
            if (!Objects.equals(s, state)) {
                for (final Word<I> trace : characterizingSet) {

                    S baseIter = state;
                    S checkIter = s;

                    final List<Object> baseSignature = new ArrayList<>(trace.size() + 1);
                    final List<Object> checkSignature = new ArrayList<>(trace.size() + 1);

                    baseSignature.add(automaton.getStateProperty(baseIter));
                    checkSignature.add(automaton.getStateProperty(checkIter));

                    for (final I i : trace) {
                        final T baseTrans = automaton.getTransition(baseIter, i);
                        final T checkTrans = automaton.getTransition(checkIter, i);

                        baseSignature.add(automaton.getTransitionProperty(baseTrans));
                        checkSignature.add(automaton.getTransitionProperty(checkTrans));

                        // update values for next iteration
                        baseIter = automaton.getSuccessor(baseTrans);
                        checkIter = automaton.getSuccessor(checkTrans);

                        baseSignature.add(automaton.getStateProperty(baseIter));
                        checkSignature.add(automaton.getStateProperty(checkIter));
                    }

                    Assert.assertEquals(baseSignature.size(), checkSignature.size());

                    if (!baseSignature.equals(checkSignature)) {
                        continue outer;
                    }
                }

                Assert.fail("State '" + state + "' cannot be distinguished from state '" + s + '\'');
            }
        }
    }
}
