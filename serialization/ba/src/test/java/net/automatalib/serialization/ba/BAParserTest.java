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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.io.UnclosableInputStream;
import net.automatalib.common.util.io.UnclosableOutputStream;
import net.automatalib.util.automaton.random.RandomAutomata;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BAParserTest {

    @Test
    public void smallBATest() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/test1.ba")) {
            final CompactNFA<String> automaton = new BAParser<>().readModel(is).model;

            Assert.assertEquals(automaton.size(), 3);
            Assert.assertEquals(automaton.getInputAlphabet().size(), 5);
            Assert.assertEquals(automaton.getInitialStates(), Collections.singleton(0));
            Assert.assertFalse(automaton.isAccepting(0));
            Assert.assertTrue(automaton.isAccepting(1));

            Assert.assertEquals(automaton.getTransitions(0), List.of(1, 2, 2, 1, 1));
            Assert.assertEquals(automaton.getTransitions(1), List.of(1, 2, 2, 1, 1));
            Assert.assertEquals(automaton.getTransitions(2), List.of(2, 2, 1));
        }
    }

    @Test
    public void smallBARenumberTest() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/test1_renumber.ba")) {
            final CompactNFA<String> automaton = new BAParser<>().readModel(is).model;

            Assert.assertEquals(automaton.size(), 3);
            Assert.assertEquals(automaton.getInputAlphabet().size(), 5);
            Assert.assertEquals(automaton.getInitialStates(), Collections.singleton(0));
            Assert.assertFalse(automaton.isAccepting(0));
            Assert.assertTrue(automaton.isAccepting(1));

            Assert.assertEquals(automaton.getTransitions(0), List.of(1, 1, 1, 2, 2));
            Assert.assertEquals(automaton.getTransitions(1), List.of(1, 1, 1, 2, 2));
            Assert.assertEquals(automaton.getTransitions(2), List.of(1, 2, 2));
        }
    }

    @Test
    public void smallBA2Test() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/test2.ba")) {
            final CompactNFA<String> automaton = new BAParser<>().readModel(is).model;
            Assert.assertEquals(automaton.size(), 2);
            Assert.assertEquals(automaton.getInputAlphabet().size(), 2);
            Assert.assertEquals(automaton.getInitialStates(), Collections.singleton(0));
            Assert.assertFalse(automaton.isAccepting(0));
            Assert.assertTrue(automaton.isAccepting(1));

            Assert.assertEquals(automaton.getTransitions(0), List.of(0, 1));
            Assert.assertEquals(automaton.getTransitions(1), List.of(1, 1));
        }
    }

    @Test
    public void allAcceptTest() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/test_all_accept.ba")) {
            final CompactNFA<String> automaton = new BAParser<>().readModel(is).model;
            Assert.assertEquals(automaton.size(), 2);
            Assert.assertEquals(automaton.getInputAlphabet().size(), 2);
            Assert.assertEquals(automaton.getInitialStates(), Collections.singleton(0));
            Assert.assertTrue(automaton.isAccepting(0));
            Assert.assertTrue(automaton.isAccepting(1));

            Assert.assertEquals(automaton.getTransitions(0), List.of(0, 1));
            Assert.assertEquals(automaton.getTransitions(1), List.of(1, 1));
        }
    }

    @Test
    public void serializationTest() throws Exception {
        final Alphabet<String> alphabet = Alphabets.fromList(List.of("0", "1"));
        final Random random = new Random(0);
        final CompactDFA<String> automaton = RandomAutomata.randomDFA(random, 20, alphabet);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new BAWriter<String>().writeModel(baos, automaton, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        final CompactNFA<String> deserialized = new BAParser<>().readModel(is).model;

        equalityTest(automaton, deserialized, alphabet);
    }

    private <I> void equalityTest(CompactDFA<I> src, CompactNFA<I> target, Alphabet<I> inputs) {
        Assert.assertEquals(target.getInitialStates(), src.getInitialStates());
        for (Integer s : src.getStates()) {
            Assert.assertEquals(target.isAccepting(s), src.isAccepting(s));
        }
        for (Integer s : src.getStates()) {
            for (I i : inputs) {
                Assert.assertEquals(target.getSuccessors(s, i), src.getSuccessors(s, i));
            }
        }
    }

    @Test
    public void doNotCloseOutputStreamTest() throws IOException {
        final CompactDFA<Integer> automaton = RandomAutomata.randomDFA(new Random(0), 10, Alphabets.integers(0, 2));

        new BAWriter<Integer>().writeModel(new UnclosableOutputStream(OutputStream.nullOutputStream()),
                                            automaton,
                                            automaton.getInputAlphabet());
    }

    @Test
    public void doNotCloseInputStreamTest() throws IOException {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/test1.ba")) {
            new BAParser<>().readModel(new UnclosableInputStream(is));
        }
    }
}
