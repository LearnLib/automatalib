/* Copyright (C) 2013-2017 TU Dortmund
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

import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for DFA2ETFWriter.
 *
 * @author Jeroen Meijer
 */
public class DFA2ETFWriterTest {

    @Test
    public void testWrite() throws Exception {
        final StringWriter sw = new StringWriter();

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');

        final Random random = new Random(0);
        final DFA<?, Character> automaton = RandomAutomata.randomDFA(random, 20, alphabet);

        DFA2ETFWriter.write(sw, automaton, alphabet);

        final InputStream is = DFA2ETFWriterTest.class.getResourceAsStream("/DFA-testWrite.etf");
        final String expected = IOUtils.toString(IOUtil.asUTF8Reader(is));

        Assert.assertEquals(sw.toString(), expected);
    }
}
