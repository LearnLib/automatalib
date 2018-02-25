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
package net.automatalib.serialization.etf.writer;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Random;

import com.google.common.io.CharStreams;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for Mealy2ETFWriterAlternating.
 *
 * @author Jeroen Meijer
 */
public class Mealy2ETFWriterAlternatingTest {
    @Test
    public void testWrite() throws Exception {
        final StringWriter sw = new StringWriter();

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');

        final Random random = new Random(0);
        final MealyMachine<?, Character, ?, Character> automaton = RandomAutomata.randomMealy(random, 20, alphabet,
                alphabet);

        Mealy2ETFWriterAlternating.write(sw, automaton, alphabet);

        final InputStream is = DFA2ETFWriterTest.class.getResourceAsStream("/Alt-testWrite.etf");
        final String expected = CharStreams.toString(IOUtil.asBufferedUTF8Reader(is));

        Assert.assertEquals(sw.toString(), expected);
    }
}
