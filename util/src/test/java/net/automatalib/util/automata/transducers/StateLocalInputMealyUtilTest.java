/* Copyright (C) 2013-2020 TU Dortmund
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
package net.automatalib.util.automata.transducers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.random.RandomUtil;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class StateLocalInputMealyUtilTest {

    @Test
    public void testTransformation() {
        final Random random = new Random(42);
        final int alphabetSize = 4;
        final Alphabet<Integer> inputs = Alphabets.integers(0, alphabetSize);
        final Alphabet<Character> outputs = Alphabets.characters('a', 'd');
        final int size = 20;

        final CompactMealy<Integer, Character> automaton = RandomAutomata.randomMealy(random, size, inputs, outputs);

        final List<Word<Integer>> cover = new ArrayList<>();
        Covers.structuralCover(automaton, inputs, cover);

        // randomly remove some transitions
        for (int i = 0; i < size; i++) {
            for (int idx : RandomUtil.distinctIntegers(random.nextInt(alphabetSize), alphabetSize, random)) {
                automaton.removeAllTransitions(i, inputs.getSymbol(idx));
            }
        }

        final StateLocalInputMealyMachine<Integer, Integer, ?, OutputAndLocalInputs<Integer, Character>> transformed =
                StateLocalInputMealyUtil.partialToObservableOutput(automaton);

        for (final Word<Integer> word : cover) {

            final Word<Character> originalOutput = automaton.computeOutput(word);
            final Word<OutputAndLocalInputs<Integer, Character>> transformedOutput = transformed.computeOutput(word);

            for (int i = 0; i < word.size(); i++) {

                final Word<Integer> subWord = word.prefix(i + 1);

                if (i >= originalOutput.size()) {
                    // undefined transition in original automaton
                    Assert.assertEquals(transformedOutput.getSymbol(i), OutputAndLocalInputs.undefined());
                    Assert.assertTrue(transformed.getLocalInputs(transformed.getState(subWord)).isEmpty());
                } else {
                    Assert.assertEquals(transformedOutput.getSymbol(i).getOutput(), originalOutput.getSymbol(i));
                    Assert.assertEquals(transformedOutput.getSymbol(i).getLocalInputs(),
                                        new HashSet<>(automaton.getLocalInputs(automaton.getState(subWord))));
                    Assert.assertEquals(transformed.getLocalInputs(transformed.getState(subWord)),
                                        automaton.getLocalInputs(automaton.getState(subWord)));
                }
            }
        }
    }
}
