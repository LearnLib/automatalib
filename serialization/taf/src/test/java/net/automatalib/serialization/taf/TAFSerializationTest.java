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
import net.automatalib.serialization.InputModelSerializationProvider;
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
        final Alphabet<String> alphabet = Alphabets.fromArray("0", "1", "2");

        testInternal(alphabet, (p, is) -> p.readModel(is).model);
    }

    @Test
    public void testSerialization() throws Exception {
        final Alphabet<String> alphabet = Alphabets.fromArray("a", "b", "c");

        testInternal(alphabet, (p, is) -> p.readModel(is).model);
    }

    private void testInternal(Alphabet<String> alphabet,
                              ThrowableBiFunction<InputModelSerializationProvider<String, DFA<?, String>, DFA<Integer, String>>, InputStream, DFA<?, String>> deserializer)
            throws IOException {

        final Random random = new Random(0);
        final DFA<?, String> automaton = RandomAutomata.randomDFA(random, 20, alphabet);

        final TAFSerialization serProvider = TAFSerialization.getInstance();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        serProvider.writeModel(baos, automaton, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        final DFA<?, String> deserialized = deserializer.apply(serProvider, is);

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
