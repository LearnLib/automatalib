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
package net.automatalib.serialization.fsm.parser;

import java.io.InputStream;
import java.util.function.Function;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for FSM2DFAParser.
 *
 * @author Jeroen Meijer
 */
public class FSM2DFAParserTest {

    @Test
    public void testParse1() throws Exception {
        final InputStream is = FSM2DFAParserTest.class.getResourceAsStream("/DFA1.fsm");

        final Function<String, Character> ip = s -> s.charAt(0);

        final CompactDFA<Character> actualDFA = FSM2DFAParser.parse(is, ip, "label", "accept");
        is.close();

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'b');

        CompactDFA<Character> expectedDFA = AutomatonBuilders.newDFA(alphabet).
                from("q0").on('a').to("q1").
                from("q1").on('b').to("q0").
                withAccepting("q1").withInitial("q0").create();

        Assert.assertTrue(Automata.testEquivalence(actualDFA, expectedDFA, alphabet));
    }

    @Test
    public void testParse2() throws Exception {
        final InputStream is = FSM2DFAParserTest.class.getResourceAsStream("/DFA2.fsm");

        final Function<String, Character> ip = s -> s.charAt(0);

        final CompactDFA<Character> actualDFA = FSM2DFAParser.parse(is, ip, "label", "accept");
        is.close();

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'a');

        CompactDFA<Character> expectedDFA = AutomatonBuilders.newDFA(alphabet).
                from("q0").on('a').loop().
                withAccepting("q0").withInitial("q0").create();

        Assert.assertTrue(Automata.testEquivalence(actualDFA, expectedDFA, alphabet));
    }
}