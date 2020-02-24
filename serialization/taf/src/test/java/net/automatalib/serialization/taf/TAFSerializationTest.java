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
package net.automatalib.serialization.taf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class TAFSerializationTest {

    private static final Alphabet<String> INPUT_ALPHABET = Alphabets.closedCharStringRange('0', '3');

    private static final Alphabet<String> OUTPUT_ALPHABET = Alphabets.fromArray("Hello", "World", "Hello World");

    private static final int AUTOMATON_SIZE = 20;

    @Test
    public void testDFASerialization() throws Exception {

        final CompactDFA<String> automaton = RandomAutomata.randomDFA(new Random(0), AUTOMATON_SIZE, INPUT_ALPHABET);

        weedOutTransitions(automaton);

        final TAFSerializationDFA serializer = TAFSerializationDFA.getInstance();
        final DFA<Integer, String> deserializedModel =
                writeAndReadModel(automaton, INPUT_ALPHABET, serializer, serializer);

        Assert.assertTrue(Automata.testEquivalence(automaton, deserializedModel, INPUT_ALPHABET));
    }

    @Test
    public void testMealySerialization() throws Exception {
        final CompactMealy<String, String> automaton =
                RandomAutomata.randomMealy(new Random(0), AUTOMATON_SIZE, INPUT_ALPHABET, OUTPUT_ALPHABET);

        weedOutTransitions(automaton);

        final TAFSerializationMealy serializer = TAFSerializationMealy.getInstance();

        final MealyMachine<?, String, ?, String> deserializedModel =
                writeAndReadModel(automaton, INPUT_ALPHABET, serializer, serializer);

        Assert.assertTrue(Automata.testEquivalence(automaton, deserializedModel, INPUT_ALPHABET));
    }

    private <T, A extends MutableDeterministic<Integer, String, T, ?, ?>> void weedOutTransitions(A automaton) {

        final Random random = new Random(0);

        // remove some transitions for partiality
        for (int i = 0; i < AUTOMATON_SIZE; i++) {
            final int rState = random.nextInt(AUTOMATON_SIZE);
            final String rInput = INPUT_ALPHABET.getSymbol(random.nextInt(INPUT_ALPHABET.size()));

            automaton.removeTransition(rState, rInput, automaton.getTransition(rState, rInput));
        }
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
