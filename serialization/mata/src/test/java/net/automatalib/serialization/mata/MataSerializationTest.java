/* Copyright (C) 2013-2026 TU Dortmund University
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
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.io.UnclosableInputStream;
import net.automatalib.common.util.io.UnclosableOutputStream;
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

    @Test
    public void testSerialization() throws IOException, URISyntaxException {
        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'e');
        final CompactNFA<String> nfa = TabakovVardiRandomAutomata.generateNFA(new Random(42), 5, 2, 5, alphabet);

        final StringWriter sw = new StringWriter();
        MataNFAWriter.write(sw, nfa, alphabet);

        Path path =
                Paths.get(Objects.requireNonNull(MataSerializationTest.class.getResource("/test-output.mata"))
                                 .toURI());
        String expected = Files.readString(path);

        Assert.assertEquals(sw.toString(), expected);
    }

    @Test
    public void testDeserialization() throws IOException, FormatException {
        try (Reader r = IOUtil.asBufferedUTF8Reader(MataSerializationTest.class.getResourceAsStream(
                "/easy_basic-01-neg-all1-0.mata"))) {

            final InputModelData<String, CompactNFA<String>> model =
                    MataNFAParser.parse(r, new CompactNFA.Creator<>(), Function.identity());
            final Alphabet<String> alphabet = model.alphabet;
            final CompactNFA<String> nfa = model.model;

            Assert.assertEquals(new HashSet<>(alphabet), Set.of("0", "1"));
            Assert.assertEquals(nfa.size(), 5);
            Assert.assertEquals(nfa.getInitialStates().size(), 1);
            Assert.assertEquals(nfa.getStates().stream().filter(nfa::isAccepting).count(), 2);
        }
    }

    @Test
    public void testFailOnWrongFormat() throws IOException {
        try (InputStream is = MataSerializationTest.class.getResourceAsStream("/false-T10-lhs.mata")) {
            final MataNFAParser<?, String, CompactNFA<String>> reader =
                    new MataNFAParser<>(new CompactNFA.Creator<>(), Function.identity());

            Assert.assertThrows(FormatException.class, () -> reader.readModel(is));
        }
    }

    @Test
    public void testEquivalence() throws IOException, FormatException {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 3);
        final int size = 20;

        final CompactNFA<Integer> nfa = new CompactNFA<>(alphabet, size + 2);

        // use reduced alphabet
        TabakovVardiRandomAutomata.generateNFA(new Random(42), size, 2, 5, Alphabets.integers(0, 2), nfa);
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

        Assert.assertEquals(model.alphabet, alphabet);
        Assert.assertEquals(model.model.size(), nfa.size());

        final CompactDFA<Integer> dfa1 = NFAs.determinize(nfa, false, false);
        final CompactDFA<Integer> dfa2 = NFAs.determinize(model.model, false, false);

        Assert.assertTrue(Automata.testEquivalence(dfa1, dfa2, alphabet));
    }

    @Test
    public void doNotCloseOutputStreamTest() throws IOException {
        CompactNFA<Integer> nfa =
                TabakovVardiRandomAutomata.generateNFA(new Random(42), 10, 2, 5, Alphabets.integers(0, 3));

        // assert not throws
        new MataNFAWriter<Integer>().writeModel(new UnclosableOutputStream(OutputStream.nullOutputStream()),
                                                nfa,
                                                nfa.getInputAlphabet());
    }

    @Test
    public void doNotCloseInputStreamTest() throws IOException, FormatException {
        try (InputStream is = MataSerializationTest.class.getResourceAsStream("/easy_basic-01-neg-all1-0.mata")) {
            final MataNFAParser<?, ?, ?> reader =
                    new MataNFAParser<>(new CompactNFA.Creator<>(), Function.identity());
            // assert not throws
            reader.readModel(new UnclosableInputStream(is));
        }
    }
}
