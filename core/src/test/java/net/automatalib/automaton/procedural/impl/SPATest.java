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
package net.automatalib.automaton.procedural.impl;

import java.util.Map;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.FastDFAState;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.util.TestUtil;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SPATest {

    static final ProceduralInputAlphabet<Character> ALPHABET;
    static final Map<Character, DFA<?, Character>> SUB_MODELS;

    static {
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'd');
        final Alphabet<Character> smallCallAlphabet = Alphabets.characters('S', 'T');
        final Alphabet<Character> bigCallAlphabet = Alphabets.characters('S', 'U');

        final ProceduralInputAlphabet<Character> smallAlphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, smallCallAlphabet, 'R');

        ALPHABET = new DefaultProceduralInputAlphabet<>(internalAlphabet, bigCallAlphabet, 'R');
        SUB_MODELS = Map.of('S', buildSProcedure(smallAlphabet), 'T', buildTProcedure(smallAlphabet));
    }

    @Test
    public void testStackSPA() {
        final SPA<?, Character> spa = new StackSPA<>(ALPHABET, 'S', SUB_MODELS);

        // Check getters
        Assert.assertEquals(spa.getInputAlphabet(), ALPHABET);
        Assert.assertEquals(spa.getInitialProcedure(), (Character) 'S');
        Assert.assertEquals(spa.getProcedures(), SUB_MODELS);
        Assert.assertEquals(spa.size(), SUB_MODELS.values().stream().mapToInt(DFA::size).sum());

        // Well-matched palindromes
        TestUtil.checkOutput(spa, Word.fromString("SR"), true);
        TestUtil.checkOutput(spa, Word.fromString("SaR"), true);
        TestUtil.checkOutput(spa, Word.fromString("SaSRaR"), true);
        TestUtil.checkOutput(spa, Word.fromString("SbSTcRRbR"), true);

        // Well-matched but invalid words
        TestUtil.checkOutput(spa, Word.fromString("SaaR"), false);
        TestUtil.checkOutput(spa, Word.fromString("SaTaRaR"), false);
        TestUtil.checkOutput(spa, Word.epsilon(), false);

        // Ill-matched/non-rooted words
        TestUtil.checkOutput(spa, Word.fromString("SSS"), false);
        TestUtil.checkOutput(spa, Word.fromString("RS"), false);
        TestUtil.checkOutput(spa, Word.fromString("aba"), false);

        // Un-specified symbols
        TestUtil.checkOutput(spa, Word.fromString("SdR"), false);
        TestUtil.checkOutput(spa, Word.fromString("SaUcRaR"), false);
    }

    @Test
    public void testEmptySPA() {
        final SPA<?, Character> spa = new EmptySPA<>(ALPHABET);

        // Check getters
        Assert.assertEquals(spa.getInputAlphabet(), ALPHABET);
        Assert.assertNull(spa.getInitialProcedure());
        Assert.assertTrue(spa.getProcedures().isEmpty());

        // Well-matched palindromes
        TestUtil.checkOutput(spa, Word.fromString("SR"), false);
        TestUtil.checkOutput(spa, Word.fromString("SaR"), false);
        TestUtil.checkOutput(spa, Word.fromString("SaSRaR"), false);
        TestUtil.checkOutput(spa, Word.fromString("SbSTcRRbR"), false);

        // Well-matched but invalid words
        TestUtil.checkOutput(spa, Word.fromString("SaaR"), false);
        TestUtil.checkOutput(spa, Word.fromString("SaTaRaR"), false);
        TestUtil.checkOutput(spa, Word.epsilon(), false);

        // Ill-matched/non-rooted words
        TestUtil.checkOutput(spa, Word.fromString("SSS"), false);
        TestUtil.checkOutput(spa, Word.fromString("RS"), false);
        TestUtil.checkOutput(spa, Word.fromString("aba"), false);

        // Un-specified symbols
        TestUtil.checkOutput(spa, Word.fromString("SdR"), false);
        TestUtil.checkOutput(spa, Word.fromString("SaUcRaR"), false);
    }

    private static DFA<?, Character> buildSProcedure(ProceduralInputAlphabet<Character> alphabet) {
        final CompactDFA<Character> procedure = new CompactDFA<>(alphabet.getProceduralAlphabet());

        final int s0 = procedure.addInitialState(true);
        final int s1 = procedure.addState(true);
        final int s2 = procedure.addState(true);
        final int s3 = procedure.addState(false);
        final int s4 = procedure.addState(false);
        final int s5 = procedure.addState(true);

        procedure.addTransition(s0, 'T', s5);
        procedure.addTransition(s0, 'a', s1);
        procedure.addTransition(s0, 'b', s2);
        procedure.addTransition(s1, 'S', s3);
        procedure.addTransition(s2, 'S', s4);
        procedure.addTransition(s3, 'a', s5);
        procedure.addTransition(s4, 'b', s5);

        return procedure;
    }

    private static DFA<?, Character> buildTProcedure(ProceduralInputAlphabet<Character> alphabet) {
        final FastDFA<Character> procedure = new FastDFA<>(alphabet.getProceduralAlphabet());

        final FastDFAState t0 = procedure.addInitialState(false);
        final FastDFAState t1 = procedure.addState(true);
        final FastDFAState t2 = procedure.addState(false);
        final FastDFAState t3 = procedure.addState(true);

        procedure.addTransition(t0, 'S', t3);
        procedure.addTransition(t0, 'c', t1);
        procedure.addTransition(t1, 'T', t2);
        procedure.addTransition(t2, 'c', t3);

        return procedure;
    }
}
