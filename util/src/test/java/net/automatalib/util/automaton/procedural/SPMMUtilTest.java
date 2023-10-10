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
package net.automatalib.util.automaton.procedural;

import java.util.Map.Entry;
import java.util.Random;

import com.google.common.collect.ImmutableMap;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.ProceduralOutputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.impl.DefaultProceduralOutputAlphabet;
import net.automatalib.automaton.procedural.EmptySPMM;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.procedural.StackSPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.FastMealy;
import net.automatalib.automaton.transducer.impl.FastMealyState;
import net.automatalib.automaton.transducer.impl.compact.CompactMealy;
import net.automatalib.common.util.Pair;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SPMMUtilTest {

    private final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
    private final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'T');
    private final char returnSymbol = 'R';

    private final ProceduralInputAlphabet<Character> inputAlphabet =
            new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, returnSymbol);
    final ProceduralInputAlphabet<Character> emptyAlphabet =
            new DefaultProceduralInputAlphabet<>(Alphabets.fromArray(), Alphabets.fromArray(), returnSymbol);
    private final ProceduralOutputAlphabet<Character> outputAlphabet =
            new DefaultProceduralOutputAlphabet<>(Alphabets.characters('x', 'z'), '-');

    @Test
    public void testATSequences() {
        final Random random = new Random(42);
        final SPMM<?, Character, ?, Character> spmm =
                RandomAutomata.randomSPMM(random, inputAlphabet, outputAlphabet, 10);
        final ATSequences<Character> atSequences = SPMMUtil.computeATSequences(spmm);

        Assert.assertTrue(atSequences.accessSequences.keySet().containsAll(inputAlphabet.getCallAlphabet()));

        for (Word<Character> as : atSequences.accessSequences.values()) {
            Assert.assertFalse(spmm.isErrorOutput(spmm.computeOutput(as).lastSymbol()));
        }

        for (Entry<Character, Word<Character>> e : atSequences.terminatingSequences.entrySet()) {
            final Character key = e.getKey();
            final Word<Character> as = atSequences.accessSequences.get(key);
            final Word<Character> ts = e.getValue();

            final MealyMachine<?, Character, ?, Character> procedure = spmm.getProcedure(key);

            Assert.assertNotNull(procedure);

            final Word<Character> globalInput =
                    Word.fromWords(as, ts, Word.fromLetter(inputAlphabet.getReturnSymbol()));
            final Word<Character> globalOutput = spmm.computeOutput(globalInput);

            final Pair<Word<Character>, Word<Character>> localOutput =
                    inputAlphabet.project(globalInput, globalOutput, as.size());

            Assert.assertEquals(procedure.computeOutput(localOutput.getFirst()), localOutput.getSecond());
            Assert.assertFalse(spmm.isErrorOutput(localOutput.getSecond().lastSymbol()));
            Assert.assertFalse(spmm.isErrorOutput(globalOutput.lastSymbol()));
        }
    }

    @Test
    public void testEmptyCompleteATRSequences() {
        final SPMM<?, Character, ?, Character> spmm = new EmptySPMM<>(inputAlphabet, outputAlphabet.getErrorSymbol());
        final ATSequences<Character> atrSequences = SPMMUtil.computeATSequences(spmm);

        Assert.assertTrue(atrSequences.accessSequences.isEmpty());
        Assert.assertTrue(atrSequences.terminatingSequences.isEmpty());
    }

    @Test
    public void testDefaultSeparatingWord() {
        final Random random = new Random(42);
        final int size = 10;

        final SPMM<?, Character, ?, Character> spmm1 =
                RandomAutomata.randomSPMM(random, inputAlphabet, outputAlphabet, size);
        final SPMM<?, Character, ?, Character> spmm2 =
                RandomAutomata.randomSPMM(random, inputAlphabet, outputAlphabet, size);

        Assert.assertNull(Automata.findSeparatingWord(spmm1, spmm1, inputAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spmm2, spmm2, inputAlphabet));

        final Word<Character> sepWord1 = Automata.findSeparatingWord(spmm1, spmm2, inputAlphabet);
        final Word<Character> sepWord2 = Automata.findSeparatingWord(spmm2, spmm1, inputAlphabet);
        Assert.assertNotNull(sepWord1);
        Assert.assertNotNull(sepWord2);
        Assert.assertNotEquals(spmm1.computeOutput(sepWord1), spmm2.computeOutput(sepWord1));
        Assert.assertNotEquals(spmm1.computeOutput(sepWord2), spmm2.computeOutput(sepWord2));

        Assert.assertNull(Automata.findSeparatingWord(spmm1, spmm1, emptyAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spmm2, spmm2, emptyAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spmm1, spmm2, emptyAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spmm2, spmm1, emptyAlphabet));
    }

    // Copied and adjusted from the corresponding method in the SPAUtil test
    @Test
    public void testIntricateSeparatingWord() {
        final Character errorOutput = outputAlphabet.getErrorSymbol();

        // construct a simple (pseudo) palindrome system which we will gradually alter to model different cases
        final CompactMealy<Character, Character> s1 = new CompactMealy<>(inputAlphabet);
        final FastMealy<Character, Character> t1 = new FastMealy<>(inputAlphabet);

        final FastMealyState<Character> t1t0 = t1.addInitialState();
        final FastMealyState<Character> t1t4 = t1.addState();

        t1.addTransition(t1t0, 'R', t1t4, '-');

        final SPMM<?, Character, ?, Character> spmm1 =
                new StackSPMM<>(inputAlphabet, 'S', '✓', errorOutput, ImmutableMap.of('S', s1, 'T', t1));

        final CompactMealy<Character, Character> s2 = new CompactMealy<>(inputAlphabet);
        final FastMealy<Character, Character> t2 = new FastMealy<>(inputAlphabet);

        final FastMealyState<Character> t2t0 = t2.addInitialState();
        final FastMealyState<Character> t2t4 = t2.addState();

        t2.addTransition(t2t0, 'R', t2t4, '-');

        final SPMM<?, Character, ?, Character> spmm2 =
                new StackSPMM<>(inputAlphabet, 'S', '✓', errorOutput, ImmutableMap.of('S', s2, 'T', t2));
        final SPMM<?, Character, ?, Character> emptySPMM = new EmptySPMM<>(inputAlphabet, errorOutput);

        // no accessible procedures, no separating word should exist. Even with the empty SPMMs
        Assert.assertNull(Automata.findSeparatingWord(spmm1, spmm2, inputAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(emptySPMM, spmm2, inputAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spmm1, emptySPMM, inputAlphabet));

        // make SPMM1's 'S' procedure not empty. Now there should exist a separating word
        final int s1s0 = s1.addInitialState();

        verifySepWord(spmm1, spmm2, inputAlphabet);
        verifySepWord(spmm2, spmm1, inputAlphabet);
        verifySepWord(spmm1, emptySPMM, inputAlphabet);
        verifySepWord(emptySPMM, spmm1, inputAlphabet);

        // make SPMM1's initial procedure transduce 'a';
        final int s1s1 = s1.addState();
        s1.addTransition(s1s0, 'a', s1s1, 'x');

        final int s2s0 = s2.addInitialState();
        final int s2s1 = s2.addState();
        s2.addTransition(s2s0, 'a', s2s1, '-');

        // There should not exist a separating word if we restrict the alphabet to 'b','c'
        final ProceduralInputAlphabet<Character> bcAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('b', 'c'), callAlphabet, returnSymbol);
        Assert.assertNull(Automata.findSeparatingWord(spmm1, spmm2, bcAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spmm2, spmm1, bcAlphabet));

        // only with the empty SPMM
        Assert.assertEquals(Automata.findSeparatingWord(spmm1, emptySPMM, bcAlphabet), Word.fromLetter('S'));
        Assert.assertEquals(Automata.findSeparatingWord(emptySPMM, spmm1, bcAlphabet), Word.fromLetter('S'));
        Assert.assertEquals(Automata.findSeparatingWord(spmm2, emptySPMM, bcAlphabet), Word.fromLetter('S'));
        Assert.assertEquals(Automata.findSeparatingWord(emptySPMM, spmm2, bcAlphabet), Word.fromLetter('S'));

        // update SPMM2 according to SPMM1. There should no longer exist a separating word
        s2.setTransition(s2s0, (Character) 'a', s2s1, (Character) 'x');
        Assert.assertNull(Automata.findSeparatingWord(spmm1, spmm2, inputAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spmm2, spmm1, inputAlphabet));

        // make SPMM1's s5 accept so that we introduce procedure 'T'. This also adds a new separating word (two 'a's)
        final int s1s5 = s1.addState();
        s1.addTransition(s1s0, 'T', s1s5, '✓');

        verifySepWord(spmm1, spmm2, inputAlphabet);
        verifySepWord(spmm2, spmm1, inputAlphabet);

        // update SPMM2 accordingly
        final int s2s5 = s2.addState();
        s2.addTransition(s2s0, 'T', s2s5, '✓');

        Assert.assertNull(Automata.findSeparatingWord(spmm1, spmm2, inputAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spmm2, spmm1, inputAlphabet));

        // make SPMM1's procedure 'T' transduce on 'c' so that we can find another separating word
        final FastMealyState<Character> t1t1 = t1.addState();
        t1.addTransition(t1t0, 'c', t1t1, 'z');

        verifySepWord(spmm1, spmm2, inputAlphabet);
        verifySepWord(spmm2, spmm1, inputAlphabet);

        // this should also work for partial SPMMs
        final SPMM<?, Character, ?, Character> partial1 =
                new StackSPMM<>(inputAlphabet, 'S', '✓', errorOutput, ImmutableMap.of('S', s1));
        verifySepWord(spmm1, partial1, inputAlphabet);
        verifySepWord(partial1, spmm1, inputAlphabet);

        // If we restrict ourselves to only 'S' call symbols, a separating word should no longer exist
        final ProceduralInputAlphabet<Character> sAlphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, Alphabets.singleton('S'), returnSymbol);
        Assert.assertNull(Automata.findSeparatingWord(spmm1, spmm2, sAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spmm2, spmm1, sAlphabet));

        // update SPMM2 accordingly
        final FastMealyState<Character> t2t1 = t2.addState();
        t2.addTransition(t2t0, 'c', t2t1, 'z');

        Assert.assertNull(Automata.findSeparatingWord(spmm1, spmm2, inputAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spmm2, spmm1, inputAlphabet));

        // make SPMM1's 'T' procedure return on c.
        // This should yield a separating word even if we restrict ourselves to only 'c' as internal symbol
        t1.setTransition(t1t1, 'R', t1t4, '✓');

        final ProceduralInputAlphabet<Character> cAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.singleton('c'), callAlphabet, returnSymbol);
        verifySepWord(spmm1, spmm2, inputAlphabet);
        verifySepWord(spmm2, spmm1, inputAlphabet);
        verifySepWord(spmm1, spmm2, cAlphabet);
        verifySepWord(spmm2, spmm1, cAlphabet);
    }

    private static <I, O> void verifySepWord(SPMM<?, I, ?, O> spmm1,
                                             SPMM<?, I, ?, O> spmm2,
                                             ProceduralInputAlphabet<I> alphabet) {
        final Word<I> sepWord = Automata.findSeparatingWord(spmm1, spmm2, alphabet);
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(spmm1.computeOutput(sepWord), spmm2.computeOutput(sepWord));
    }

    @Test
    public void testEquivalence() {
        final Random random = new Random(42);
        final int size = 10;

        final SPMM<?, Character, ?, Character> spmm1 =
                RandomAutomata.randomSPMM(random, inputAlphabet, outputAlphabet, size);
        final SPMM<?, Character, ?, Character> spmm2 =
                RandomAutomata.randomSPMM(random, inputAlphabet, outputAlphabet, size);

        Assert.assertTrue(Automata.testEquivalence(spmm1, spmm1, inputAlphabet));
        Assert.assertTrue(Automata.testEquivalence(spmm2, spmm2, inputAlphabet));

        Assert.assertFalse(Automata.testEquivalence(spmm1, spmm2, inputAlphabet));
        Assert.assertFalse(Automata.testEquivalence(spmm2, spmm1, inputAlphabet));
    }

}
