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
package net.automatalib.serialization.ba;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.common.util.io.UnclosableInputStream;
import net.automatalib.exception.FormatException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BAParserTest {

    @Test
    public void smallBATest() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/test1.ba")) {
            final CompactNFA<String> automaton = BAParsers.nfa().readModel(is).model;

            Assert.assertEquals(automaton.size(), 3);
            Assert.assertEquals(automaton.getInputAlphabet().size(), 5);
            Assert.assertEquals(automaton.getInitialStates(), Collections.singleton(0));
            Assert.assertFalse(automaton.isAccepting(0));
            Assert.assertTrue(automaton.isAccepting(1));

            Assert.assertEquals(automaton.getTransitions(0), List.of(1, 2, 1, 2, 1));
            Assert.assertEquals(automaton.getTransitions(1), List.of(1, 2, 1, 2, 1));
            Assert.assertEquals(automaton.getTransitions(2), List.of(2, 1, 2));
        }
    }

    @Test
    public void smallBARenumberTest() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/test1_renumber.ba")) {
            final CompactNFA<String> automaton = BAParsers.nfa().readModel(is).model;

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
            final CompactNFA<String> automaton = BAParsers.nfa().readModel(is).model;
            Assert.assertEquals(automaton.size(), 2);
            Assert.assertEquals(automaton.getInputAlphabet().size(), 2);
            Assert.assertEquals(automaton.getInitialStates(), Collections.singleton(1));
            Assert.assertFalse(automaton.isAccepting(1));
            Assert.assertTrue(automaton.isAccepting(0));

            Assert.assertEquals(automaton.getTransitions(0), List.of(0, 0));
            Assert.assertEquals(automaton.getTransitions(1), List.of(1, 0));
        }
    }

    @Test
    public void allAcceptTest() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/test_all_accept.ba")) {
            final CompactNFA<String> automaton = BAParsers.nfa().readModel(is).model;
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
    public void emptyTest() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/empty.ba")) {
            final FastDFA<String> automaton = BAParsers.fsa(FastDFA::new).readModel(is).model;
            Assert.assertEquals(automaton.size(), 0);
            Assert.assertEquals(automaton.getInputAlphabet().size(), 0);
        }
    }

    @Test
    public void error1Test() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/err1.ba")) {
            Assert.assertThrows(FormatException.class, () -> BAParsers.dfa().readModel(is));
        }
    }

    @Test
    public void error2Test() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/err2.ba")) {
            Assert.assertThrows(FormatException.class, () -> BAParsers.dfa().readModel(is));
        }
    }

    @Test
    public void error3Test() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/err3.ba")) {
            Assert.assertThrows(FormatException.class, () -> BAParsers.dfa().readModel(is));
        }
    }

    @Test
    public void error4Test() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/err4.ba")) {
            Assert.assertThrows(FormatException.class, () -> BAParsers.dfa().readModel(is));
        }
    }

    @Test
    public void error5Test() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/err5.ba")) {
            Assert.assertThrows(FormatException.class, () -> BAParsers.dfa().readModel(is));
        }
    }

    @Test
    public void error6Test() throws Exception {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/err6.ba")) {
            Assert.assertThrows(FormatException.class, () -> BAParsers.dfa().readModel(is));
        }
    }

    @Test
    public void doNotCloseInputStreamTest() throws IOException, FormatException {
        try (InputStream is = BAParserTest.class.getResourceAsStream("/test1.ba")) {
            BAParsers.nfa().readModel(new UnclosableInputStream(is));
        }
    }
}
