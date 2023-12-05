/* Copyright (C) 2013-2024 TU Dortmund University
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

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import com.google.common.io.CharStreams;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.common.util.IOUtil;
import net.automatalib.incremental.ConflictException;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public abstract class AbstractIncrementalDFABuilderTest {

    private static final Alphabet<Character> TEST_ALPHABET = Alphabets.characters('a', 'c');

    private static final Word<Character> W_1 = Word.fromString("abc");
    private static final Word<Character> W_2 = Word.fromString("ac");
    private static final Word<Character> W_3 = Word.fromString("acb");
    private static final Word<Character> W_4 = Word.epsilon();

    private IncrementalDFABuilder<Character> incDfa;

    @BeforeClass
    public void setUp() {
        this.incDfa = createIncrementalDFABuilder(TEST_ALPHABET);
    }

    protected abstract <I> IncrementalDFABuilder<I> createIncrementalDFABuilder(Alphabet<I> alphabet);

    protected abstract String getDOTResource();

    @Test
    public void testConfluenceBug() {
        Word<Character> wB1 = Word.fromString("aaa");
        Word<Character> wB2 = Word.fromString("bba");
        Word<Character> wB3 = Word.fromString("aabaa");

        incDfa.insert(wB1, true);
        incDfa.insert(wB2, true);
        incDfa.insert(wB3, true);

        Assert.assertEquals(incDfa.lookup(Word.fromString("aababaa")), Acceptance.DONT_KNOW);
        this.incDfa = createIncrementalDFABuilder(TEST_ALPHABET);
    }

    @Test(dependsOnMethods = "testConfluenceBug")
    public void testLookup() {
        Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(W_1));
        Assert.assertEquals(incDfa.lookup(W_2), Acceptance.DONT_KNOW);
        Assert.assertEquals(incDfa.lookup(W_3), Acceptance.DONT_KNOW);

        incDfa.insert(W_1, true);
        Assert.assertEquals(incDfa.lookup(W_1), Acceptance.TRUE);
        Assert.assertEquals(incDfa.lookup(W_2), Acceptance.DONT_KNOW);
        Assert.assertEquals(incDfa.lookup(W_3), Acceptance.DONT_KNOW);

        Assert.assertEquals(incDfa.lookup(W_1.prefix(2)), Acceptance.DONT_KNOW);
        Assert.assertEquals(incDfa.lookup(W_2.prefix(1)), Acceptance.DONT_KNOW);
        Assert.assertEquals(incDfa.lookup(W_3.prefix(2)), Acceptance.DONT_KNOW);

        incDfa.insert(W_2, false);
        Assert.assertEquals(incDfa.lookup(W_1), Acceptance.TRUE);
        Assert.assertEquals(incDfa.lookup(W_2), Acceptance.FALSE);
        Assert.assertEquals(incDfa.lookup(W_3), Acceptance.DONT_KNOW);

        Assert.assertEquals(incDfa.lookup(W_1.prefix(2)), Acceptance.DONT_KNOW);
        Assert.assertEquals(incDfa.lookup(W_2.prefix(1)), Acceptance.DONT_KNOW);
        Assert.assertEquals(incDfa.lookup(W_3.prefix(2)), Acceptance.FALSE);

        incDfa.insert(W_3, true);
        Assert.assertEquals(incDfa.lookup(W_1), Acceptance.TRUE);
        Assert.assertEquals(incDfa.lookup(W_2), Acceptance.FALSE);
        Assert.assertEquals(incDfa.lookup(W_3), Acceptance.TRUE);

        Assert.assertEquals(incDfa.lookup(W_1.prefix(2)), Acceptance.DONT_KNOW);
        Assert.assertEquals(incDfa.lookup(W_2.prefix(1)), Acceptance.DONT_KNOW);
        Assert.assertEquals(incDfa.lookup(W_3.prefix(2)), Acceptance.FALSE);

        incDfa.insert(W_4, true);
        Assert.assertEquals(incDfa.lookup(W_1), Acceptance.TRUE);
        Assert.assertEquals(incDfa.lookup(W_2), Acceptance.FALSE);
        Assert.assertEquals(incDfa.lookup(W_3), Acceptance.TRUE);
        Assert.assertEquals(incDfa.lookup(W_4), Acceptance.TRUE);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testInsertSame() {
        int oldSize = incDfa.asGraph().size();
        incDfa.insert(W_1, true);
        Assert.assertEquals(incDfa.asGraph().size(), oldSize);
    }

    @Test(expectedExceptions = ConflictException.class, dependsOnMethods = "testLookup")
    public void testConflict() {
        incDfa.insert(W_1, false);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testFindSeparatingWord() {
        CompactDFA<Character> testDfa = new CompactDFA<>(TEST_ALPHABET);

        Word<Character> sepWord;
        sepWord = incDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.epsilon());

        int s0 = testDfa.addInitialState(true);
        int s1 = testDfa.addState(false);
        int s2 = testDfa.addState(false);
        int s3 = testDfa.addState(true);

        testDfa.addTransition(s0, 'a', s1);
        testDfa.addTransition(s1, 'b', s2);
        testDfa.addTransition(s2, 'c', s3);

        sepWord = incDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("acb"));

        testDfa.setAccepting(s3, false);
        sepWord = incDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertEquals(sepWord, Word.fromString("abc"));
        testDfa.setAccepting(s3, true);

        int s4 = testDfa.addState(false);
        int s5 = testDfa.addState(true);
        testDfa.addTransition(s1, 'c', s4);
        testDfa.addTransition(s4, 'b', s5);

        sepWord = incDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        testDfa.setAccepting(s1, true);
        testDfa.setAccepting(s2, true);
        sepWord = incDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testVisualization() throws IOException {
        final StringWriter dotWriter = new StringWriter();
        final StringWriter expectedWriter = new StringWriter();

        try (Reader reader = IOUtil.asBufferedUTF8Reader(AbstractIncrementalDFABuilderTest.class.getResourceAsStream(
                getDOTResource()))) {

            GraphDOT.write(incDfa.asGraph(), dotWriter);
            CharStreams.copy(reader, expectedWriter);

            Assert.assertEquals(dotWriter.toString(), expectedWriter.toString());
        }
    }

    @Test(dependsOnMethods = "testLookup")
    public void testTSView() {
        testTSViewInternal(incDfa.asTransitionSystem());
    }

    private static <S> void testTSViewInternal(UniversalDTS<S, Character, ?, Acceptance, Void> view) {
        final S s1 = view.getState(W_1);
        final S s2 = view.getState(W_2);
        final S s3 = view.getState(W_3);
        final S s4 = view.getState(W_4);

        Assert.assertTrue(view.getStateProperty(s1).toBoolean());
        Assert.assertFalse(view.getStateProperty(s2).toBoolean());
        Assert.assertTrue(view.getStateProperty(s3).toBoolean());
        Assert.assertTrue(view.getStateProperty(s4).toBoolean());
    }

    @Test
    public void testCounterexampleOfLengthOne() {
        final IncrementalDFABuilder<Character> incDfa = createIncrementalDFABuilder(TEST_ALPHABET);
        incDfa.insert(Word.fromLetter('a'), true);

        final CompactDFA<Character> dfa = new CompactDFA<>(TEST_ALPHABET);
        final Integer q0 = dfa.addInitialState(true);
        final Integer q1 = dfa.addState(false);

        dfa.addTransition(q0, 'a', q1);

        final Word<Character> ce = incDfa.findSeparatingWord(dfa, TEST_ALPHABET, false);
        Assert.assertNotNull(ce);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testNewInputSymbol() {
        final GrowingAlphabet<Character> alphabet = new GrowingMapAlphabet<>(TEST_ALPHABET);
        final IncrementalDFABuilder<Character> growableBuilder = createIncrementalDFABuilder(alphabet);

        growableBuilder.addAlphabetSymbol('d');
        growableBuilder.addAlphabetSymbol('d');

        final Word<Character> input1 = Word.fromString("dcba");

        growableBuilder.insert(input1, true);

        Assert.assertTrue(growableBuilder.hasDefinitiveInformation(input1));
        Assert.assertEquals(growableBuilder.lookup(input1), Acceptance.TRUE);

        growableBuilder.addAlphabetSymbol('e');

        final Word<Character> input2 = Word.fromString("ddee");

        Assert.assertFalse(growableBuilder.hasDefinitiveInformation(input2));
        Assert.assertEquals(growableBuilder.lookup(input2), Acceptance.DONT_KNOW);

        growableBuilder.insert(input2, false);
        Assert.assertEquals(growableBuilder.lookup(input2), Acceptance.FALSE);
    }

}
