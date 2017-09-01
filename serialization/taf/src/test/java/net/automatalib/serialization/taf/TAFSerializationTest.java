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
package net.automatalib.serialization.taf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.serialization.SerializationProvider;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class TAFSerializationTest {

    @Test
    public void testGenericSerialization() throws Exception {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 2);

        testInternal(alphabet, SerializationProvider::readGenericDFA);
    }

    @Test
    public void testSerialization() throws Exception {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');

        testInternal(alphabet, (s, is) -> s.readCustomDFA(is, alphabet));
    }

    private <I> void testInternal(Alphabet<I> alphabet,
                                  ThrowableBiFunction<SerializationProvider, InputStream, DFA<?, I>> deserializer)
            throws IOException {

        final Random random = new Random(0);
        final DFA<?, I> automaton = RandomAutomata.randomDFA(random, 20, alphabet);

        final TAFSerialization serProvider = TAFSerialization.getInstance();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        serProvider.writeDFA(automaton, alphabet, baos);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        final DFA<?, I> deserialized = deserializer.apply(serProvider, is);

        Assert.assertTrue(Automata.testEquivalence(automaton, deserialized, alphabet));

        baos.close();
        is.close();
    }

    // Convenience interface to allow inlining throwing functions
    @FunctionalInterface
    private interface ThrowableBiFunction<R, S, T> {

        T apply(R arg1, S arg2) throws IOException;
    }

}
