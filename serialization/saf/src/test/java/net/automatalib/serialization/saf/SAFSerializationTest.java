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
package net.automatalib.serialization.saf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.io.UnclosableInputStream;
import net.automatalib.common.util.io.UnclosableOutputStream;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.fsa.NFAs;
import net.automatalib.util.automaton.random.RandomAutomata;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SAFSerializationTest {

    private static final Alphabet<Integer> ALPHABET = Alphabets.integers(0, 4);
    private static final Alphabet<Character> ALPHABET2 = Alphabets.characters('a', 'e');
    private static final int AUTOMATON_SIZE = 100;

    private CompactDFA<Integer> dfa;
    private CompactMealy<Integer, String> mealy;
    private CompactNFA<Integer> nfa;

    @BeforeMethod
    public void setUp() {
        final Random random = new Random(0);
        this.dfa = RandomAutomata.randomDFA(random, AUTOMATON_SIZE, ALPHABET);
        this.mealy = RandomAutomata.randomMealy(random, AUTOMATON_SIZE, ALPHABET, Arrays.asList("Hello", "World", "!"));

        // remove some transitions for partiality
        for (int i = 0; i < AUTOMATON_SIZE; i++) {
            this.dfa.removeAllTransitions(random.nextInt(AUTOMATON_SIZE), random.nextInt(ALPHABET.size()));
            this.mealy.removeAllTransitions(random.nextInt(AUTOMATON_SIZE), random.nextInt(ALPHABET.size()));
        }

        this.nfa = new CompactNFA<>(ALPHABET, AUTOMATON_SIZE);

        for (int i = 0; i < AUTOMATON_SIZE; i++) {
            this.nfa.addState(random.nextBoolean());
            this.nfa.setInitial(i, random.nextBoolean());
        }

        for (int i = 0; i < AUTOMATON_SIZE * AUTOMATON_SIZE; i++) {
            this.nfa.addTransition(random.nextInt(AUTOMATON_SIZE),
                                   random.nextInt(ALPHABET.size()),
                                   random.nextInt(AUTOMATON_SIZE));
        }
    }

    @Test
    public void testDFASerialization() throws Exception {
        final InputModelSerializer<Integer, CompactDFA<Integer>> serializer = SAFWriters.dfa();
        final InputModelDeserializer<Integer, CompactDFA<Integer>> deserializer = SAFParsers.dfa();

        final DFA<Integer, Integer> deserializedModel = writeAndReadModel(this.dfa, ALPHABET, serializer, deserializer);

        Assert.assertTrue(Automata.testEquivalence(this.dfa, deserializedModel, ALPHABET));
    }

    @Test
    public void testDFASerializationWithAlphabet() throws Exception {
        final InputModelSerializer<Character, CompactDFA<Character>> serializer = SAFWriters.dfa();
        final InputModelDeserializer<Character, CompactDFA<Character>> deserializer = SAFParsers.dfa(ALPHABET2);

        final CompactDFA<Character> translated = this.dfa.translate(ALPHABET2);
        final DFA<Integer, Character> deserializedModel =
                writeAndReadModel(translated, ALPHABET2, serializer, deserializer);

        Assert.assertTrue(Automata.testEquivalence(translated, deserializedModel, ALPHABET2));
    }

    @Test
    public void testMealySerialization() throws Exception {
        final InputModelSerializer<Integer, CompactMealy<Integer, String>> serializer =
                SAFWriters.mealy(DataOutput::writeUTF);
        final InputModelDeserializer<Integer, CompactMealy<Integer, String>> deserializer =
                SAFParsers.mealy(DataInput::readUTF);

        final MealyMachine<?, Integer, ?, String> deserializedModel =
                writeAndReadModel(this.mealy, ALPHABET, serializer, deserializer);

        Assert.assertTrue(Automata.testEquivalence(this.mealy, deserializedModel, ALPHABET));
    }

    @Test
    public void testMealySerializationWithAlphabet() throws Exception {
        final InputModelSerializer<Character, CompactMealy<Character, String>> serializer =
                SAFWriters.mealy(DataOutput::writeUTF);
        final InputModelDeserializer<Character, CompactMealy<Character, String>> deserializer =
                SAFParsers.mealy(ALPHABET2, DataInput::readUTF);

        final CompactMealy<Character, String> translated = this.mealy.translate(ALPHABET2);
        final MealyMachine<?, Character, ?, String> deserializedModel =
                writeAndReadModel(translated, ALPHABET2, serializer, deserializer);

        Assert.assertTrue(Automata.testEquivalence(translated, deserializedModel, ALPHABET2));
    }

    @Test
    public void testNFASerialization() throws Exception {
        final InputModelSerializer<Integer, CompactNFA<Integer>> serializer = SAFWriters.nfa();
        final InputModelDeserializer<Integer, CompactNFA<Integer>> deserializer = SAFParsers.nfa();

        final NFA<Integer, Integer> deserializedModel = writeAndReadModel(this.nfa, ALPHABET, serializer, deserializer);

        Assert.assertTrue(Automata.testEquivalence(NFAs.determinize(this.nfa, ALPHABET),
                                                   NFAs.determinize(deserializedModel, ALPHABET),
                                                   ALPHABET));
    }

    @Test
    public void testNFASerializationWithAlphabet() throws Exception {
        final InputModelSerializer<Character, CompactNFA<Character>> serializer = SAFWriters.nfa();
        final InputModelDeserializer<Character, CompactNFA<Character>> deserializer = SAFParsers.nfa(ALPHABET2);

        final CompactNFA<Character> translated = this.nfa.translate(ALPHABET2);
        final NFA<Integer, Character> deserializedModel =
                writeAndReadModel(translated, ALPHABET2, serializer, deserializer);

        Assert.assertTrue(Automata.testEquivalence(NFAs.determinize(translated, ALPHABET2),
                                                   NFAs.determinize(deserializedModel, ALPHABET2),
                                                   ALPHABET2));
    }

    @Test
    public void doNotCloseInputOutputStreamDFATest() throws IOException, FormatException {

        final InputModelSerializer<Integer, CompactDFA<Integer>> serializer = SAFWriters.dfa();
        final InputModelDeserializer<Integer, CompactDFA<Integer>> deserializer = SAFParsers.dfa();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.writeModel(new UnclosableOutputStream(baos), this.dfa, ALPHABET);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        deserializer.readModel(new UnclosableInputStream(is));
    }

    @Test
    public void doNotCloseInputOutputStreamMealyTest() throws IOException, FormatException {

        final InputModelSerializer<Integer, CompactMealy<Integer, String>> serializer =
                SAFWriters.mealy(DataOutput::writeUTF);
        final InputModelDeserializer<Integer, CompactMealy<Integer, String>> deserializer =
                SAFParsers.mealy(DataInput::readUTF);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.writeModel(new UnclosableOutputStream(baos), this.mealy, ALPHABET);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        deserializer.readModel(new UnclosableInputStream(is));
    }

    @Test
    public void doNotCloseInputOutputStreamNFATest() throws IOException, FormatException {

        final InputModelSerializer<Integer, CompactNFA<Integer>> serializer = SAFWriters.nfa();
        final InputModelDeserializer<Integer, CompactNFA<Integer>> deserializer = SAFParsers.nfa();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.writeModel(new UnclosableOutputStream(baos), this.nfa, ALPHABET);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        deserializer.readModel(new UnclosableInputStream(is));
    }

    private <I, IN extends UniversalAutomaton<?, I, ?, ?, ?>, OUT extends UniversalAutomaton<?, I, ?, ?, ?>> OUT writeAndReadModel(
            IN source,
            Alphabet<I> alphabet,
            InputModelSerializer<I, IN> serializer,
            InputModelDeserializer<I, OUT> deserializer) throws IOException, FormatException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        serializer.writeModel(baos, source, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return deserializer.readModel(is).model;
    }

}
