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
package net.automatalib.util.automaton.procedural;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.FastDFAState;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.impl.EmptySBA;
import net.automatalib.automaton.procedural.impl.StackSBA;
import net.automatalib.automaton.procedural.impl.StackSPA;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.graph.ProceduralModalProcessGraph;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.fsa.MutableDFAs;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SBAsTest {

    private final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
    private final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'T');
    private final char returnSymbol = 'R';

    private final ProceduralInputAlphabet<Character> alphabet =
            new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, returnSymbol);
    final ProceduralInputAlphabet<Character> emptyAlphabet =
            new DefaultProceduralInputAlphabet<>(Alphabets.fromArray(), Alphabets.fromArray(), returnSymbol);

    @Test
    public void testATSequences() {
        final Random random = new Random(42);
        final SBA<?, Character> sba = RandomAutomata.randomSBA(random, alphabet, 10);
        final ATSequences<Character> atSequences = SBAs.computeATSequences(sba);

        Assert.assertTrue(atSequences.accessSequences.keySet().containsAll(alphabet.getCallAlphabet()));

        for (Word<Character> as : atSequences.accessSequences.values()) {
            Assert.assertTrue(sba.accepts(as));
        }

        for (Entry<Character, Word<Character>> e : atSequences.terminatingSequences.entrySet()) {
            final Character proc = e.getKey();
            final Word<Character> ts = e.getValue();

            Assert.assertTrue(sba.getProcedure(proc)
                                 .accepts(alphabet.project(ts, 0).append(alphabet.getReturnSymbol())));
            Assert.assertTrue(sba.accepts(Word.fromWords(atSequences.accessSequences.get(proc),
                                                         atSequences.terminatingSequences.get(proc),
                                                         Word.fromLetter(alphabet.getReturnSymbol()))));
        }
    }

    @Test
    public void testEmptyCompleteATRSequences() {
        final SBA<?, Character> sba = new EmptySBA<>(alphabet);
        final ATSequences<Character> atrSequences = SBAs.computeATSequences(sba);

        Assert.assertTrue(atrSequences.accessSequences.isEmpty());
        Assert.assertTrue(atrSequences.terminatingSequences.isEmpty());
    }

    @Test
    public void testDefaultSeparatingWord() {
        final Random random = new Random(42);
        final int size = 10;

        final SBA<?, Character> sba1 = RandomAutomata.randomSBA(random, alphabet, size);
        final SBA<?, Character> sba2 = RandomAutomata.randomSBA(random, alphabet, size);

        Assert.assertNull(SBAs.findSeparatingWord(sba1, sba1, alphabet));
        Assert.assertNull(SBAs.findSeparatingWord(sba2, sba2, alphabet));

        final Word<Character> sepWord1 = SBAs.findSeparatingWord(sba1, sba2, alphabet);
        final Word<Character> sepWord2 = SBAs.findSeparatingWord(sba2, sba1, alphabet);
        Assert.assertNotNull(sepWord1);
        Assert.assertNotNull(sepWord2);
        Assert.assertNotEquals(sba1.computeOutput(sepWord1), sba2.computeOutput(sepWord1));
        Assert.assertNotEquals(sba1.computeOutput(sepWord2), sba2.computeOutput(sepWord2));

        Assert.assertNull(SBAs.findSeparatingWord(sba1, sba1, emptyAlphabet));
        Assert.assertNull(SBAs.findSeparatingWord(sba2, sba2, emptyAlphabet));
        Assert.assertNull(SBAs.findSeparatingWord(sba1, sba2, emptyAlphabet));
        Assert.assertNull(SBAs.findSeparatingWord(sba2, sba1, emptyAlphabet));
    }

    // Copied and adjusted from the corresponding method in the SPAUtil test
    @Test
    public void testIntricateSeparatingWord() {
        // construct a simple (pseudo) palindrome system which we will gradually alter to model different cases
        final CompactDFA<Character> s1 = new CompactDFA<>(alphabet);
        final FastDFA<Character> t1 = new FastDFA<>(alphabet);

        final FastDFAState t1t0 = t1.addInitialState(true);
        final FastDFAState t1t4 = t1.addState(false);

        t1.addTransition(t1t0, 'R', t1t4);

        final SBA<?, Character> sba1 = new StackSBA<>(alphabet, 'S', Map.of('S', s1, 'T', t1));

        final CompactDFA<Character> s2 = new CompactDFA<>(alphabet);
        final FastDFA<Character> t2 = new FastDFA<>(alphabet);

        final FastDFAState t2t0 = t2.addInitialState(true);
        final FastDFAState t2t4 = t2.addState(false);

        t2.addTransition(t2t0, 'R', t2t4);

        final SBA<?, Character> sba2 = new StackSBA<>(alphabet, 'S', Map.of('S', s2, 'T', t2));
        final SBA<?, Character> emptySBA = new EmptySBA<>(alphabet);

        // no accessible procedures, no separating word should exist. Even with the empty SBAs
        Assert.assertNull(SBAs.findSeparatingWord(sba1, sba2, alphabet));
        Assert.assertNull(SBAs.findSeparatingWord(emptySBA, sba2, alphabet));
        Assert.assertNull(SBAs.findSeparatingWord(sba1, emptySBA, alphabet));

        // make SBA1's 'S' procedure not empty. Now there should exist a separating word
        final int s1s0 = s1.addInitialState(true);

        verifySepWord(sba1, sba2, alphabet);
        verifySepWord(sba2, sba1, alphabet);
        verifySepWord(sba1, emptySBA, alphabet);
        verifySepWord(emptySBA, sba1, alphabet);

        // make SBA1's initial procedure accept 'a';
        final int s1s1 = s1.addState(true);
        s1.addTransition(s1s0, 'a', s1s1);

        final int s2s0 = s2.addInitialState(true);
        final int s2s1 = s2.addState(false);
        s2.addTransition(s2s0, 'a', s2s1);

        // There should not exist a separating word if we restrict the alphabet to 'b','c'
        final ProceduralInputAlphabet<Character> bcAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('b', 'c'), callAlphabet, returnSymbol);
        Assert.assertNull(SBAs.findSeparatingWord(sba1, sba2, bcAlphabet));
        Assert.assertNull(SBAs.findSeparatingWord(sba2, sba1, bcAlphabet));

        // only with the empty SBA
        Assert.assertEquals(SBAs.findSeparatingWord(sba1, emptySBA, bcAlphabet), Word.fromLetter('S'));
        Assert.assertEquals(SBAs.findSeparatingWord(emptySBA, sba1, bcAlphabet), Word.fromLetter('S'));
        Assert.assertEquals(SBAs.findSeparatingWord(sba2, emptySBA, bcAlphabet), Word.fromLetter('S'));
        Assert.assertEquals(SBAs.findSeparatingWord(emptySBA, sba2, bcAlphabet), Word.fromLetter('S'));

        // update SBA2 according to SBA1. There should no longer exist a separating word
        s2.setAccepting(s2s1, true);
        Assert.assertNull(SBAs.findSeparatingWord(sba1, sba2, alphabet));
        Assert.assertNull(SBAs.findSeparatingWord(sba2, sba1, alphabet));

        // make SBA1's s5 accept so that we introduce procedure 'T'. This also adds a new separating word (two 'a's)
        final int s1s5 = s1.addState(true);
        s1.addTransition(s1s0, 'T', s1s5);

        verifySepWord(sba1, sba2, alphabet);
        verifySepWord(sba2, sba1, alphabet);

        // update SBA2 accordingly
        final int s2s5 = s2.addState(true);
        s2.addTransition(s2s0, 'T', s2s5);

        Assert.assertNull(SBAs.findSeparatingWord(sba1, sba2, alphabet));
        Assert.assertNull(SBAs.findSeparatingWord(sba2, sba1, alphabet));

        // make SBA1's procedure 'T' accept 'c' so that we can find another separating word
        final FastDFAState t1t1 = t1.addState(true);
        t1.addTransition(t1t0, 'c', t1t1);

        verifySepWord(sba1, sba2, alphabet);
        verifySepWord(sba2, sba1, alphabet);

        // this should also work for partial SBAs
        final SBA<?, Character> partial1 = new StackSBA<>(alphabet, 'S', Map.of('S', s1));
        verifySepWord(sba1, partial1, alphabet);
        verifySepWord(partial1, sba1, alphabet);

        // If we restrict ourselves to only 'S' call symbols, a separating word should no longer exist
        final ProceduralInputAlphabet<Character> sAlphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, Alphabets.singleton('S'), returnSymbol);
        Assert.assertNull(SBAs.findSeparatingWord(sba1, sba2, sAlphabet));
        Assert.assertNull(SBAs.findSeparatingWord(sba2, sba1, sAlphabet));

        // update SBA2 accordingly
        final FastDFAState t2t1 = t2.addState(true);
        t2.addTransition(t2t0, 'c', t2t1);

        Assert.assertNull(SBAs.findSeparatingWord(sba1, sba2, alphabet));
        Assert.assertNull(SBAs.findSeparatingWord(sba2, sba1, alphabet));

        // make SBA1's 'T' procedure return on c.
        // This should yield a separating word even if we restrict ourselves to only 'c' as internal symbol
        t1.setAccepting(t1t4, true);

        final ProceduralInputAlphabet<Character> cAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.singleton('c'), callAlphabet, returnSymbol);
        verifySepWord(sba1, sba2, alphabet);
        verifySepWord(sba2, sba1, alphabet);
        verifySepWord(sba1, sba2, cAlphabet);
        verifySepWord(sba2, sba1, cAlphabet);
    }

    private static <I> void verifySepWord(SBA<?, I> sba1, SBA<?, I> sba2, ProceduralInputAlphabet<I> alphabet) {
        final Word<I> sepWord = SBAs.findSeparatingWord(sba1, sba2, alphabet);
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(sba1.accepts(sepWord), sba2.accepts(sepWord));
    }

    @Test
    public void testEquivalence() {
        final Random random = new Random(42);
        final int size = 10;

        final SBA<?, Character> sba1 = RandomAutomata.randomSBA(random, alphabet, size);
        final SBA<?, Character> sba2 = RandomAutomata.randomSBA(random, alphabet, size);

        Assert.assertTrue(SBAs.testEquivalence(sba1, sba1, alphabet));
        Assert.assertTrue(SBAs.testEquivalence(sba2, sba2, alphabet));

        Assert.assertFalse(SBAs.testEquivalence(sba1, sba2, alphabet));
        Assert.assertFalse(SBAs.testEquivalence(sba2, sba1, alphabet));
    }

    @Test
    public void testReduction() {
        // construct palindrome SBA and SPA
        final CompactDFA<Character> sbaS = new CompactDFA<>(alphabet);
        final FastDFA<Character> sbaT = new FastDFA<>(alphabet);
        final CompactDFA<Character> spaS = new CompactDFA<>(alphabet.getProceduralAlphabet());
        final FastDFA<Character> spaT = new FastDFA<>(alphabet.getProceduralAlphabet());

        fillS(sbaS, true);
        fillT(sbaT, true);
        fillS(spaS, false);
        fillT(spaT, false);

        MutableDFAs.complete(sbaS, alphabet);
        MutableDFAs.complete(sbaT, alphabet);
        MutableDFAs.complete(spaS, alphabet.getProceduralAlphabet());
        MutableDFAs.complete(spaT, alphabet.getProceduralAlphabet());

        final StackSBA<?, Character> sba = new StackSBA<>(alphabet, 'S', Map.of('S', sbaS, 'T', sbaT));
        final StackSPA<?, Character> spa = new StackSPA<>(alphabet, 'S', Map.of('S', spaS, 'T', spaT));
        final SPA<?, Character> reduced = SBAs.reduce(sba);

        Assert.assertTrue(SPAs.testEquivalence(spa, reduced, alphabet));
    }

    @Test
    public void testSBAasCFMPS() throws IOException {
        final SBA<?, String> sba = buildSBAWithNonTerminatingProcedures();
        final ContextFreeModalProcessSystem<String, Void> cfmps = SBAs.toCFMPS(sba);

        Assert.assertEquals(cfmps.getMainProcess(), "P1");

        final Map<String, ProceduralModalProcessGraph<?, String, ?, Void, ?>> pmpgs = cfmps.getPMPGs();
        Assert.assertEquals(pmpgs.size(), 4);

        final ProceduralModalProcessGraph<?, String, ?, Void, ?> p1 = pmpgs.get("P1");
        Assert.assertNotNull(p1);
        Assert.assertEquals(p1.getNodes().size(), 5);

        final ProceduralModalProcessGraph<?, String, ?, Void, ?> p2 = pmpgs.get("P2");
        Assert.assertNotNull(p2);
        Assert.assertEquals(p2.getNodes().size(), 5);

        final ProceduralModalProcessGraph<?, String, ?, Void, ?> p3 = pmpgs.get("P3");
        Assert.assertNotNull(p3);
        Assert.assertEquals(p3.getNodes().size(), 5);

        final ProceduralModalProcessGraph<?, String, ?, Void, ?> p4 = pmpgs.get("P4");
        Assert.assertNotNull(p4);
        Assert.assertEquals(p4.getNodes().size(), 3);

        SPAsTest.verifyDot(cfmps, "/cfmps/sba.dot");
    }

    private static <S> void fillS(MutableDFA<S, Character> dfa, boolean sba) {
        final S s0 = dfa.addInitialState(true);
        final S s1 = dfa.addState(true);
        final S s2 = dfa.addState(true);
        final S s3 = dfa.addState(sba);
        final S s4 = dfa.addState(sba);
        final S s5 = dfa.addState(true);

        dfa.addTransition(s0, 'T', s5);
        dfa.addTransition(s0, 'a', s1);
        dfa.addTransition(s0, 'b', s2);
        dfa.addTransition(s1, 'S', s3);
        dfa.addTransition(s2, 'S', s4);
        dfa.addTransition(s3, 'a', s5);
        dfa.addTransition(s4, 'b', s5);

        if (sba) {
            final S s6 = dfa.addState(true);
            dfa.addTransition(s0, 'R', s6);
            dfa.addTransition(s1, 'R', s6);
            dfa.addTransition(s2, 'R', s6);
            dfa.addTransition(s5, 'R', s6);
        }
    }

    private static <S> void fillT(MutableDFA<S, Character> dfa, boolean sba) {
        final S t0 = dfa.addInitialState(sba);
        final S t1 = dfa.addState(true);
        final S t2 = dfa.addState(sba);
        final S t3 = dfa.addState(true);

        dfa.addTransition(t0, 'c', t1);
        dfa.addTransition(t1, 'T', t2);
        dfa.addTransition(t2, 'c', t3);

        if (sba) {
            final S t4 = dfa.addState(true);
            dfa.addTransition(t1, 'R', t4);
            dfa.addTransition(t3, 'R', t4);
        }
    }

    private static SBA<?, String> buildSBAWithNonTerminatingProcedures() {
        final Alphabet<String> internalAlphabet = Alphabets.closedCharStringRange('a', 'd');
        final Alphabet<String> callAlphabet = Alphabets.fromArray("P1", "P2", "P3", "P4");
        final ProceduralInputAlphabet<String> alphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, "R");

        // @formatter:off
        final CompactDFA<String> p1 = AutomatonBuilders.forDFA(new CompactDFA<>(alphabet))
                                                       .withInitial("s0")
                                                       .from("s0").on("P2").to("s1")
                                                       .from("s0").on("a").to("s1")
                                                       .from("s1").on("R").to("s2")
                                                       .withAccepting("s0", "s1", "s2")
                                                       .create();
        final FastDFA<String> p2 = AutomatonBuilders.forDFA(new FastDFA<>(alphabet))
                                                    .withInitial("t0")
                                                    .from("t0").on("b").to("t1")
                                                    .from("t0").on("P3").to("t2")
                                                    .from("t0").on("P4").to("t2")
                                                    .from("t1").on("R").to("t2")
                                                    .withAccepting("t0", "t1", "t2")
                                                    .create();
        final FastDFA<String> p3 = AutomatonBuilders.forDFA(new FastDFA<>(alphabet))
                                                    .withInitial("t0")
                                                    .from("t0").on("c").to("t1")
                                                    .from("t0").on("P4").to("t2")
                                                    .from("t1").on("d").to("t2")
                                                    .withAccepting("t0", "t1", "t2")
                                                    .create();
        final CompactDFA<String> p4 = AutomatonBuilders.forDFA(new CompactDFA<>(alphabet))
                                                       .withInitial("t0")
                                                       .withAccepting("t0")
                                                       .create();
        // @formatter:on

        MutableDFAs.complete(p3, alphabet, true);

        return new StackSBA<>(alphabet, "P1", Map.of("P1", p1, "P2", p2, "P3", p3, "P4", p4));
    }

}
