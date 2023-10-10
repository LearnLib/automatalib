/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.example.procedural;

import java.util.HashMap;
import java.util.Map;

import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.compact.CompactDFA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.StackSPA;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.visualization.Visualization;
import net.automatalib.word.Alphabet;
import net.automatalib.word.ProceduralInputAlphabet;
import net.automatalib.word.Word;
import net.automatalib.word.impl.Alphabets;
import net.automatalib.word.impl.DefaultProceduralInputAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A small example for constructing a (context-free) palindrome example over the letters {@code a, b, c} using two
 * separate procedures (non-terminals) {@code F} and {@code G}.
 */
public final class PalindromeExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(PalindromeExample.class);

    private PalindromeExample() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        final SPA<?, Character> spa = buildSPA();

        LOGGER.info("Well-matched palindromes");
        checkWord(spa, Word.fromCharSequence("FR"));
        checkWord(spa, Word.fromCharSequence("FaR"));
        checkWord(spa, Word.fromCharSequence("FaFaR"));
        checkWord(spa, Word.fromCharSequence("FbFGcRRbR"));

        LOGGER.info("");
        LOGGER.info("Well-matched but invalid words");
        checkWord(spa, Word.fromCharSequence("FaaR"));
        checkWord(spa, Word.fromCharSequence("FaGaRaR"));
        checkWord(spa, Word.epsilon());

        LOGGER.info("");
        LOGGER.info("Ill-matched/non-rooted words");
        checkWord(spa, Word.fromCharSequence("FFF"));
        checkWord(spa, Word.fromCharSequence("RF"));
        checkWord(spa, Word.fromCharSequence("aba"));

        Visualization.visualize(spa);
    }

    private static <S, I> void checkWord(SPA<S, I> spa, Word<I> input) {
        final boolean accepted = spa.accepts(input);
        LOGGER.info("Word '{}' is {}accepted by the SPA", input, accepted ? "" : "not ");
    }

    public static SPA<?, Character> buildSPA() {
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
        final Alphabet<Character> callAlphabet = Alphabets.characters('F', 'G');
        final ProceduralInputAlphabet<Character> alphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, 'R');

        final DFA<?, Character> sProcedure = buildFProcedure(alphabet);
        final DFA<?, Character> tProcedure = buildGProcedure(alphabet);

        final Map<Character, DFA<?, Character>> subModels = new HashMap<>();
        subModels.put('F', sProcedure);
        subModels.put('G', tProcedure);

        // explicit type variable declaration to make checker-framework happy
        return new StackSPA<@Nullable Object, Character>(alphabet, 'F', subModels);
    }

    /**
     * Utility method for building a procedure based on a {@link CompactDFA} that emits terminal symbols 'a', 'b' or
     * delegates to procedure {@link #buildGProcedure(ProceduralInputAlphabet) 'G'}.
     */
    private static DFA<?, Character> buildFProcedure(ProceduralInputAlphabet<Character> alphabet) {
        // @formatter:off
        return AutomatonBuilders.forDFA(new CompactDFA<>(alphabet.getProceduralAlphabet()))
                                .withInitial("s0")
                                .from("s0").on('G').to("s5")
                                .from("s0").on('a').to("s1")
                                .from("s0").on('b').to("s2")
                                .from("s1").on('F').to("s3")
                                .from("s2").on('F').to("s4")
                                .from("s3").on('a').to("s5")
                                .from("s4").on('b').to("s5")
                                .withAccepting("s0", "s1", "s2", "s5")
                                .create();
        // @formatter:on
    }

    /**
     * Utility method for building a procedure based on a {@link FastDFA} that emits the terminal symbol 'c' or
     * delegates to procedure {@link #buildFProcedure(ProceduralInputAlphabet) 'F'}.
     */
    private static DFA<?, Character> buildGProcedure(ProceduralInputAlphabet<Character> alphabet) {
        // @formatter:off
        return AutomatonBuilders.forDFA(new FastDFA<>(alphabet.getProceduralAlphabet()))
                                .withInitial("t0")
                                .from("t0").on('F').to("t3")
                                .from("t0").on('c').to("t1")
                                .from("t1").on('G').to("t2")
                                .from("t2").on('c').to("t3")
                                .withAccepting("t1", "t3")
                                .create();
        // @formatter:on
    }

}
