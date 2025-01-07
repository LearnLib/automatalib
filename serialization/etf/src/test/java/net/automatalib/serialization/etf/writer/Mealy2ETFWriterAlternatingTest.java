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
package net.automatalib.serialization.etf.writer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.io.UnclosableOutputStream;
import net.automatalib.util.automaton.random.RandomAutomata;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for Mealy2ETFWriterAlternating.
 */
public class Mealy2ETFWriterAlternatingTest {

    @Test
    public void testWrite() throws Exception {
        try (Reader r = IOUtil.asBufferedUTF8Reader(DFA2ETFWriterTest.class.getResourceAsStream("/Alt-testWrite.etf"));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');

            final Random random = new Random(0);
            final MealyMachine<?, Character, ?, Character> automaton =
                    RandomAutomata.randomMealy(random, 20, alphabet, alphabet);

            Mealy2ETFWriterAlternating.<Character, Character>getInstance().writeModel(baos, automaton, alphabet);

            final String expected = IOUtil.toString(r);

            Assert.assertEquals(baos.toString(StandardCharsets.UTF_8), expected);
        }
    }

    @Test
    public void doNotCloseOutputStreamTest() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final MealyMachine<?, Character, ?, Character> automaton =
                RandomAutomata.randomMealy(new Random(0), 10, alphabet, alphabet);

        Mealy2ETFWriterAlternating.<Character, Character>getInstance()
                                  .writeModel(new UnclosableOutputStream(OutputStream.nullOutputStream()),
                                              automaton,
                                              alphabet);
    }
}
