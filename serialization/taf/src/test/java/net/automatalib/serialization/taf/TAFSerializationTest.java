/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.Automaton;
import net.automatalib.automaton.FiniteAlphabetAutomaton;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.io.UnclosableInputStream;
import net.automatalib.common.util.io.UnclosableOutputStream;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.serialization.taf.parser.TAFParsers;
import net.automatalib.serialization.taf.writer.TAFWriters;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TAFSerializationTest {

    private static final Alphabet<String> INPUT_ALPHABET = Alphabets.closedCharStringRange('0', '3');
    private static final Alphabet<String> OUTPUT_ALPHABET = Alphabets.fromArray("Hello", "World", "Hello World");
    private static final int AUTOMATON_SIZE = 20;

    private CompactDFA<String> dfa;
    private CompactMealy<String, String> mealy;

    @BeforeMethod
    public void setUp() {
        final Random random = new Random(0);

        this.dfa = RandomAutomata.randomDFA(random, AUTOMATON_SIZE, INPUT_ALPHABET);
        weedOutTransitions(this.dfa);

        this.mealy = RandomAutomata.randomMealy(new Random(0), AUTOMATON_SIZE, INPUT_ALPHABET, OUTPUT_ALPHABET);
        weedOutTransitions(this.mealy);
    }

    @Test
    public void testDFASerialization() throws IOException, FormatException {
        final InputModelDeserializer<String, CompactDFA<String>> deserializer = TAFParsers.dfa();
        final InputModelSerializer<String, CompactDFA<String>> serializer = TAFWriters.dfa();
        final DFA<Integer, String> deserializedModel =
                writeAndReadModel(this.dfa, INPUT_ALPHABET, serializer, deserializer);

        Assert.assertTrue(Automata.testEquivalence(this.dfa, deserializedModel, INPUT_ALPHABET));
    }

    @Test
    public void testMealySerialization() throws IOException, FormatException {
        final InputModelDeserializer<String, CompactMealy<String, String>> deserializer = TAFParsers.mealy();
        final InputModelSerializer<String, CompactMealy<String, String>> serializer = TAFWriters.mealy();

        final MealyMachine<?, String, ?, String> deserializedModel =
                writeAndReadModel(this.mealy, INPUT_ALPHABET, serializer, deserializer);

        Assert.assertTrue(Automata.testEquivalence(this.mealy, deserializedModel, INPUT_ALPHABET));
    }

    @Test
    public void testAnySerializationDFA() throws IOException, FormatException {
        final InputModelDeserializer<String, FiniteAlphabetAutomaton<?, String, ?>> deserializer = TAFParsers.any();
        final InputModelSerializer<String, FiniteAlphabetAutomaton<Integer, String, Integer>> serializer =
                TAFWriters.any();
        final FiniteAlphabetAutomaton<?, String, ?> deserializedModel =
                writeAndReadModel(this.dfa, INPUT_ALPHABET, serializer, deserializer);

        Assert.assertNotNull(deserializedModel);
    }

    @Test
    public void testAnySerializationMealy() throws IOException, FormatException {
        final InputModelDeserializer<String, FiniteAlphabetAutomaton<?, String, ?>> deserializer = TAFParsers.any();
        final InputModelSerializer<String, FiniteAlphabetAutomaton<Integer, String, CompactTransition<String>>>
                serializer = TAFWriters.any();
        final FiniteAlphabetAutomaton<?, String, ?> deserializedModel =
                writeAndReadModel(this.mealy, INPUT_ALPHABET, serializer, deserializer);

        Assert.assertNotNull(deserializedModel);
    }

    @Test
    public void testParseDFA() throws IOException, FormatException {

        final InputModelDeserializer<String, CompactDFA<String>> parser = TAFParsers.dfa();
        final Alphabet<String> alphabet;
        final CompactDFA<String> dfa;

        try (InputStream is = TAFSerializationTest.class.getResourceAsStream("/dfa.taf")) {
            final InputModelData<String, CompactDFA<String>> model = parser.readModel(is);
            alphabet = model.alphabet;
            dfa = model.model;
        }

        Assert.assertNotNull(alphabet);
        Assert.assertNotNull(dfa);

        Assert.assertEquals(alphabet, Alphabets.closedCharStringRange('a', 'd'));
        Assert.assertTrue(dfa.accepts(Word.fromSymbols("a", "b", "c")));
        Assert.assertTrue(dfa.accepts(Word.fromSymbols("a", "b", "c", "a", "b", "c")));
        Assert.assertFalse(dfa.accepts(Word.fromSymbols("a", "a", "b")));
    }

    @Test
    public void testParseMealy() throws IOException, FormatException {

        final InputModelDeserializer<String, CompactMealy<String, String>> parser = TAFParsers.mealy();
        final Alphabet<String> alphabet;
        final CompactMealy<String, String> mealy;

        try (InputStream is = TAFSerializationTest.class.getResourceAsStream("/mealy.taf")) {
            final InputModelData<String, CompactMealy<String, String>> model = parser.readModel(is);
            alphabet = model.alphabet;
            mealy = model.model;
        }

        Assert.assertNotNull(alphabet);
        Assert.assertNotNull(mealy);

        Assert.assertEquals(alphabet, Alphabets.fromArray("Hello", "?"));
        Assert.assertEquals(mealy.computeOutput(Word.fromSymbols("Hello", "?")), Word.fromSymbols("World", "!"));
        Assert.assertEquals(mealy.computeOutput(Word.fromSymbols("?", "?")), Word.fromSymbols("err", "err"));
        Assert.assertNull(mealy.getState(Word.fromSymbols("Hello", "Hello")));
    }

    @Test
    public void testParseError() throws IOException {
        try (InputStream is = TAFSerializationTest.class.getResourceAsStream("/error.taf")) {
            Assert.assertThrows(FormatException.class, () -> TAFParsers.dfa().readModel(is));
            Assert.assertThrows(FormatException.class, () -> TAFParsers.mealy().readModel(is));
            Assert.assertThrows(FormatException.class, () -> TAFParsers.any().readModel(is));
        }
    }

    @Test
    public void doNotCloseInputOutputStreamDFATest() throws IOException, FormatException {
        final InputModelDeserializer<String, CompactDFA<String>> deserializer = TAFParsers.dfa();
        final InputModelSerializer<String, CompactDFA<String>> serializer = TAFWriters.dfa();
        writeAndReadUnclosableModel(this.dfa, INPUT_ALPHABET, serializer, deserializer);
    }

    @Test
    public void doNotCloseInputOutputStreamMealyTest() throws IOException, FormatException {
        final InputModelDeserializer<String, CompactMealy<String, String>> deserializer = TAFParsers.mealy();
        final InputModelSerializer<String, CompactMealy<String, String>> serializer = TAFWriters.mealy();
        writeAndReadUnclosableModel(this.mealy, INPUT_ALPHABET, serializer, deserializer);
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

    private <I, IN extends Automaton<?, I, ?>, OUT extends Automaton<?, I, ?>> OUT writeAndReadModel(IN source,
                                                                                                     Alphabet<I> alphabet,
                                                                                                     InputModelSerializer<I, IN> serializer,
                                                                                                     InputModelDeserializer<I, OUT> deserializer)
            throws IOException, FormatException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.writeModel(baos, source, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return deserializer.readModel(is).model;
    }

    private <I, IN extends Automaton<?, I, ?>, OUT extends Automaton<?, I, ?>> OUT writeAndReadUnclosableModel(IN source,
                                                                                                               Alphabet<I> alphabet,
                                                                                                               InputModelSerializer<I, IN> serializer,
                                                                                                               InputModelDeserializer<I, OUT> deserializer)
            throws IOException, FormatException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.writeModel(new UnclosableOutputStream(baos), source, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return deserializer.readModel(new UnclosableInputStream(is)).model;
    }

}
