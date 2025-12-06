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
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.transducer.impl.FastMealy;
import net.automatalib.automaton.transducer.impl.FastMealyState;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SPMMTest {

    private final SPMM<?, Character, ?, Character> spmm;
    private final EmptySPMM<Character, Character> emptySpmm;

    public SPMMTest() {
        final Alphabet<Character> smallCallAlphabet = Alphabets.characters('S', 'T');
        final Alphabet<Character> bigCallAlphabet = Alphabets.characters('S', 'U');

        final ProceduralInputAlphabet<Character> smallAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('a', 'c'), smallCallAlphabet, 'R');
        final ProceduralInputAlphabet<Character> bigAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('a', 'c'), bigCallAlphabet, 'R');
        final Character errorOutput = '-';

        final Map<Character, MealyMachine<?, Character, ?, Character>> mealies =
                Map.of('S', buildSProcedure(smallAlphabet), 'T', buildTProcedure(smallAlphabet));

        spmm = new StackSPMM<>(bigAlphabet, 'S', '✓', errorOutput, mealies);
        emptySpmm = new EmptySPMM<>(bigAlphabet, errorOutput);
    }

    private static MealyMachine<?, Character, ?, Character> buildSProcedure(ProceduralInputAlphabet<Character> alphabet) {
        final CompactMealy<Character, Character> procedure = new CompactMealy<>(alphabet);

        final int s0 = procedure.addInitialState();
        final int s1 = procedure.addState();
        final int s2 = procedure.addState();
        final int s3 = procedure.addState();
        final int s4 = procedure.addState();
        final int s5 = procedure.addState();
        final int s6 = procedure.addState();

        procedure.addTransition(s0, 'a', s1, 'x');
        procedure.addTransition(s0, 'b', s2, 'y');
        procedure.addTransition(s0, 'T', s5, '✓');
        procedure.addTransition(s0, 'R', s6, '✓');
        procedure.addTransition(s1, 'S', s3, '✓');
        procedure.addTransition(s1, 'R', s6, '✓');
        procedure.addTransition(s2, 'S', s4, '✓');
        procedure.addTransition(s2, 'R', s6, '✓');
        procedure.addTransition(s3, 'a', s5, 'x');
        procedure.addTransition(s4, 'b', s5, 'y');
        procedure.addTransition(s5, 'R', s6, '✓');

        return procedure;
    }

    private static MealyMachine<?, Character, ?, Character> buildTProcedure(ProceduralInputAlphabet<Character> alphabet) {
        final FastMealy<Character, Character> procedure = new FastMealy<>(alphabet);

        final FastMealyState<Character> t0 = procedure.addInitialState();
        final FastMealyState<Character> t1 = procedure.addState();
        final FastMealyState<Character> t2 = procedure.addState();
        final FastMealyState<Character> t3 = procedure.addState();
        final FastMealyState<Character> t4 = procedure.addState();

        procedure.addTransition(t0, 'c', t1, 'z');
        procedure.addTransition(t0, 'S', t3, '✓');
        procedure.addTransition(t1, 'T', t2, '✓');
        procedure.addTransition(t1, 'R', t4, '✓');
        procedure.addTransition(t2, 'c', t3, 'z');
        procedure.addTransition(t3, 'R', t4, '✓');

        return procedure;
    }

    @Test
    public void testSPMM() {
        final Word<Character> i1 = Word.fromString("SaSTcRRaR");
        final Word<Character> o1 = Word.fromString("✓x✓✓z✓✓x✓");
        Assert.assertEquals(spmm.computeOutput(i1), o1);
        Assert.assertEquals(spmm.computeSuffixOutput(i1.prefix(5), i1.suffix(-5)), o1.suffix(-5));

        final Word<Character> i2 = Word.fromString("SaSbRaR");
        final Word<Character> o2 = Word.fromString("✓x✓y✓x✓");
        Assert.assertEquals(spmm.computeOutput(i2), o2);
        Assert.assertEquals(spmm.computeSuffixOutput(i2.prefix(5), i2.suffix(-5)), o2.suffix(-5));

        final Word<Character> i3 = Word.fromString("SaSbaRcRabc");
        final Word<Character> o3 = Word.fromString("✓x✓y-------");
        Assert.assertEquals(spmm.computeOutput(i3), o3);
        Assert.assertEquals(spmm.computeSuffixOutput(i3.prefix(5), i3.suffix(-5)), o3.suffix(-5));

        final Word<Character> i4 = Word.fromString("SaUcR");
        final Word<Character> o4 = Word.fromString("✓x---");
        Assert.assertEquals(spmm.computeOutput(i4), o4);
        Assert.assertEquals(spmm.computeSuffixOutput(i4.prefix(5), i4.suffix(-5)), i4.suffix(-5));

        final Word<Character> i5 = Word.fromString("TcR");
        final Word<Character> o5 = Word.fromString("---");
        Assert.assertEquals(spmm.computeOutput(i5), o5);
        Assert.assertEquals(spmm.computeSuffixOutput(i5.prefix(2), i5.suffix(-2)), o5.suffix(-2));

        final Word<Character> i6 = Word.fromString("Sd");
        final Word<Character> o6 = Word.fromString("✓-");
        Assert.assertEquals(spmm.computeOutput(i6), o6);
        Assert.assertEquals(spmm.computeSuffixOutput(i6.prefix(2), i6.suffix(-2)), o6.suffix(-2));

        final Word<Character> i7 = Word.fromString("aca");
        final Word<Character> o7 = Word.fromString("---");
        Assert.assertEquals(spmm.computeOutput(i7), o7);
        Assert.assertEquals(spmm.computeSuffixOutput(i7.prefix(2), i7.suffix(-2)), o7.suffix(-2));

        final Word<Character> i8 = Word.fromString("SacTcR");
        final Word<Character> o8 = Word.fromString("✓x----");
        Assert.assertEquals(spmm.computeOutput(i8), o8);
        Assert.assertEquals(spmm.computeSuffixOutput(i8.prefix(5), i8.suffix(-5)), o8.suffix(-5));

        final Word<Character> i9 = Word.fromString("R");
        final Word<Character> o9 = Word.fromString("-");
        Assert.assertEquals(spmm.computeOutput(i9), o9);
        Assert.assertEquals(spmm.computeSuffixOutput(i9.prefix(1), i9.suffix(-1)), o9.suffix(-1));

        final Word<Character> i10 = Word.fromString("STTc");
        final Word<Character> o10 = Word.fromString("✓✓--");
        Assert.assertEquals(spmm.computeOutput(i10), o10);
        Assert.assertEquals(spmm.computeSuffixOutput(i10.prefix(2), i10.suffix(-2)), o10.suffix(-2));

        final Word<Character> i11 = Word.fromString("SaSRR");
        final Word<Character> o11 = Word.fromString("✓x✓✓-");
        Assert.assertEquals(spmm.computeOutput(i11), o11);
        Assert.assertEquals(spmm.computeSuffixOutput(i11.prefix(2), i11.suffix(-2)), o11.suffix(-2));

        final Word<Character> i12 = Word.epsilon();
        final Word<Character> o12 = Word.epsilon();
        Assert.assertEquals(spmm.computeOutput(i12), o12);
    }

    @Test
    public void testEmptySPMM() {
        final Word<Character> i1 = Word.fromString("SaSTcRRaR");
        final Word<Character> o1 = Word.fromString("---------");
        Assert.assertEquals(emptySpmm.computeOutput(i1), o1);
        Assert.assertEquals(emptySpmm.computeSuffixOutput(i1.prefix(5), i1.suffix(-5)), o1.suffix(-5));

        final Word<Character> i2 = Word.fromString("SaSbRaR");
        final Word<Character> o2 = Word.fromString("-------");
        Assert.assertEquals(emptySpmm.computeOutput(i2), o2);
        Assert.assertEquals(emptySpmm.computeSuffixOutput(i2.prefix(5), i2.suffix(-5)), o2.suffix(-5));

        final Word<Character> i3 = Word.fromString("SaSbaRcRabc");
        final Word<Character> o3 = Word.fromString("-----------");
        Assert.assertEquals(emptySpmm.computeOutput(i3), o3);
        Assert.assertEquals(emptySpmm.computeSuffixOutput(i3.prefix(5), i3.suffix(-5)), o3.suffix(-5));

        final Word<Character> i4 = Word.fromString("SaUcR");
        final Word<Character> o4 = Word.fromString("-----");
        Assert.assertEquals(emptySpmm.computeOutput(i4), o4);
        Assert.assertEquals(emptySpmm.computeSuffixOutput(i4.prefix(5), i4.suffix(-5)), i4.suffix(-5));

        final Word<Character> i5 = Word.fromString("TcR");
        final Word<Character> o5 = Word.fromString("---");
        Assert.assertEquals(emptySpmm.computeOutput(i5), o5);
        Assert.assertEquals(emptySpmm.computeSuffixOutput(i5.prefix(2), i5.suffix(-2)), o5.suffix(-2));

        final Word<Character> i6 = Word.fromString("Sd");
        final Word<Character> o6 = Word.fromString("--");
        Assert.assertEquals(emptySpmm.computeOutput(i6), o6);
        Assert.assertEquals(emptySpmm.computeSuffixOutput(i6.prefix(2), i6.suffix(-2)), o6.suffix(-2));

        final Word<Character> i7 = Word.fromString("aca");
        final Word<Character> o7 = Word.fromString("---");
        Assert.assertEquals(emptySpmm.computeOutput(i7), o7);
        Assert.assertEquals(emptySpmm.computeSuffixOutput(i7.prefix(2), i7.suffix(-2)), o7.suffix(-2));

        final Word<Character> i8 = Word.fromString("SacTcR");
        final Word<Character> o8 = Word.fromString("------");
        Assert.assertEquals(emptySpmm.computeOutput(i8), o8);
        Assert.assertEquals(emptySpmm.computeSuffixOutput(i8.prefix(5), i8.suffix(-5)), o8.suffix(-5));

        final Word<Character> i9 = Word.fromString("R");
        final Word<Character> o9 = Word.fromString("-");
        Assert.assertEquals(emptySpmm.computeOutput(i9), o9);
        Assert.assertEquals(emptySpmm.computeSuffixOutput(i9.prefix(1), i9.suffix(-1)), o9.suffix(-1));

        final Word<Character> i10 = Word.fromString("STTc");
        final Word<Character> o10 = Word.fromString("----");
        Assert.assertEquals(emptySpmm.computeOutput(i10), o10);
        Assert.assertEquals(emptySpmm.computeSuffixOutput(i10.prefix(2), i10.suffix(-2)), o10.suffix(-2));

        final Word<Character> i11 = Word.fromString("SaSRR");
        final Word<Character> o11 = Word.fromString("-----");
        Assert.assertEquals(emptySpmm.computeOutput(i11), o11);
        Assert.assertEquals(emptySpmm.computeSuffixOutput(i11.prefix(2), i11.suffix(-2)), o11.suffix(-2));

        final Word<Character> i12 = Word.epsilon();
        final Word<Character> o12 = Word.epsilon();
        Assert.assertEquals(emptySpmm.computeOutput(i12), o12);
    }
}
