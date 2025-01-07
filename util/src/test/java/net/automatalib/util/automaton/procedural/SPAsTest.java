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
import java.io.Reader;
import java.io.StringWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.FastDFAState;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.impl.EmptySPA;
import net.automatalib.automaton.procedural.impl.StackSPA;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.automaton.vpa.SEVPA;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.graph.ProceduralModalProcessGraph;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.conformance.SPATestsIterator;
import net.automatalib.util.automaton.conformance.WMethodTestsIterator;
import net.automatalib.util.automaton.fsa.MutableDFAs;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SPAsTest {

    private final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
    private final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'T');
    private final char returnSymbol = 'R';

    private final ProceduralInputAlphabet<Character> alphabet =
            new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, returnSymbol);
    final ProceduralInputAlphabet<Character> emptyAlphabet =
            new DefaultProceduralInputAlphabet<>(Alphabets.fromArray(), Alphabets.fromArray(), returnSymbol);

    @Test
    public void testIncompleteATRSequences() {
        // construct a simple (pseudo) palindrome system which we will gradually alter to model different cases
        final CompactDFA<Character> s = new CompactDFA<>(alphabet.getProceduralAlphabet());
        final FastDFA<Character> t = new FastDFA<>(alphabet.getProceduralAlphabet());

        final int s0 = s.addInitialState(false);
        final int s1 = s.addState(false);
        final int s2 = s.addState(false);
        final int s3 = s.addState(false);
        final int s4 = s.addState(false);
        final int s5 = s.addState(false);

        s.addTransition(s0, 'T', s5);
        s.addTransition(s0, 'a', s1);
        s.addTransition(s0, 'b', s2);
        s.addTransition(s1, 'S', s3);
        s.addTransition(s2, 'S', s4);
        s.addTransition(s3, 'a', s5);
        s.addTransition(s4, 'b', s5);

        final FastDFAState t0 = t.addInitialState(false);
        final FastDFAState t1 = t.addState(false);
        final FastDFAState t2 = t.addState(false);
        final FastDFAState t3 = t.addState(false);

        t.addTransition(t0, 'c', t1);
        t.addTransition(t1, 'T', t2);
        t.addTransition(t2, 'c', t3);

        final SimpleEntry<Character, Word<Character>> sAsEntry = new SimpleEntry<>('S', Word.fromLetter('S'));
        final SimpleEntry<Character, Word<Character>> sTsEntry = new SimpleEntry<>('S', Word.epsilon());
        final SimpleEntry<Character, Word<Character>> sRsEntry = new SimpleEntry<>('S', Word.fromLetter('R'));
        final SimpleEntry<Character, Word<Character>> tAsEntry = new SimpleEntry<>('T', Word.fromString("ST"));
        final SimpleEntry<Character, Word<Character>> tTsEntry = new SimpleEntry<>('T', Word.fromString("c"));
        final SimpleEntry<Character, Word<Character>> tRsEntry = new SimpleEntry<>('T', Word.fromString("RR"));

        final ProceduralInputAlphabet<Character> halfAlphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, Alphabets.singleton('S'), returnSymbol);

        // With no accepting states, there exist no a/t/r sequences.
        final SPA<?, Character> spa = new StackSPA<>(alphabet, 'S', Map.of('S', s, 'T', t));
        ATRSequences<Character> atrSequences = SPAs.computeATRSequences(spa, alphabet);

        Assert.assertEquals(atrSequences.accessSequences, Map.ofEntries(sAsEntry));
        Assert.assertTrue(atrSequences.terminatingSequences.isEmpty());
        Assert.assertEquals(atrSequences.returnSequences, Map.ofEntries(sRsEntry));
        Assert.assertFalse(SPAs.isMinimal(spa));
        Assert.assertTrue(SPAs.isMinimal(emptyAlphabet, atrSequences));

        // Now we make the initial state of S accepting
        // This should give us a terminating sequence for S but still no sequences for T.
        s.setAccepting(s0, true);

        atrSequences = SPAs.computeATRSequences(spa, alphabet);

        Assert.assertEquals(atrSequences.accessSequences, Map.ofEntries(sAsEntry));
        Assert.assertEquals(atrSequences.terminatingSequences, Map.ofEntries(sTsEntry));
        Assert.assertEquals(atrSequences.returnSequences, Map.ofEntries(sRsEntry));
        Assert.assertFalse(SPAs.isMinimal(spa));
        Assert.assertTrue(SPAs.isMinimal(halfAlphabet, atrSequences));
        Assert.assertTrue(SPAs.isMinimal(emptyAlphabet, atrSequences));

        // Now we make s5 of S accepting.
        // This gives us a terminating sequence that traverses T and therefore allows us to extract access and terminating sequences for T.
        s.setAccepting(s5, true);

        atrSequences = SPAs.computeATRSequences(spa, alphabet);

        Assert.assertEquals(atrSequences.accessSequences, Map.ofEntries(sAsEntry, tAsEntry));
        Assert.assertEquals(atrSequences.terminatingSequences, Map.ofEntries(sTsEntry));
        Assert.assertEquals(atrSequences.returnSequences, Map.ofEntries(sRsEntry, tRsEntry));
        Assert.assertFalse(SPAs.isMinimal(spa));
        Assert.assertTrue(SPAs.isMinimal(halfAlphabet, atrSequences));
        Assert.assertTrue(SPAs.isMinimal(emptyAlphabet, atrSequences));

        // Now make t3 of T accepting.
        // The only path to an accepting state contains a recursive call to T, so we still cannot extract a valid terminating sequence.
        t.setAccepting(t3, true);

        atrSequences = SPAs.computeATRSequences(spa, alphabet);

        Assert.assertEquals(atrSequences.accessSequences, Map.ofEntries(sAsEntry, tAsEntry));
        Assert.assertEquals(atrSequences.terminatingSequences, Map.ofEntries(sTsEntry));
        Assert.assertEquals(atrSequences.returnSequences, Map.ofEntries(sRsEntry, tRsEntry));
        Assert.assertFalse(SPAs.isMinimal(spa));
        Assert.assertTrue(SPAs.isMinimal(halfAlphabet, atrSequences));
        Assert.assertTrue(SPAs.isMinimal(emptyAlphabet, atrSequences));

        // Now make t1 of T accepting.
        // This allows us to construct a valid terminating sequence for T and therefore make the global ATRSequences valid.
        t.setAccepting(t1, true);

        atrSequences = SPAs.computeATRSequences(spa, alphabet);

        Assert.assertEquals(atrSequences.accessSequences, Map.ofEntries(sAsEntry, tAsEntry));
        Assert.assertEquals(atrSequences.terminatingSequences, Map.ofEntries(sTsEntry, tTsEntry));
        Assert.assertEquals(atrSequences.returnSequences, Map.ofEntries(sRsEntry, tRsEntry));
        Assert.assertTrue(SPAs.isMinimal(spa));
    }

    @Test
    public void testPartialATRSequences() {
        final Random random = new Random(42);
        final SPA<?, Character> spa = RandomAutomata.randomSPA(random, alphabet, 10);
        final ProceduralInputAlphabet<Character> halfAlphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, Alphabets.singleton('S'), returnSymbol);

        final ATRSequences<Character> atrSequences = SPAs.computeATRSequences(spa, halfAlphabet);

        Assert.assertEquals(atrSequences.accessSequences.keySet(), Collections.singleton('S'));
        Assert.assertEquals(atrSequences.terminatingSequences.keySet(), Collections.singleton('S'));
        Assert.assertEquals(atrSequences.returnSequences.keySet(), Collections.singleton('S'));

        Assert.assertTrue(SPAs.isMinimal(spa, halfAlphabet));
        Assert.assertFalse(SPAs.isMinimal(alphabet, atrSequences));
    }

    @Test
    public void testEmptyPartialATRSequences() {
        final SPA<?, Character> spa = new EmptySPA<>(alphabet);
        final ProceduralInputAlphabet<Character> halfAlphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, Alphabets.singleton('S'), returnSymbol);

        final ATRSequences<Character> atrSequences = SPAs.computeATRSequences(spa, halfAlphabet);

        Assert.assertTrue(atrSequences.accessSequences.isEmpty());
        Assert.assertTrue(atrSequences.terminatingSequences.isEmpty());
        Assert.assertTrue(atrSequences.returnSequences.isEmpty());

        Assert.assertFalse(SPAs.isMinimal(spa, halfAlphabet));
        Assert.assertFalse(SPAs.isMinimal(alphabet, atrSequences));
    }

    @Test
    public void testCompleteATRSequences() {
        final Random random = new Random(42);
        final SPA<?, Character> spa = RandomAutomata.randomSPA(random, alphabet, 10);
        final ATRSequences<Character> atrSequences = SPAs.computeATRSequences(spa);

        Assert.assertTrue(atrSequences.accessSequences.keySet().containsAll(alphabet.getCallAlphabet()));
        Assert.assertTrue(atrSequences.terminatingSequences.keySet().containsAll(alphabet.getCallAlphabet()));
        Assert.assertTrue(atrSequences.returnSequences.keySet().containsAll(alphabet.getCallAlphabet()));
        Assert.assertTrue(SPAs.isMinimal(spa));
    }

    @Test
    public void testEmptyCompleteATRSequences() {
        final SPA<?, Character> spa = new EmptySPA<>(alphabet);
        final ATRSequences<Character> atrSequences = SPAs.computeATRSequences(spa);

        Assert.assertTrue(atrSequences.accessSequences.isEmpty());
        Assert.assertTrue(atrSequences.terminatingSequences.isEmpty());
        Assert.assertTrue(atrSequences.returnSequences.isEmpty());
        Assert.assertFalse(SPAs.isMinimal(spa));
    }

    @Test
    public void testMissingAccessReturnSequenceDueToMissingTerminatingSequence() {
        final CompactDFA<Character> s = new CompactDFA<>(alphabet.getProceduralAlphabet());
        final FastDFA<Character> t = new FastDFA<>(alphabet.getProceduralAlphabet());

        final int s0 = s.addInitialState(false);
        final int s1 = s.addState(false);
        final int s2 = s.addState(false);
        final int s3 = s.addState(true);

        s.addTransition(s0, 'T', s1);
        s.addTransition(s1, 'c', s2);
        s.addTransition(s2, 'T', s3);

        t.addInitialState(false);

        final SPA<?, Character> spa = new StackSPA<>(alphabet, 'S', Map.of('S', s, 'T', t));

        final ATRSequences<Character> atrSequences = SPAs.computeATRSequences(spa);
        Assert.assertEquals(atrSequences.accessSequences.keySet(), Collections.singleton('S'));
        Assert.assertTrue(atrSequences.terminatingSequences.isEmpty());
        Assert.assertEquals(atrSequences.returnSequences.keySet(), Collections.singleton('S'));
    }

    @Test
    public void testDefaultSeparatingWord() {
        final Random random = new Random(42);
        final int size = 10;

        final SPA<?, Character> spa1 = RandomAutomata.randomSPA(random, alphabet, size);
        final SPA<?, Character> spa2 = RandomAutomata.randomSPA(random, alphabet, size);

        Assert.assertNull(SPAs.findSeparatingWord(spa1, spa1, alphabet));
        Assert.assertNull(SPAs.findSeparatingWord(spa2, spa2, alphabet));

        final Word<Character> sepWord1 = SPAs.findSeparatingWord(spa1, spa2, alphabet);
        final Word<Character> sepWord2 = SPAs.findSeparatingWord(spa2, spa1, alphabet);
        Assert.assertNotNull(sepWord1);
        Assert.assertNotNull(sepWord2);
        Assert.assertNotEquals(spa1.computeOutput(sepWord1), spa2.computeOutput(sepWord1));
        Assert.assertNotEquals(spa1.computeOutput(sepWord2), spa2.computeOutput(sepWord2));

        Assert.assertNull(SPAs.findSeparatingWord(spa1, spa1, emptyAlphabet));
        Assert.assertNull(SPAs.findSeparatingWord(spa2, spa2, emptyAlphabet));
        Assert.assertNull(SPAs.findSeparatingWord(spa1, spa2, emptyAlphabet));
        Assert.assertNull(SPAs.findSeparatingWord(spa2, spa1, emptyAlphabet));
    }

    @Test
    public void testIntricateSeparatingWord() {
        // construct a simple (pseudo) palindrome system which we will gradually alter to model different cases
        final CompactDFA<Character> s1 = new CompactDFA<>(alphabet.getProceduralAlphabet());
        final FastDFA<Character> t1 = new FastDFA<>(alphabet.getProceduralAlphabet());

        final int s1s0 = s1.addInitialState(false);
        final int s1s1 = s1.addState(false);
        final int s1s2 = s1.addState(false);
        final int s1s3 = s1.addState(false);
        final int s1s4 = s1.addState(false);
        final int s1s5 = s1.addState(false);

        s1.addTransition(s1s0, 'T', s1s5);
        s1.addTransition(s1s0, 'a', s1s1);
        s1.addTransition(s1s0, 'b', s1s2);
        s1.addTransition(s1s1, 'S', s1s3);
        s1.addTransition(s1s2, 'S', s1s4);
        s1.addTransition(s1s3, 'a', s1s5);
        s1.addTransition(s1s4, 'b', s1s5);

        final FastDFAState t1t0 = t1.addInitialState(false);
        final FastDFAState t1t1 = t1.addState(false);
        final FastDFAState t1t2 = t1.addState(false);
        final FastDFAState t1t3 = t1.addState(false);

        t1.addTransition(t1t0, 'c', t1t1);
        t1.addTransition(t1t1, 'T', t1t2);
        t1.addTransition(t1t2, 'c', t1t3);

        final SPA<?, Character> spa1 = new StackSPA<>(alphabet, 'S', Map.of('S', s1, 'T', t1));

        final CompactDFA<Character> s2 = new CompactDFA<>(alphabet.getProceduralAlphabet());
        final FastDFA<Character> t2 = new FastDFA<>(alphabet.getProceduralAlphabet());

        final int s2s0 = s2.addInitialState(false);
        final int s2s1 = s2.addState(false);
        final int s2s2 = s2.addState(false);
        final int s2s3 = s2.addState(false);
        final int s2s4 = s2.addState(false);
        final int s2s5 = s2.addState(false);

        s2.addTransition(s2s0, 'T', s2s5);
        s2.addTransition(s2s0, 'a', s2s1);
        s2.addTransition(s2s0, 'b', s2s2);
        s2.addTransition(s2s1, 'S', s2s3);
        s2.addTransition(s2s2, 'S', s2s4);
        s2.addTransition(s2s3, 'a', s2s5);
        s2.addTransition(s2s4, 'b', s2s5);

        final FastDFAState t2t0 = t2.addInitialState(false);
        final FastDFAState t2t1 = t2.addState(false);
        final FastDFAState t2t2 = t2.addState(false);
        final FastDFAState t2t3 = t2.addState(false);

        t2.addTransition(t2t0, 'c', t2t1);
        t2.addTransition(t2t1, 'T', t2t2);
        t2.addTransition(t2t2, 'c', t2t3);

        final SPA<?, Character> spa2 = new StackSPA<>(alphabet, 'S', Map.of('S', s2, 'T', t2));

        final SPA<?, Character> emptySPA = new EmptySPA<>(alphabet);

        // no accessible procedures, no separating word should exist. Even with the empty SPAs
        Assert.assertNull(SPAs.findSeparatingWord(spa1, spa2, alphabet));
        Assert.assertNull(SPAs.findSeparatingWord(emptySPA, spa2, alphabet));
        Assert.assertNull(SPAs.findSeparatingWord(spa1, emptySPA, alphabet));

        // make SPA1's 'S' procedure accept 'a'. Now there should exist a separating word
        s1.setAccepting(s1s1, true);
        verifySepWord(spa1, spa2, alphabet);
        verifySepWord(spa2, spa1, alphabet);
        verifySepWord(spa1, emptySPA, alphabet);
        verifySepWord(emptySPA, spa1, alphabet);

        // however, not if we restrict the alphabet to 'b','c'
        final ProceduralInputAlphabet<Character> bcAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('b', 'c'), callAlphabet, returnSymbol);
        Assert.assertNull(SPAs.findSeparatingWord(spa1, spa2, bcAlphabet));
        Assert.assertNull(SPAs.findSeparatingWord(spa2, spa1, bcAlphabet));
        Assert.assertNull(SPAs.findSeparatingWord(spa1, emptySPA, bcAlphabet));
        Assert.assertNull(SPAs.findSeparatingWord(emptySPA, spa1, bcAlphabet));

        // update SPA2 according to SPA1. There should no longer exist a separating word
        s2.setAccepting(s2s1, true);
        Assert.assertNull(SPAs.findSeparatingWord(spa1, spa2, alphabet));
        Assert.assertNull(SPAs.findSeparatingWord(spa2, spa1, alphabet));

        // make SPA1's s5 accept so that we introduce procedure 'T'. This also adds a new separating word (two 'a's)
        s1.setAccepting(s1s5, true);
        verifySepWord(spa1, spa2, alphabet);
        verifySepWord(spa2, spa1, alphabet);

        // update SPA2 accordingly
        s2.setAccepting(s2s5, true);
        Assert.assertNull(SPAs.findSeparatingWord(spa1, spa2, alphabet));
        Assert.assertNull(SPAs.findSeparatingWord(spa2, spa1, alphabet));

        // make SPA1's procedure 'T' accept 'c' so that we can find another separating word
        t1.setAccepting(t1t1, true);
        verifySepWord(spa1, spa2, alphabet);
        verifySepWord(spa2, spa1, alphabet);

        // this should also work for partial SPAs
        final SPA<?, Character> partial1 = new StackSPA<>(alphabet, 'S', Map.of('S', s1));
        verifySepWord(spa1, partial1, alphabet);
        verifySepWord(partial1, spa1, alphabet);

        // If we restrict ourselves to only 'S' call symbols, a separating word should no longer exist
        final ProceduralInputAlphabet<Character> sAlphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, Alphabets.singleton('S'), returnSymbol);
        Assert.assertNull(SPAs.findSeparatingWord(spa1, spa2, sAlphabet));
        Assert.assertNull(SPAs.findSeparatingWord(spa2, spa1, sAlphabet));

        // update SPA2 accordingly
        t2.setAccepting(t2t1, true);
        Assert.assertNull(SPAs.findSeparatingWord(spa1, spa2, alphabet));
        Assert.assertNull(SPAs.findSeparatingWord(spa2, spa1, alphabet));

        // make SPA1's 'T' procedure also accept two c's.
        // This should yield a separating word even if we restrict ourselves to only 'c' as internal symbol
        t1.setAccepting(t1t3, true);
        final ProceduralInputAlphabet<Character> cAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.singleton('c'), callAlphabet, returnSymbol);
        verifySepWord(spa1, spa2, alphabet);
        verifySepWord(spa2, spa1, alphabet);
        verifySepWord(spa1, spa2, cAlphabet);
        verifySepWord(spa2, spa1, cAlphabet);
    }

    private static <I> void verifySepWord(SPA<?, I> spa1, SPA<?, I> spa2, ProceduralInputAlphabet<I> alphabet) {
        final Word<I> sepWord = SPAs.findSeparatingWord(spa1, spa2, alphabet);
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(spa1.accepts(sepWord), spa2.accepts(sepWord));
    }

    /**
     * Tests a (snapshot of a) randomly generated SPA that has uncovered a bug in a previous implementation.
     */
    @Test
    public void testRandomBenchmarkSystem1() {
        final ProceduralInputAlphabet<Integer> alphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.integers(0, 9), Alphabets.integers(10, 12), -1);

        // @formatter:off
        CompactDFA<Integer> p10 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).loop()
                                                   .from("s0").on(0).to("s1")
                                                   .from("s1").on(0, 1, 3, 4, 10, 11, 12).to("s0")
                                                   .from("s1").on(2, 5, 7).loop()
                                                   .withAccepting("s1")
                                                   .create();

        CompactDFA<Integer> p11 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).loop()
                                                   .from("s0").on(12).to("s1")
                                                   .from("s1").on(0).loop()
                                                   .from("s1").on(1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).to("s0")
                                                   .withAccepting("s1")
                                                   .create();

        CompactDFA<Integer> p12 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0).to("s1")
                                                   .from("s0").on(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).to("s2")
                                                   .from("s1").on(6, 12).to("s0")
                                                   .from("s1").on(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11).to("s2")
                                                   .from("s2").on(8).to("s0")
                                                   .from("s2").on(5).to("s1")
                                                   .from("s2").on(0, 1, 2, 3, 4, 6, 7, 9, 10, 11, 12).loop()
                                                   .withAccepting("s0")
                                                   .create();
        // @formatter:on

        final Map<Integer, DFA<?, Integer>> procedures = new HashMap<>();
        procedures.put(10, p10);
        procedures.put(11, p11);
        procedures.put(12, p12);

        final SPA<?, Integer> spa = new StackSPA<>(alphabet, 11, procedures);

        final ATRSequences<Integer> atr = SPAs.computeATRSequences(spa);

        verifyATR(spa, alphabet, atr);
    }

    /**
     * Tests a (snapshot of a) randomly generated SPA that has uncovered a bug in a previous implementation.
     */
    @Test
    public void testRandomBenchmarkSystem2() {
        final ProceduralInputAlphabet<Integer> alphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.integers(0, 9), Alphabets.integers(10, 11), -1);

        // @formatter:off
        CompactDFA<Integer> p10 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0, 6, 7, 10, 11).loop()
                                                   .from("s0").on(1, 2, 3, 4, 5, 8, 9).to("s1")
                                                   .from("s1").on(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).loop()
                                                   .withAccepting("s0")
                                                   .create();

        CompactDFA<Integer> p11 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0, 2, 3, 4, 5, 7, 8, 10, 11).to("s1")
                                                   .from("s0").on(1, 6, 9).to("s2")
                                                   .from("s1").on(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).loop()
                                                   .from("s2").on(8, 11).to("s0")
                                                   .from("s2").on(0, 1, 2, 9).to("s1")
                                                   .from("s2").on(4, 5, 6, 7).loop()
                                                   .from("s2").on(3, 10).to("s3")
                                                   .from("s3").on(1, 6, 9, 11).to("s2")
                                                   .from("s3").on(0, 2, 3, 4, 5, 7, 8, 10).to("s1")
                                                   .withAccepting("s0")
                                                   .create();
        // @formatter:on

        final Map<Integer, DFA<?, Integer>> procedures = new HashMap<>();
        procedures.put(10, p10);
        procedures.put(11, p11);

        final SPA<?, Integer> spa = new StackSPA<>(alphabet, 11, procedures);

        final ATRSequences<Integer> atr = SPAs.computeATRSequences(spa);

        verifyATR(spa, alphabet, atr);
    }

    /**
     * Tests a (snapshot of a) randomly generated SPA that has uncovered a bug in a previous implementation.
     */
    @Test
    public void testRandomBenchmarkSystem3() {
        final ProceduralInputAlphabet<Integer> alphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.singleton(0), Alphabets.fromArray(1, 2), -1);

        // @formatter:off
        CompactDFA<Integer> p1 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0).to("s1")
                                                   .from("s1").on(0).to("s2")
                                                   .from("s2").on(0).to("s3")
                                                   .from("s3").on(2).to("s4")
                                                   .from("s4").on(1).to("s3")
                                                   .from("s3").on(1).to("s2")
                                                   .from("s2").on(1).to("s1")
                                                   .from("s1").on(1).to("s0")
                                                   .withAccepting("s0")
                                                   .create();

        CompactDFA<Integer> p2 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0, 1, 2).loop()
                                                   .withAccepting("s0")
                                                   .create();
        // @formatter:on

        final Map<Integer, DFA<?, Integer>> procedures = new HashMap<>();
        procedures.put(1, p1);
        procedures.put(2, p2);

        final SPA<?, Integer> spa = new StackSPA<>(alphabet, 1, procedures);

        final ATRSequences<Integer> atr = SPAs.computeATRSequences(spa);

        verifyATR(spa, alphabet, atr);
    }

    @Test
    public void testEquivalence() {
        final Random random = new Random(42);
        final int size = 10;

        final SPA<?, Character> spa1 = RandomAutomata.randomSPA(random, alphabet, size);
        final SPA<?, Character> spa2 = RandomAutomata.randomSPA(random, alphabet, size);

        Assert.assertTrue(SPAs.testEquivalence(spa1, spa1, alphabet));
        Assert.assertTrue(SPAs.testEquivalence(spa2, spa2, alphabet));

        Assert.assertFalse(SPAs.testEquivalence(spa1, spa2, alphabet));
        Assert.assertFalse(SPAs.testEquivalence(spa2, spa1, alphabet));
    }

    @Test(dataProvider = "systems")
    public <I> void testOneSEVPAConversion(SPA<?, I> spa) {
        final OneSEVPA<?, I> oneSEVPA = SPAs.toOneSEVPA(spa);

        final List<Word<I>> tests = IteratorUtil.list(new SPATestsIterator<>(spa, WMethodTestsIterator::new));

        for (Word<I> t : tests) {
            Assert.assertEquals(spa.accepts(t), oneSEVPA.accepts(t));
        }
    }

    @Test(dataProvider = "systems")
    public <I> void testNSEVPAConversion(SPA<?, I> spa) {
        final SEVPA<?, I> sevpa = SPAs.toNSEVPA(spa);

        final List<Word<I>> tests = IteratorUtil.list(new SPATestsIterator<>(spa, WMethodTestsIterator::new));

        for (Word<I> t : tests) {
            Assert.assertEquals(spa.accepts(t), sevpa.accepts(t));
        }
    }

    @Test
    public void testPalindromeSystemAsCFMPS() throws IOException {
        final SPA<?, Character> spa = buildPalindromeSystem();
        final ContextFreeModalProcessSystem<Character, Void> cfmps = SPAs.toCFMPS(spa);

        Assert.assertEquals(cfmps.getMainProcess(), 'S');

        final Map<Character, ProceduralModalProcessGraph<?, Character, ?, Void, ?>> pmpgs = cfmps.getPMPGs();
        Assert.assertEquals(pmpgs.size(), 2);

        final ProceduralModalProcessGraph<?, Character, ?, Void, ?> s = pmpgs.get('S');
        Assert.assertNotNull(s);
        Assert.assertEquals(s.getNodes().size(), 9);

        final ProceduralModalProcessGraph<?, Character, ?, Void, ?> t = pmpgs.get('T');
        Assert.assertNotNull(t);
        Assert.assertEquals(t.getNodes().size(), 7);

        verifyDot(cfmps, "/cfmps/palindrome.dot");
    }

    @Test
    public void testDissSystemAsCFMPS() throws IOException {
        final SPA<?, String> spa = buildDissSystem();
        final ContextFreeModalProcessSystem<String, Void> cfmps = SPAs.toCFMPS(spa);

        Assert.assertEquals(cfmps.getMainProcess(), "main");

        final Map<String, ProceduralModalProcessGraph<?, String, ?, Void, ?>> pmpgs = cfmps.getPMPGs();
        Assert.assertEquals(pmpgs.size(), 3);

        final ProceduralModalProcessGraph<?, String, ?, Void, ?> main = pmpgs.get("main");
        Assert.assertNotNull(main);
        Assert.assertEquals(main.getNodes().size(), 6);

        final ProceduralModalProcessGraph<?, String, ?, Void, ?> c1 = pmpgs.get("c_1");
        Assert.assertNotNull(c1);
        Assert.assertEquals(c1.getNodes().size(), 5);

        final ProceduralModalProcessGraph<?, String, ?, Void, ?> c2 = pmpgs.get("c_2");
        Assert.assertNotNull(c2);
        Assert.assertEquals(c2.getNodes().size(), 5);

        verifyDot(cfmps, "/cfmps/diss.dot");
    }

    static void verifyDot(ContextFreeModalProcessSystem<?, ?> cfmps, String expected) throws IOException {
        final StringWriter dotWriter = new StringWriter();
        final StringWriter expectedWriter = new StringWriter();

        try (Reader reader = IOUtil.asBufferedUTF8Reader(SPAsTest.class.getResourceAsStream(expected))) {
            IOUtil.copy(reader, expectedWriter);
            GraphDOT.write(cfmps, dotWriter);
            Assert.assertEquals(dotWriter.toString(), expectedWriter.toString());
        }
    }

    @DataProvider(name = "systems")
    private static Object[][] getExampleSystems() {
        final ProceduralInputAlphabet<Character> alphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('0', '9'),
                                                     Alphabets.characters('A', 'C'),
                                                     'R');

        return new Object[][] {{RandomAutomata.randomSPA(new Random(42), alphabet, 5)},
                               {buildPalindromeSystem()},
                               {buildDissSystem()}};
    }

    private static <I> void verifyATR(SPA<?, I> spa, ProceduralInputAlphabet<I> alphabet, ATRSequences<I> atr) {

        for (I i : alphabet.getCallAlphabet()) {
            final Word<I> as = atr.accessSequences.get(i);
            final Word<I> ts = atr.terminatingSequences.get(i);
            final Word<I> rs = atr.returnSequences.get(i);

            Assert.assertNotNull(as, Objects.toString(i));
            Assert.assertNotNull(ts, Objects.toString(i));
            Assert.assertNotNull(rs, Objects.toString(i));
            Assert.assertTrue(spa.accepts(Word.fromWords(as, ts, rs)), Objects.toString(i));
        }
    }

    private static SPA<?, Character> buildPalindromeSystem() {
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
        final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'T');
        final ProceduralInputAlphabet<Character> alphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, 'R');

        // @formatter:off
        final MutableDFA<?, Character> sProcedure =
                AutomatonBuilders.forDFA(new CompactDFA<>(alphabet.getProceduralAlphabet()))
                                 .withInitial("s0")
                                 .from("s0").on('T').to("s5")
                                 .from("s0").on('a').to("s1")
                                 .from("s0").on('b').to("s2")
                                 .from("s1").on('S').to("s3")
                                 .from("s2").on('S').to("s4")
                                 .from("s3").on('a').to("s5")
                                 .from("s4").on('b').to("s5")
                                 .withAccepting("s0", "s1", "s2", "s5")
                                 .create();

        final MutableDFA<?, Character> tProcedure =
                AutomatonBuilders.forDFA(new FastDFA<>(alphabet.getProceduralAlphabet()))
                                 .withInitial("t0")
                                 .from("t0").on('S').to("t3")
                                 .from("t0").on('c').to("t1")
                                 .from("t1").on('T').to("t2")
                                 .from("t2").on('c').to("t3")
                                 .withAccepting("t1", "t3")
                                 .create();
        // @formatter:on

        MutableDFAs.complete(sProcedure, alphabet.getProceduralAlphabet());
        MutableDFAs.complete(tProcedure, alphabet.getProceduralAlphabet());

        return new StackSPA<>(alphabet, 'S', Map.of('S', sProcedure, 'T', tProcedure));
    }

    private static SPA<?, String> buildDissSystem() {
        final Alphabet<String> internalAlphabet = Alphabets.fromArray("a", "b");
        final Alphabet<String> callAlphabet = Alphabets.fromArray("main", "c_1", "c_2");
        final ProceduralInputAlphabet<String> alphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, "r");

        // @formatter:off
        final MutableDFA<?, String> mainProcedure =
                AutomatonBuilders.forDFA(new CompactDFA<>(alphabet.getProceduralAlphabet()))
                                 .withInitial("s0")
                                 .from("s0").on("c_1").to("s1")
                                 .from("s1").on("c_2").to("s2")
                                 .withAccepting("s2")
                                 .create();

        final MutableDFA<?, String> aProcedure =
                AutomatonBuilders.forDFA(new CompactDFA<>(alphabet.getProceduralAlphabet()))
                                 .withInitial("s0")
                                 .from("s0").on("a").to("s1")
                                 .withAccepting("s1")
                                 .create();

        final MutableDFA<?, String> bProcedure =
                AutomatonBuilders.forDFA(new CompactDFA<>(alphabet.getProceduralAlphabet()))
                                 .withInitial("s0")
                                 .from("s0").on("b").to("s1")
                                 .withAccepting("s1")
                                 .create();
        // @formatter:on

        MutableDFAs.complete(mainProcedure, alphabet.getProceduralAlphabet());
        MutableDFAs.complete(aProcedure, alphabet.getProceduralAlphabet());
        MutableDFAs.complete(bProcedure, alphabet.getProceduralAlphabet());

        return new StackSPA<>(alphabet, "main", Map.of("main", mainProcedure, "c_1", aProcedure, "c_2", bProcedure));
    }

}
