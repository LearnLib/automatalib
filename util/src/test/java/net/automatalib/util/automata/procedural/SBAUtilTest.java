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
package net.automatalib.util.automata.procedural;

import java.util.Map.Entry;
import java.util.Random;

import com.google.common.collect.ImmutableMap;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.procedural.EmptySBA;
import net.automatalib.automata.procedural.SBA;
import net.automatalib.automata.procedural.SPA;
import net.automatalib.automata.procedural.StackSBA;
import net.automatalib.automata.procedural.StackSPA;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.fsa.MutableDFAs;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultProceduralInputAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class SBAUtilTest {

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
        final ATSequences<Character> atSequences = SBAUtil.computeATSequences(sba);

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
        final ATSequences<Character> atrSequences = SBAUtil.computeATSequences(sba);

        Assert.assertTrue(atrSequences.accessSequences.isEmpty());
        Assert.assertTrue(atrSequences.terminatingSequences.isEmpty());
    }

    @Test
    public void testSeparatingWord() {
        final Random random = new Random(42);
        final int size = 10;

        final SBA<?, Character> sba1 = RandomAutomata.randomSBA(random, alphabet, size);
        final SBA<?, Character> sba2 = RandomAutomata.randomSBA(random, alphabet, size);

        Assert.assertNull(Automata.findSeparatingWord(sba1, sba1, alphabet));
        Assert.assertNull(Automata.findSeparatingWord(sba2, sba2, alphabet));

        final Word<Character> sepWord1 = Automata.findSeparatingWord(sba1, sba2, alphabet);
        final Word<Character> sepWord2 = Automata.findSeparatingWord(sba2, sba1, alphabet);
        Assert.assertNotNull(sepWord1);
        Assert.assertNotNull(sepWord2);
        Assert.assertNotEquals(sba1.computeOutput(sepWord1), sba2.computeOutput(sepWord1));
        Assert.assertNotEquals(sba1.computeOutput(sepWord2), sba2.computeOutput(sepWord2));

        Assert.assertNull(Automata.findSeparatingWord(sba1, sba1, emptyAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(sba2, sba2, emptyAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(sba1, sba2, emptyAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(sba2, sba1, emptyAlphabet));
    }

    @Test
    public void testEquivalence() {
        final Random random = new Random(42);
        final int size = 10;

        final SBA<?, Character> sba1 = RandomAutomata.randomSBA(random, alphabet, size);
        final SBA<?, Character> sba2 = RandomAutomata.randomSBA(random, alphabet, size);

        Assert.assertTrue(Automata.testEquivalence(sba1, sba1, alphabet));
        Assert.assertTrue(Automata.testEquivalence(sba2, sba2, alphabet));

        Assert.assertFalse(Automata.testEquivalence(sba1, sba2, alphabet));
        Assert.assertFalse(Automata.testEquivalence(sba2, sba1, alphabet));
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

        final StackSBA<?, Character> sba = new StackSBA<>(alphabet, 'S', ImmutableMap.of('S', sbaS, 'T', sbaT));
        final StackSPA<?, Character> spa = new StackSPA<>(alphabet, 'S', ImmutableMap.of('S', spaS, 'T', spaT));
        final SPA<?, Character> reduced = SBAUtil.reduce(sba);

        Assert.assertTrue(Automata.testEquivalence(spa, reduced, alphabet));
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

}
