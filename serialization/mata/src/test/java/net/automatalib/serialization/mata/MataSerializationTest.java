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
package net.automatalib.serialization.mata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.mata.parser.MataNFAParser;
import net.automatalib.serialization.mata.writer.MataNFAWriter;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.fsa.NFAs;
import net.automatalib.util.automaton.random.TabakovVardiRandomAutomata;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MataSerializationTest {

    private static final Alphabet<Integer> INPUT_ALPHABET = Alphabets.integers(0, 3);
    private static final int AUTOMATON_SIZE = 20;

    @Test
    public void testSerialization() {

    }

    @Test
    public void testDeserialization() throws IOException, FormatException {
        try (InputStream is = MataSerializationTest.class.getResourceAsStream("/easy_basic-01-neg-all1-0.mata")) {
            final MataNFAParser<?, String, CompactNFA<String>> reader =
                    new MataNFAParser<>(new CompactNFA.Creator<>(), Function.identity());

            final InputModelData<String, CompactNFA<String>> model = reader.readModel(is);
            final Alphabet<String> alphabet = model.alphabet;
            final CompactNFA<String> nfa = model.model;

            Assert.assertEquals(new HashSet<>(alphabet), Set.of("0", "1"));
            Assert.assertEquals(nfa.size(), 5);
            Assert.assertEquals(nfa.getInitialStates().size(), 2);
            Assert.assertEquals((int) nfa.getStates().stream().filter(nfa::isAccepting).count(), 2);
        }
    }

    @Test
    public void testEquivalence() throws IOException, FormatException {

        final CompactNFA<Integer> nfa = new CompactNFA<>(INPUT_ALPHABET, AUTOMATON_SIZE + 2);

        // use reduced alphabet
        TabakovVardiRandomAutomata.generateNFA(new Random(42), AUTOMATON_SIZE, 2, 5, Alphabets.integers(0, 2), nfa);
        // add non-reachable state
        nfa.addState(true);
        nfa.addInitialState();

        final MataNFAWriter<Integer> writer = new MataNFAWriter<>();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        writer.writeModel(baos, nfa, nfa.getInputAlphabet());

        final MataNFAParser<Integer, Integer, CompactNFA<Integer>> reader =
                new MataNFAParser<>(new CompactNFA.Creator<>(), Integer::parseInt);

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        InputModelData<Integer, CompactNFA<Integer>> model = reader.readModel(bais);

        Assert.assertEquals(model.alphabet, INPUT_ALPHABET);
        Assert.assertEquals(model.model.size(), nfa.size());

        final CompactDFA<Integer> dfa1 = NFAs.determinize(nfa, false, false);
        final CompactDFA<Integer> dfa2 = NFAs.determinize(model.model, false, false);

        Assert.assertTrue(Automata.testEquivalence(dfa1, dfa2, INPUT_ALPHABET));
    }
}
