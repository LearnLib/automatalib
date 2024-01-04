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
package net.automatalib.example.dot;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.Automaton;
import net.automatalib.common.util.Pair;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.visualization.dot.DOT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example for using the DOT visualizer for displaying multiple automata at the same time.
 */
public final class DOTExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(DOTExample.class);

    private static final Alphabet<Integer> INPUT_ALPHABET = Alphabets.integers(1, 6);
    private static final Alphabet<Character> OUTPUT_ALPHABET = Alphabets.characters('a', 'f');
    private static final int AUTOMATON_SIZE = 10;

    private DOTExample() {
        // prevent instantiation
    }

    public static void main(String[] args) throws IOException {
        if (DOT.checkUsable()) {
            final Random r = new Random(42);
            final List<Pair<String, String>> graphs = Arrays.asList(Pair.of("Automaton 1", generateRandomAutomaton(r)),
                                                                    Pair.of("Automaton 2", generateRandomAutomaton(r)),
                                                                    Pair.of("Automaton 3", generateRandomAutomaton(r)));

            DOT.renderDOTStrings(graphs, true);
        } else {
            LOGGER.warn("DOT does not seem usable. Check you configuration.");
        }
    }

    /**
     * Generate DOT code for some random automaton.
     *
     * @param random
     *         the random object used for generating random values
     *
     * @return the serialized (DOT) representation of the automaton.
     */
    private static String generateRandomAutomaton(Random random) throws IOException {

        final Automaton<?, Integer, ?> automaton;

        if (random.nextBoolean()) {
            automaton = RandomAutomata.randomDFA(random, AUTOMATON_SIZE, INPUT_ALPHABET);
        } else {
            automaton = RandomAutomata.randomMealy(random, AUTOMATON_SIZE, INPUT_ALPHABET, OUTPUT_ALPHABET);
        }

        final StringWriter writer = new StringWriter();

        GraphDOT.write(automaton, INPUT_ALPHABET, writer);

        return writer.toString();
    }
}
