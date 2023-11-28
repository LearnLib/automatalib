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
package net.automatalib.serialization.fsm.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Function;

import net.automatalib.alphabet.Alphabets;
import net.automatalib.api.alphabet.Alphabet;
import net.automatalib.automaton.transducer.CompactMealy;
import net.automatalib.common.util.io.UnclosableInputStream;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests forFSM2MealyParserIO.
 * <p>
 * This tests will involve parsing Mealy machines where transitions in the FSM are alternating (opposed to regular
 * transitions that can be directly mapped).
 *
 * @see FSM2MealyParserIOTest
 */
public class FSM2MealyParserAlternatingTest extends AbstractFSM2ParserTest {

    @Test
    public void testParse() throws Exception {
        try (InputStream is = FSM2MealyParserAlternatingTest.class.getResourceAsStream("/MealyAlternating.fsm")) {

            final Function<String, Character> ep = s -> s.charAt(0);
            final CompactMealy<Character, Character> actualMealy =
                    FSM2MealyParserAlternating.getParser(ep).readModel(is);

            final Alphabet<Character> alphabet = Alphabets.characters('a', 'a');

            CompactMealy<Character, ?> expectedMealy = AutomatonBuilders.newMealy(alphabet)
                                                                        .from("q0").on('a').withOutput('1').to("q1")
                                                                        .from("q1").on('a').withOutput('2').to("q0")
                                                                        .withInitial("q0")
                                                                        .create();

            Assert.assertTrue(Automata.testEquivalence(actualMealy, expectedMealy, alphabet));
        }
    }

    @Override
    protected CompactMealy<Character, Character> getParsedAutomaton(@Nullable Collection<Character> requiredInputs)
            throws IOException {
        try (InputStream is = FSM2MealyParserAlternatingTest.class.getResourceAsStream("/MealyAlternating.fsm")) {
            final Function<String, Character> ep = s -> s.charAt(0);
            return FSM2MealyParserAlternating.getParser(requiredInputs, null, ep).readModel(is);
        }
    }

    @Test
    public void doNotCloseInputStreamTest() throws IOException {
        try (InputStream is = FSM2MealyParserAlternatingTest.class.getResourceAsStream("/MealyAlternating.fsm")) {
            FSM2MealyParserAlternating.getParser(s -> s.charAt(0)).readModel(new UnclosableInputStream(is));
        }
    }
}
