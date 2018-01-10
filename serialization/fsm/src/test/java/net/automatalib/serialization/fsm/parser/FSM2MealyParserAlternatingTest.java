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

import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests forFSM2MealyParserIO.
 *
 * This tests will involve parsing Mealy machines where transitions in the FSM are alternating (opposed to regular
 * transitions that can be directly mapped).
 *
 * @see FSM2MealyParserIOTest
 *
 * @author Jeroen Meijer
 */
public class FSM2MealyParserAlternatingTest {

    @Test
    public void testParse() throws Exception {
        final InputStream is = FSM2MealyParserAlternatingTest.class.getResourceAsStream("/MealyAlternating.fsm");

        final Function<String, Character> ep = s -> s.charAt(0);

        final CompactMealy<Character, Character> actualMealy = FSM2MealyParserAlternating.parse(is, ep, ep);
        is.close();

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'a');

        CompactMealy<Character, ?> expectedMealy = AutomatonBuilders.newMealy(alphabet).
                from("q0").on('a').withOutput('1').to("q1").
                from("q1").on('a').withOutput('2').to("q0").
                withInitial("q0").create();

        Assert.assertTrue(Automata.testEquivalence(actualMealy, expectedMealy, alphabet));
    }
}