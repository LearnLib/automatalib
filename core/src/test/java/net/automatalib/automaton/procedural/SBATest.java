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
package net.automatalib.automaton.procedural;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.FastDFAState;
import net.automatalib.automaton.fsa.impl.compact.CompactDFA;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SBATest {

    static final ProceduralInputAlphabet<Character> ALPHABET;
    static final Map<Character, DFA<?, Character>> SUB_MODELS;

    static {
        final Alphabet<Character> smallCallAlphabet = Alphabets.characters('S', 'T');
        final Alphabet<Character> bigCallAlphabet = Alphabets.characters('S', 'U');

        final ProceduralInputAlphabet<Character> smallAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('a', 'c'), smallCallAlphabet, 'R');

        ALPHABET = new DefaultProceduralInputAlphabet<>(Alphabets.characters('a', 'c'), bigCallAlphabet, 'R');
        SUB_MODELS = ImmutableMap.of('S', buildSProcedure(smallAlphabet), 'T', buildTProcedure(smallAlphabet));
    }

    private static DFA<?, Character> buildSProcedure(ProceduralInputAlphabet<Character> alphabet) {
        final CompactDFA<Character> procedure = new CompactDFA<>(alphabet);

        final int s0 = procedure.addInitialState(true);
        final int s1 = procedure.addState(true);
        final int s2 = procedure.addState(true);
        final int s3 = procedure.addState(true);
        final int s4 = procedure.addState(true);
        final int s5 = procedure.addState(true);
        final int s6 = procedure.addState(true);

        procedure.addTransition(s0, 'a', s1);
        procedure.addTransition(s0, 'b', s2);
        procedure.addTransition(s0, 'T', s5);
        procedure.addTransition(s0, 'R', s6);
        procedure.addTransition(s1, 'S', s3);
        procedure.addTransition(s1, 'R', s6);
        procedure.addTransition(s2, 'S', s4);
        procedure.addTransition(s2, 'R', s6);
        procedure.addTransition(s3, 'a', s5);
        procedure.addTransition(s4, 'b', s5);
        procedure.addTransition(s5, 'R', s6);

        return procedure;
    }

    private static DFA<?, Character> buildTProcedure(ProceduralInputAlphabet<Character> alphabet) {
        final FastDFA<Character> procedure = new FastDFA<>(alphabet);

        final FastDFAState t0 = procedure.addInitialState(true);
        final FastDFAState t1 = procedure.addState(true);
        final FastDFAState t2 = procedure.addState(true);
        final FastDFAState t3 = procedure.addState(true);
        final FastDFAState t4 = procedure.addState(true);

        procedure.addTransition(t0, 'c', t1);
        procedure.addTransition(t0, 'S', t3);
        procedure.addTransition(t1, 'T', t2);
        procedure.addTransition(t1, 'R', t4);
        procedure.addTransition(t2, 'c', t3);
        procedure.addTransition(t3, 'R', t4);

        return procedure;
    }

    @Test
    public void testSBA() {
        final SBA<?, Character> sba = new StackSBA<>(ALPHABET, 'S', SUB_MODELS);

        final Word<Character> i1 = Word.fromCharSequence("SaSTcRRaR");
        Assert.assertTrue(sba.accepts(i1));

        final Word<Character> i2 = Word.fromCharSequence("SaSbRaR");
        Assert.assertTrue(sba.accepts(i2));

        final Word<Character> i3 = Word.fromCharSequence("SaSbaRcRabc");
        Assert.assertFalse(sba.accepts(i3));

        final Word<Character> i4 = Word.fromCharSequence("SaUcR");
        Assert.assertFalse(sba.accepts(i4));

        final Word<Character> i5 = Word.fromCharSequence("TcR");
        Assert.assertFalse(sba.accepts(i5));

        final Word<Character> i6 = Word.fromCharSequence("Sd");
        Assert.assertFalse(sba.accepts(i6));

        final Word<Character> i7 = Word.fromCharSequence("aca");
        Assert.assertFalse(sba.accepts(i7));

        final Word<Character> i8 = Word.fromCharSequence("SacTcR");
        Assert.assertFalse(sba.accepts(i8));

        final Word<Character> i9 = Word.fromCharSequence("R");
        Assert.assertFalse(sba.accepts(i9));

        final Word<Character> i10 = Word.fromCharSequence("STTc");
        Assert.assertFalse(sba.accepts(i10));

        final Word<Character> i11 = Word.fromCharSequence("SaSRR");
        Assert.assertFalse(sba.accepts(i11));

        final Word<Character> i12 = Word.epsilon();
        Assert.assertTrue(sba.accepts(i12));
    }

    @Test
    public void testEmptySBA() {
        final SBA<?, Character> sba = new EmptySBA<>(ALPHABET);

        final Word<Character> i1 = Word.fromCharSequence("SaSTcRRaR");
        Assert.assertFalse(sba.accepts(i1));

        final Word<Character> i2 = Word.fromCharSequence("SaSbRaR");
        Assert.assertFalse(sba.accepts(i2));

        final Word<Character> i3 = Word.fromCharSequence("SaSbaRcRabc");
        Assert.assertFalse(sba.accepts(i3));

        final Word<Character> i4 = Word.fromCharSequence("SaUcR");
        Assert.assertFalse(sba.accepts(i4));

        final Word<Character> i5 = Word.fromCharSequence("TcR");
        Assert.assertFalse(sba.accepts(i5));

        final Word<Character> i6 = Word.fromCharSequence("Sd");
        Assert.assertFalse(sba.accepts(i6));

        final Word<Character> i7 = Word.fromCharSequence("aca");
        Assert.assertFalse(sba.accepts(i7));

        final Word<Character> i8 = Word.fromCharSequence("SacTcR");
        Assert.assertFalse(sba.accepts(i8));

        final Word<Character> i9 = Word.fromCharSequence("R");
        Assert.assertFalse(sba.accepts(i9));

        final Word<Character> i10 = Word.fromCharSequence("STTc");
        Assert.assertFalse(sba.accepts(i10));

        final Word<Character> i11 = Word.fromCharSequence("SaSRR");
        Assert.assertFalse(sba.accepts(i11));

        final Word<Character> i12 = Word.epsilon();
        Assert.assertFalse(sba.accepts(i12));
    }
}
