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
package net.automatalib.serialization.learnlibv2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.serialization.InputModelData;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class LearnLibV2SerializationTest {

    @Test
    public void outputEqualsInputTest() throws Exception {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 2);
        final Random random = new Random(0);
        final DFA<Integer, Integer> automaton = RandomAutomata.randomDFA(random, 20, alphabet);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        LearnLibV2Serialization.getInstance().writeModel(baos, automaton, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        final InputModelData<Integer, DFA<Integer, Integer>> deserializedData =
                LearnLibV2Serialization.getInstance().readModel(is);

        final DFA<Integer, Integer> deserializedAutomaton = deserializedData.model;
        final Alphabet<Integer> deserializedAlphabet = deserializedData.alphabet;

        Assert.assertEquals(alphabet, deserializedAlphabet);
        Assert.assertTrue(Automata.testEquivalence(automaton, deserializedAutomaton, alphabet));

        baos.close();
        is.close();
    }

}
