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
package net.automatalib.serialization.aut;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class AUTSerializationTest {

    @Test
    public void quotationLabelTest() throws Exception {
        final InputStream is = AUTSerializationTest.class.getResourceAsStream("/quotationTest.aut");

        final SimpleAutomaton<Integer, String> automaton = AUTParser.readAutomaton(is).model;

        Assert.assertEquals(4, automaton.size());

        final String input1 = "PUT_\\6";
        final String input2 = "GET !true !7 !CONS (A, CONS (B, NIL))";
        final String input3 = "SEND !\"hello\" !\"world\"";

        final Set<Integer> s0 = automaton.getInitialStates();
        final Set<Integer> s1 = automaton.getSuccessors(s0, Collections.singletonList(input1));
        final Set<Integer> s2 = automaton.getSuccessors(s0, Collections.singletonList(input2));
        final Set<Integer> s3 = automaton.getSuccessors(s0, Arrays.asList(input2, input3));

        Assert.assertEquals(Collections.singleton(0), s0);
        Assert.assertEquals(Collections.singleton(1), s1);
        Assert.assertEquals(Collections.singleton(2), s2);
        Assert.assertEquals(Collections.singleton(3), s3);
    }

    @Test
    public void serializationTest() throws Exception {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 2);
        final Random random = new Random(0);
        final DFA<Integer, Integer> automaton = RandomAutomata.randomDFA(random, 20, alphabet);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        AUTWriter.writeAutomaton(automaton, alphabet, baos);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        final SimpleAutomaton<Integer, Integer> deserialized = AUTParser.readAutomaton(is, Integer::parseInt).model;

        equalityTest(automaton, deserialized, alphabet);

        baos.close();
        is.close();
    }

    private <S, I> void equalityTest(SimpleAutomaton<S, I> src, SimpleAutomaton<S, I> target, Alphabet<I> inputs) {
        for (final S s : src.getStates()) {
            for (final I i : inputs) {
                Assert.assertEquals(src.getSuccessors(s, i), target.getSuccessors(s, i));
            }
        }
    }

    @Test
    public void errorTest() throws Exception {
        final InputStream e1 = AUTSerializationTest.class.getResourceAsStream("/error1.aut");
        final InputStream e2 = AUTSerializationTest.class.getResourceAsStream("/error2.aut");
        final InputStream e3 = AUTSerializationTest.class.getResourceAsStream("/error3.aut");

        Assert.assertThrows(() -> AUTParser.readAutomaton(e1));
        Assert.assertThrows(() -> AUTParser.readAutomaton(e2));
        Assert.assertThrows(() -> AUTParser.readAutomaton(e3));
    }

}
