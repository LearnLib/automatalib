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
package net.automatalib.serialization.saf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.fsa.NFAs;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class SAFSerializationTest {

    private static final Alphabet<Integer> ALPHABET = Alphabets.integers(0, 4);

    private static final int AUTOMATON_SIZE = 20;

    @Test
    public void testDFASerialization() throws Exception {
        final Random random = new Random(0);
        final CompactDFA<Integer> automaton = RandomAutomata.randomDFA(random, AUTOMATON_SIZE, ALPHABET);

        // remove some transitions for partiality
        for (int i = 0; i < AUTOMATON_SIZE; i++) {
            automaton.removeTransition(random.nextInt(AUTOMATON_SIZE),
                                       random.nextInt(ALPHABET.size()),
                                       random.nextInt(AUTOMATON_SIZE));
        }

        final SAFSerializationDFA serializer = SAFSerializationDFA.getInstance();

        final DFA<Integer, Integer> deserializedModel = writeAndReadModel(automaton, ALPHABET, serializer, serializer);

        Assert.assertTrue(Automata.testEquivalence(automaton, deserializedModel, ALPHABET));
    }

    @Test
    public void testNFASerialization() throws Exception {
        final Random random = new Random(0);

        final CompactNFA<Integer> automaton = new CompactNFA<>(ALPHABET, AUTOMATON_SIZE);

        for (int i = 0; i < AUTOMATON_SIZE; i++) {
            automaton.addState(random.nextBoolean());
            automaton.setInitial(i, random.nextBoolean());
        }

        for (int i = 0; i < AUTOMATON_SIZE * AUTOMATON_SIZE; i++) {
            automaton.addTransition(random.nextInt(AUTOMATON_SIZE),
                                    random.nextInt(ALPHABET.size()),
                                    random.nextInt(AUTOMATON_SIZE));
        }

        final SAFSerializationNFA serializer = SAFSerializationNFA.getInstance();

        final NFA<Integer, Integer> deserializedModel = writeAndReadModel(automaton, ALPHABET, serializer, serializer);

        Assert.assertTrue(Automata.testEquivalence(NFAs.determinize(automaton, ALPHABET),
                                                   NFAs.determinize(deserializedModel, ALPHABET),
                                                   ALPHABET));
    }

    private <I, IN extends UniversalAutomaton<?, I, ?, ?, ?>, OUT extends UniversalAutomaton<?, I, ?, ?, ?>> OUT writeAndReadModel(
            IN source,
            Alphabet<I> alphabet,
            InputModelSerializer<I, IN> serializer,
            InputModelDeserializer<I, OUT> deserializer) throws IOException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        serializer.writeModel(baos, source, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return deserializer.readModel(is).model;
    }

}
