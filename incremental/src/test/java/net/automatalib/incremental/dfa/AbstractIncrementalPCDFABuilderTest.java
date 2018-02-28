/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.incremental.dfa;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.incremental.ConflictException;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public abstract class AbstractIncrementalPCDFABuilderTest {

    private static final Alphabet<Character> TEST_ALPHABET = Alphabets.characters('a', 'c');

    private IncrementalDFABuilder<Character> incPcDfa;

    @BeforeClass
    public void setUp() {
        this.incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);
    }

    protected abstract <I> IncrementalDFABuilder<I> createIncrementalPCDFABuilder(Alphabet<I> alphabet);

    @Test
    public void testConfluenceBug() {
        Word<Character> wB1 = Word.fromString("aaa");
        Word<Character> wB2 = Word.fromString("bba");
        Word<Character> wB3 = Word.fromString("aabaa");

        incPcDfa.insert(wB1, true);
        incPcDfa.insert(wB2, true);
        incPcDfa.insert(wB3, true);

        Assert.assertEquals(incPcDfa.lookup(Word.fromString("aababaa")), Acceptance.DONT_KNOW);
        this.incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);
    }

    @Test(dependsOnMethods = "testConfluenceBug")
    public void testLookup() {
        Word<Character> w1 = Word.fromString("abc");
        Word<Character> w2 = Word.fromString("acb");
        Word<Character> w3 = Word.fromString("ac");

        Assert.assertEquals(incPcDfa.lookup(w1), Acceptance.DONT_KNOW);
        Assert.assertEquals(incPcDfa.lookup(w2), Acceptance.DONT_KNOW);
        Assert.assertEquals(incPcDfa.lookup(w3), Acceptance.DONT_KNOW);

        incPcDfa.insert(w1, true);
        Assert.assertEquals(incPcDfa.lookup(w1), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(w1.prefix(2)), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(w1.prefix(1)), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(w1.prefix(0)), Acceptance.TRUE);

        Assert.assertEquals(incPcDfa.lookup(w2), Acceptance.DONT_KNOW);
        Assert.assertEquals(incPcDfa.lookup(w3), Acceptance.DONT_KNOW);

        incPcDfa.insert(w2, false);
        Assert.assertEquals(incPcDfa.lookup(w1), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(w2), Acceptance.FALSE);
        Assert.assertEquals(incPcDfa.lookup(w2.append('a')), Acceptance.FALSE);
        Assert.assertEquals(incPcDfa.lookup(w3), Acceptance.DONT_KNOW);

        Assert.assertEquals(incPcDfa.lookup(w1.prefix(1)), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(w2.prefix(1)), Acceptance.TRUE);

        incPcDfa.insert(w3, true);
        Assert.assertEquals(incPcDfa.lookup(w1), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(w2), Acceptance.FALSE);
        Assert.assertEquals(incPcDfa.lookup(w3), Acceptance.TRUE);

        Assert.assertEquals(incPcDfa.lookup(w1.prefix(2)), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(w2.prefix(1)), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(w3.append('a')), Acceptance.DONT_KNOW);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testInsertSame() {
        Word<Character> w1 = Word.fromString("abc");
        int oldSize = incPcDfa.asGraph().size();
        incPcDfa.insert(w1, true);
        Assert.assertEquals(incPcDfa.asGraph().size(), oldSize);
    }

    @Test(expectedExceptions = ConflictException.class, dependsOnMethods = "testLookup")
    public void testConflict() {
        Word<Character> w1 = Word.fromString("abc");
        incPcDfa.insert(w1, false);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testFindSeparatingWord() {
        CompactDFA<Character> testDfa = new CompactDFA<>(TEST_ALPHABET);
        int s0 = testDfa.addInitialState(true);
        int s1 = testDfa.addState(true);
        int s2 = testDfa.addState(true);
        int s3 = testDfa.addState(true);

        testDfa.addTransition(s0, 'a', s1);
        testDfa.addTransition(s1, 'b', s2);
        testDfa.addTransition(s2, 'c', s3);

        Word<Character> sepWord;
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("ac"));

        int s4 = testDfa.addState(true);
        testDfa.addTransition(s1, 'c', s4);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        int s5 = testDfa.addState(false);
        testDfa.addTransition(s4, 'b', s5);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        int s6 = testDfa.addState(false);
        testDfa.addTransition(s3, 'a', s6);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        int s7 = testDfa.addState(true);
        testDfa.addTransition(s5, 'a', s7);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertEquals(sepWord, Word.fromString("acba"));
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("acba"));
    }

    @Test
    public void testCounterexampleOfLengthOne() {
        final IncrementalDFABuilder<Character> incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);
        incPcDfa.insert(Word.fromCharSequence("a"), true);

        final CompactDFA<Character> dfa = new CompactDFA<>(TEST_ALPHABET);
        final Integer q0 = dfa.addInitialState(true);
        final Integer q1 = dfa.addState(false);

        dfa.addTransition(q0, 'a', q1);

        final Word<Character> ce = incPcDfa.findSeparatingWord(dfa, TEST_ALPHABET, false);
        Assert.assertNotNull(ce);
    }
}
