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
package net.automatalib.serialization.aut;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.simple.SimpleAutomaton;
import net.automatalib.common.util.io.UnclosableInputStream;
import net.automatalib.common.util.io.UnclosableOutputStream;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AUTSerializationTest {

    @Test
    public void quotationLabelTest() throws Exception {
        try (InputStream is = AUTSerializationTest.class.getResourceAsStream("/quotationTest.aut")) {
            final SimpleAutomaton<Integer, String> automaton = AUTParsers.parser().readModel(is).model;

            Assert.assertEquals(automaton.size(), 4);

            final String input1 = "PUT_\\6";
            final String input2 = "GET !true !7 !CONS (A, CONS (B, NIL))";
            final String input3 = "SEND !\"hello\" !\"world\"";

            final Set<Integer> s0 = automaton.getInitialStates();
            final Set<Integer> s1 = automaton.getStates(Word.fromLetter(input1));
            final Set<Integer> s2 = automaton.getStates(Word.fromLetter(input2));
            final Set<Integer> s3 = automaton.getStates(Arrays.asList(input2, input3));

            Assert.assertEquals(s0, Collections.singleton(0));
            Assert.assertEquals(s1, Collections.singleton(1));
            Assert.assertEquals(s2, Collections.singleton(2));
            Assert.assertEquals(s3, Collections.singleton(3));
        }
    }

    @Test
    public void sinkStateTest() throws Exception {
        try (InputStream is = AUTSerializationTest.class.getResourceAsStream("/sinkStateTest.aut")) {
            final SimpleAutomaton<Integer, String> automaton = AUTParsers.parser().readModel(is).model;
            Assert.assertEquals(automaton.size(), 3);

            final Set<Integer> s0 = automaton.getInitialStates();
            final Set<Integer> s1 = automaton.getStates(Word.fromLetter("input"));
            final Set<Integer> s2 = automaton.getStates(Word.fromLetter("output"));
            final Set<Integer> s3 = automaton.getStates(Arrays.asList("input", "output"));
            final Set<Integer> s4 = automaton.getStates(Arrays.asList("input", "output", "output"));

            Assert.assertEquals(s0, Collections.singleton(0));
            Assert.assertEquals(s1, Collections.singleton(1));
            Assert.assertEquals(s2, Collections.singleton(2));
            Assert.assertEquals(s3, Collections.singleton(0));
            Assert.assertEquals(s4, Collections.singleton(2));
        }
    }

    @Test
    public void nfaTest() throws Exception {
        try (InputStream is = AUTSerializationTest.class.getResourceAsStream("/nfa.aut")) {
            final SimpleAutomaton<Integer, String> automaton = AUTParsers.parser().readModel(is).model;
            Assert.assertEquals(automaton.size(), 3);

            final Set<Integer> s0 = automaton.getInitialStates();
            final Set<Integer> s1 = automaton.getStates(Word.fromLetter("a"));
            final Set<Integer> s2 = automaton.getStates(Word.fromLetter("b"));
            final Set<Integer> s3 = automaton.getStates(Arrays.asList("a", "b"));

            Assert.assertEquals(s0, Collections.singleton(0));
            Assert.assertEquals(s1, Set.of(0, 1));
            Assert.assertEquals(s2, Collections.emptySet());
            Assert.assertEquals(s3, Collections.singleton(2));
        }
    }

    @Test
    public void serializationTest() throws Exception {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 2);
        final Random random = new Random(0);
        final DFA<Integer, Integer> automaton = RandomAutomata.randomDFA(random, 20, alphabet);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new AUTWriter<Integer>().writeModel(baos, automaton, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        final SimpleAutomaton<Integer, Integer> deserialized = AUTParsers.parser(Integer::parseInt).readModel(is).model;

        equalityTest(automaton, deserialized, alphabet);
    }

    private <S, I> void equalityTest(SimpleAutomaton<S, I> src, SimpleAutomaton<S, I> target, Alphabet<I> inputs) {
        for (S s : src.getStates()) {
            for (I i : inputs) {
                Assert.assertEquals(target.getSuccessors(s, i), src.getSuccessors(s, i));
            }
        }
    }

    @Test
    public void errorTest() throws IOException {
        final InputModelDeserializer<String, SimpleAutomaton<Integer, String>> parser = AUTParsers.parser();

        try (InputStream e1 = AUTSerializationTest.class.getResourceAsStream("/error1.aut");
             InputStream e2 = AUTSerializationTest.class.getResourceAsStream("/error2.aut");
             InputStream e3 = AUTSerializationTest.class.getResourceAsStream("/error3.aut");
             InputStream e4 = AUTSerializationTest.class.getResourceAsStream("/error4.aut");
             InputStream e5 = AUTSerializationTest.class.getResourceAsStream("/error5.aut")) {
            Assert.assertThrows(FormatException.class, () -> parser.readModel(e1));
            Assert.assertThrows(FormatException.class, () -> parser.readModel(e2));
            Assert.assertThrows(FormatException.class, () -> parser.readModel(e3));
            Assert.assertThrows(FormatException.class, () -> parser.readModel(e4));
            Assert.assertThrows(FormatException.class, () -> parser.readModel(e5));
        }
    }

    @Test
    public void doNotCloseOutputStreamTest() throws IOException {
        final CompactDFA<Integer> automaton = RandomAutomata.randomDFA(new Random(0), 10, Alphabets.integers(0, 2));

        new AUTWriter<Integer>().writeModel(new UnclosableOutputStream(OutputStream.nullOutputStream()),
                                            automaton,
                                            automaton.getInputAlphabet());
    }

    @Test
    public void doNotCloseInputStreamTest() throws IOException, FormatException {
        try (InputStream is = AUTSerializationTest.class.getResourceAsStream("/quotationTest.aut")) {
            AUTParsers.parser().readModel(new UnclosableInputStream(is));
        }
    }
}
