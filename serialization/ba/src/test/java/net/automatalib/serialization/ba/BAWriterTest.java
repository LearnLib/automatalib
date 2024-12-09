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
package net.automatalib.serialization.ba;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.io.UnclosableOutputStream;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.random.RandomAutomata;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BAWriterTest {

    @Test
    public void emptyTest() throws Exception {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        final CompactDFA<Integer> dfa = new CompactDFA<>(alphabet);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new BAWriter<Integer>().writeModel(baos, dfa, alphabet);

        Assert.assertEquals(baos.size(), 0);
    }

    @Test
    public void serializationTest() throws Exception {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        final Random random = new Random(0);
        final CompactDFA<Integer> automaton = RandomAutomata.randomDFA(random, 20, alphabet);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new BAWriter<Integer>().writeModel(baos, automaton, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        final CompactDFA<Integer> deserialized = BAParsers.dfa(Integer::parseInt).readModel(is).model;

        Assert.assertTrue(Automata.testEquivalence(automaton, deserialized, alphabet));
    }

    @Test
    public void errorTest() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        final CompactNFA<Integer> automaton = new CompactNFA<>(alphabet);

        final int s0 = automaton.addInitialState(true);
        final int s1 = automaton.addInitialState(true);

        automaton.addTransition(s0, 0, s0);
        automaton.addTransition(s0, 0, s1);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Assert.assertThrows(IllegalArgumentException.class, () -> BAWriter.writeAutomaton(automaton, alphabet, baos));
    }

    @Test
    public void doNotCloseOutputStreamTest() throws IOException {
        final CompactDFA<Integer> automaton = RandomAutomata.randomDFA(new Random(0), 10, Alphabets.integers(0, 2));

        new BAWriter<Integer>().writeModel(new UnclosableOutputStream(OutputStream.nullOutputStream()),
                                           automaton,
                                           automaton.getInputAlphabet());
    }

}
