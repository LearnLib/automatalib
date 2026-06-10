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
package net.automatalib.util.automaton.procedural;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.ProceduralOutputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.impl.DefaultProceduralOutputAlphabet;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.procedural.impl.EmptySPMM;
import net.automatalib.automaton.procedural.impl.StackSPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.transducer.impl.FastMealy;
import net.automatalib.automaton.transducer.impl.FastMealyState;
import net.automatalib.common.util.Pair;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.util.automaton.transducer.MutableMealyMachines;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SPMMsTest {

    private static final Alphabet<Character> INTERNAL_ALPHABET = Alphabets.characters('a', 'c');
    private static final Alphabet<Character> CALL_ALPHABET = Alphabets.characters('S', 'T');
    private static final char RETURN_SYMBOL = 'R';

    private static final Character SUCCESS_OUTPUT = '✓';
    private static final Character ERROR_OUTPUT = '-';

    private static final ProceduralInputAlphabet<Character> INPUT_ALPHABET =
            new DefaultProceduralInputAlphabet<>(INTERNAL_ALPHABET, CALL_ALPHABET, RETURN_SYMBOL);
    static final ProceduralInputAlphabet<Character> EMPTY_ALPHABET =
            new DefaultProceduralInputAlphabet<>(Alphabets.fromArray(), Alphabets.fromArray(), RETURN_SYMBOL);
    private static final ProceduralOutputAlphabet<Character> OUTPUT_ALPHABET =
            new DefaultProceduralOutputAlphabet<>(Alphabets.characters('x', 'z'), ERROR_OUTPUT);

    @Test
    public void testATSequences() {
        final Random random = new Random(42);
        final SPMM<?, Character, ?, Character> spmm =
                RandomAutomata.randomSPMM(random, INPUT_ALPHABET, OUTPUT_ALPHABET, 10);
        final ATSequences<Character> atSequences = SPMMs.computeATSequences(spmm);

        Assert.assertTrue(atSequences.accessSequences.keySet().containsAll(INPUT_ALPHABET.getCallAlphabet()));

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
                    Word.fromWords(as, ts, Word.fromLetter(INPUT_ALPHABET.getReturnSymbol()));
            final Word<Character> globalOutput = spmm.computeOutput(globalInput);

            final Pair<Word<Character>, Word<Character>> localOutput =
                    INPUT_ALPHABET.project(globalInput, globalOutput, as.size());

            Assert.assertEquals(procedure.computeOutput(localOutput.getFirst()), localOutput.getSecond());
            Assert.assertFalse(spmm.isErrorOutput(localOutput.getSecond().lastSymbol()));
            Assert.assertFalse(spmm.isErrorOutput(globalOutput.lastSymbol()));
        }
    }

    @Test
    public void testEmptyCompleteATRSequences() {
        final SPMM<?, Character, ?, Character> spmm = new EmptySPMM<>(INPUT_ALPHABET, OUTPUT_ALPHABET.getErrorSymbol());
        final ATSequences<Character> atrSequences = SPMMs.computeATSequences(spmm);

        Assert.assertTrue(atrSequences.accessSequences.isEmpty());
        Assert.assertTrue(atrSequences.terminatingSequences.isEmpty());
    }

    @Test
    public void testDefaultSeparatingWord() {
        final Random random = new Random(42);
        final int size = 10;

        final SPMM<?, Character, ?, Character> spmm1 =
                RandomAutomata.randomSPMM(random, INPUT_ALPHABET, OUTPUT_ALPHABET, size);
        final SPMM<?, Character, ?, Character> spmm2 =
                RandomAutomata.randomSPMM(random, INPUT_ALPHABET, OUTPUT_ALPHABET, size);

        Assert.assertNull(SPMMs.findSeparatingWord(spmm1, spmm1, INPUT_ALPHABET));
        Assert.assertNull(SPMMs.findSeparatingWord(spmm2, spmm2, INPUT_ALPHABET));

        final Word<Character> sepWord1 = SPMMs.findSeparatingWord(spmm1, spmm2, INPUT_ALPHABET);
        final Word<Character> sepWord2 = SPMMs.findSeparatingWord(spmm2, spmm1, INPUT_ALPHABET);
        Assert.assertNotNull(sepWord1);
        Assert.assertNotNull(sepWord2);
        Assert.assertNotEquals(spmm1.computeOutput(sepWord1), spmm2.computeOutput(sepWord1));
        Assert.assertNotEquals(spmm1.computeOutput(sepWord2), spmm2.computeOutput(sepWord2));

        Assert.assertNull(SPMMs.findSeparatingWord(spmm1, spmm1, EMPTY_ALPHABET));
        Assert.assertNull(SPMMs.findSeparatingWord(spmm2, spmm2, EMPTY_ALPHABET));
        Assert.assertNull(SPMMs.findSeparatingWord(spmm1, spmm2, EMPTY_ALPHABET));
        Assert.assertNull(SPMMs.findSeparatingWord(spmm2, spmm1, EMPTY_ALPHABET));
    }

    // Copied and adjusted from the corresponding method in the SPAUtil test
    @Test
    public void testIntricateSeparatingWord() {
        final Character errorOutput = OUTPUT_ALPHABET.getErrorSymbol();

        // construct a simple (pseudo) palindrome system which we will gradually alter to model different cases
        final CompactMealy<Character, Character> s1 = new CompactMealy<>(INPUT_ALPHABET);
        final FastMealy<Character, Character> t1 = new FastMealy<>(INPUT_ALPHABET);

        final FastMealyState<Character> t1t0 = t1.addInitialState();
        final FastMealyState<Character> t1t4 = t1.addState();

        t1.addTransition(t1t0, 'R', t1t4, ERROR_OUTPUT);

        final SPMM<?, Character, ?, Character> spmm1 =
                new StackSPMM<>(INPUT_ALPHABET, 'S', SUCCESS_OUTPUT, errorOutput, Map.of('S', s1, 'T', t1));

        final CompactMealy<Character, Character> s2 = new CompactMealy<>(INPUT_ALPHABET);
        final FastMealy<Character, Character> t2 = new FastMealy<>(INPUT_ALPHABET);

        final FastMealyState<Character> t2t0 = t2.addInitialState();
        final FastMealyState<Character> t2t4 = t2.addState();

        t2.addTransition(t2t0, 'R', t2t4, ERROR_OUTPUT);

        final SPMM<?, Character, ?, Character> spmm2 =
                new StackSPMM<>(INPUT_ALPHABET, 'S', SUCCESS_OUTPUT, errorOutput, Map.of('S', s2, 'T', t2));
        final SPMM<?, Character, ?, Character> emptySPMM = new EmptySPMM<>(INPUT_ALPHABET, errorOutput);

        // no accessible procedures, no separating word should exist. Even with the empty SPMMs
        Assert.assertNull(SPMMs.findSeparatingWord(spmm1, spmm2, INPUT_ALPHABET));
        Assert.assertNull(SPMMs.findSeparatingWord(emptySPMM, spmm2, INPUT_ALPHABET));
        Assert.assertNull(SPMMs.findSeparatingWord(spmm1, emptySPMM, INPUT_ALPHABET));

        // make SPMM1's 'S' procedure not empty. Now there should exist a separating word
        final int s1s0 = s1.addInitialState();

        verifySepWord(spmm1, spmm2, INPUT_ALPHABET);
        verifySepWord(spmm2, spmm1, INPUT_ALPHABET);
        verifySepWord(spmm1, emptySPMM, INPUT_ALPHABET);
        verifySepWord(emptySPMM, spmm1, INPUT_ALPHABET);

        // make SPMM1's initial procedure transduce 'a';
        final int s1s1 = s1.addState();
        s1.addTransition(s1s0, 'a', s1s1, 'x');

        final int s2s0 = s2.addInitialState();
        final int s2s1 = s2.addState();
        s2.addTransition(s2s0, 'a', s2s1, ERROR_OUTPUT);

        // There should not exist a separating word if we restrict the alphabet to 'b','c'
        final ProceduralInputAlphabet<Character> bcAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('b', 'c'), CALL_ALPHABET, RETURN_SYMBOL);
        Assert.assertNull(SPMMs.findSeparatingWord(spmm1, spmm2, bcAlphabet));
        Assert.assertNull(SPMMs.findSeparatingWord(spmm2, spmm1, bcAlphabet));

        // only with the empty SPMM
        Assert.assertEquals(SPMMs.findSeparatingWord(spmm1, emptySPMM, bcAlphabet), Word.fromLetter('S'));
        Assert.assertEquals(SPMMs.findSeparatingWord(emptySPMM, spmm1, bcAlphabet), Word.fromLetter('S'));
        Assert.assertEquals(SPMMs.findSeparatingWord(spmm2, emptySPMM, bcAlphabet), Word.fromLetter('S'));
        Assert.assertEquals(SPMMs.findSeparatingWord(emptySPMM, spmm2, bcAlphabet), Word.fromLetter('S'));

        // update SPMM2 according to SPMM1. There should no longer exist a separating word
        s2.setTransition(s2s0, (Character) 'a', s2s1, (Character) 'x');
        Assert.assertNull(SPMMs.findSeparatingWord(spmm1, spmm2, INPUT_ALPHABET));
        Assert.assertNull(SPMMs.findSeparatingWord(spmm2, spmm1, INPUT_ALPHABET));

        // make SPMM1's s5 accept so that we introduce procedure 'T'. This also adds a new separating word (two 'a's)
        final int s1s5 = s1.addState();
        s1.addTransition(s1s0, 'T', s1s5, SUCCESS_OUTPUT);

        verifySepWord(spmm1, spmm2, INPUT_ALPHABET);
        verifySepWord(spmm2, spmm1, INPUT_ALPHABET);

        // update SPMM2 accordingly
        final int s2s5 = s2.addState();
        s2.addTransition(s2s0, 'T', s2s5, SUCCESS_OUTPUT);

        Assert.assertNull(SPMMs.findSeparatingWord(spmm1, spmm2, INPUT_ALPHABET));
        Assert.assertNull(SPMMs.findSeparatingWord(spmm2, spmm1, INPUT_ALPHABET));

        // make SPMM1's procedure 'T' transduce on 'c' so that we can find another separating word
        final FastMealyState<Character> t1t1 = t1.addState();
        t1.addTransition(t1t0, 'c', t1t1, 'z');

        verifySepWord(spmm1, spmm2, INPUT_ALPHABET);
        verifySepWord(spmm2, spmm1, INPUT_ALPHABET);

        // this should also work for partial SPMMs
        final SPMM<?, Character, ?, Character> partial1 =
                new StackSPMM<>(INPUT_ALPHABET, 'S', SUCCESS_OUTPUT, errorOutput, Map.of('S', s1));
        verifySepWord(spmm1, partial1, INPUT_ALPHABET);
        verifySepWord(partial1, spmm1, INPUT_ALPHABET);

        // If we restrict ourselves to only 'S' call symbols, a separating word should no longer exist
        final ProceduralInputAlphabet<Character> sAlphabet =
                new DefaultProceduralInputAlphabet<>(INTERNAL_ALPHABET, Alphabets.singleton('S'), RETURN_SYMBOL);
        Assert.assertNull(SPMMs.findSeparatingWord(spmm1, spmm2, sAlphabet));
        Assert.assertNull(SPMMs.findSeparatingWord(spmm2, spmm1, sAlphabet));

        // update SPMM2 accordingly
        final FastMealyState<Character> t2t1 = t2.addState();
        t2.addTransition(t2t0, 'c', t2t1, 'z');

        Assert.assertNull(SPMMs.findSeparatingWord(spmm1, spmm2, INPUT_ALPHABET));
        Assert.assertNull(SPMMs.findSeparatingWord(spmm2, spmm1, INPUT_ALPHABET));

        // make SPMM1's 'T' procedure return on c.
        // This should yield a separating word even if we restrict ourselves to only 'c' as internal symbol
        t1.setTransition(t1t1, 'R', t1t4, SUCCESS_OUTPUT);

        final ProceduralInputAlphabet<Character> cAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.singleton('c'), CALL_ALPHABET, RETURN_SYMBOL);
        verifySepWord(spmm1, spmm2, INPUT_ALPHABET);
        verifySepWord(spmm2, spmm1, INPUT_ALPHABET);
        verifySepWord(spmm1, spmm2, cAlphabet);
        verifySepWord(spmm2, spmm1, cAlphabet);
    }

    @Test
    public void testEquivalence() {
        final Random random = new Random(42);
        final int size = 10;

        final SPMM<?, Character, ?, Character> spmm1 =
                RandomAutomata.randomSPMM(random, INPUT_ALPHABET, OUTPUT_ALPHABET, size);
        final SPMM<?, Character, ?, Character> spmm2 =
                RandomAutomata.randomSPMM(random, INPUT_ALPHABET, OUTPUT_ALPHABET, size);

        Assert.assertTrue(SPMMs.testEquivalence(spmm1, spmm1, INPUT_ALPHABET));
        Assert.assertTrue(SPMMs.testEquivalence(spmm2, spmm2, INPUT_ALPHABET));

        Assert.assertFalse(SPMMs.testEquivalence(spmm1, spmm2, INPUT_ALPHABET));
        Assert.assertFalse(SPMMs.testEquivalence(spmm2, spmm1, INPUT_ALPHABET));
    }

    @Test
    public void testLocalSepWordWithoutTerminatingSequence() {

        final SPMM<?, Character, ?, Character> spmm1 = buildPalindromeSystem();
        final SPMM<?, Character, ?, Character> spmm2 = buildHypothesisSystem();

        /*
         * This case previously uncovered an issue where the equivalence check would detect a separating word "T R"
         * in procedure S which caused an NPE because T does not have a terminating sequence.
         */

        Assert.assertTrue(SPMMs.testEquivalence(spmm1, spmm1, INPUT_ALPHABET));
        Assert.assertTrue(SPMMs.testEquivalence(spmm2, spmm2, INPUT_ALPHABET));

        Assert.assertFalse(SPMMs.testEquivalence(spmm1, spmm2, INPUT_ALPHABET));
        Assert.assertFalse(SPMMs.testEquivalence(spmm2, spmm1, INPUT_ALPHABET));
    }

    private static <I, O> void verifySepWord(SPMM<?, I, ?, O> spmm1,
                                             SPMM<?, I, ?, O> spmm2,
                                             ProceduralInputAlphabet<I> alphabet) {
        final Word<I> sepWord = SPMMs.findSeparatingWord(spmm1, spmm2, alphabet);
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(spmm1.computeOutput(sepWord), spmm2.computeOutput(sepWord));
    }

    private static SPMM<?, Character, ?, Character> buildPalindromeSystem() {

        final CompactMealy<Character, Character> sProcedure = new CompactMealy<>(INPUT_ALPHABET);
        final FastMealy<Character, Character> tProcedure = new FastMealy<>(INPUT_ALPHABET);

        // @formatter:off
        AutomatonBuilders.forMealy(sProcedure)
                         .withInitial("s0")
                         .from("s0").on('T').withOutput(SUCCESS_OUTPUT).to("s5")
                         .from("s0").on('a').withOutput('x').to("s1")
                         .from("s0").on('b').withOutput('y').to("s2")
                         .from("s0").on('R').withOutput(SUCCESS_OUTPUT).to("s6")
                         .from("s1").on('S').withOutput(SUCCESS_OUTPUT).to("s3")
                         .from("s1").on('R').withOutput(SUCCESS_OUTPUT).to("s6")
                         .from("s2").on('S').withOutput(SUCCESS_OUTPUT).to("s4")
                         .from("s2").on('R').withOutput(SUCCESS_OUTPUT).to("s6")
                         .from("s3").on('a').withOutput('x').to("s5")
                         .from("s4").on('b').withOutput('y').to("s5")
                         .from("s5").on('R').withOutput(SUCCESS_OUTPUT).to("s6")
                         .create();

        AutomatonBuilders.forMealy(tProcedure)
                         .withInitial("t0")
                         .from("t0").on('S').withOutput(SUCCESS_OUTPUT).to("t3")
                         .from("t0").on('c').withOutput('z').to("t1")
                         .from("t1").on('T').withOutput(SUCCESS_OUTPUT).to("t2")
                         .from("t1").on('R').withOutput(SUCCESS_OUTPUT).to("t4")
                         .from("t2").on('c').withOutput('z').to("t3")
                         .from("t3").on('R').withOutput(SUCCESS_OUTPUT).to("t4")
                         .create();
        // @formatter:on

        MutableMealyMachines.complete(sProcedure, SPMMsTest.INPUT_ALPHABET, ERROR_OUTPUT, true);
        MutableMealyMachines.complete(tProcedure, SPMMsTest.INPUT_ALPHABET, ERROR_OUTPUT, true);

        final Map<Character, MealyMachine<?, Character, ?, Character>> subModels = new HashMap<>();
        subModels.put('S', sProcedure);
        subModels.put('T', tProcedure);

        return new StackSPMM<>(INPUT_ALPHABET, 'S', SUCCESS_OUTPUT, ERROR_OUTPUT, subModels);
    }

    private static SPMM<?, Character, ?, Character> buildHypothesisSystem() {

        final CompactMealy<Character, Character> sProcedure = new CompactMealy<>(INPUT_ALPHABET);
        final FastMealy<Character, Character> tProcedure = new FastMealy<>(INPUT_ALPHABET);

        // @formatter:off
        AutomatonBuilders.forMealy(sProcedure)
                         .withInitial("s0")
                         .from("s0").on('S', 'c').withOutput(ERROR_OUTPUT).to("s2")
                         .from("s0").on('T', 'R').withOutput(SUCCESS_OUTPUT).to("s2")
                         .from("s0").on('a').withOutput('x').to("s1")
                         .from("s0").on('b').withOutput('y').to("s1")
                         .from("s1").on('S').withOutput(SUCCESS_OUTPUT).to("s0")
                         .from("s1").on('a', 'b', 'c', 'T').withOutput(ERROR_OUTPUT).to("s2")
                         .from("s1").on('R').withOutput(SUCCESS_OUTPUT).to("s2")
                         .from("s2").on('a', 'b', 'c', 'S', 'T', 'R').withOutput(ERROR_OUTPUT).loop()
                         .create();
        // @formatter:on

        MutableMealyMachines.complete(sProcedure, INPUT_ALPHABET, ERROR_OUTPUT, true);
        MutableMealyMachines.complete(tProcedure, INPUT_ALPHABET, ERROR_OUTPUT, true);

        final Map<Character, MealyMachine<?, Character, ?, Character>> subModels = new HashMap<>();
        subModels.put('S', sProcedure);
        subModels.put('T', tProcedure);

        return new StackSPMM<>(INPUT_ALPHABET, 'S', SUCCESS_OUTPUT, ERROR_OUTPUT, subModels);
    }

}
