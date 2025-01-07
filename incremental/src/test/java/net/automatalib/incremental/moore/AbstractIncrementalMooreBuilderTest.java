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
package net.automatalib.incremental.moore;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.common.util.IOUtil;
import net.automatalib.incremental.ConflictException;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.ts.output.MooreTransitionSystem;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public abstract class AbstractIncrementalMooreBuilderTest {

    private static final Alphabet<Character> TEST_ALPHABET = Alphabets.characters('a', 'c');
    private static final Word<Character> W_1 = Word.fromString("abc");
    private static final Word<Character> W_1_O = Word.fromString("-xyz");
    private static final Word<Character> W_2 = Word.fromString("ac");
    private static final Word<Character> W_2_O = Word.fromString("-xw");
    private static final Word<Character> W_3 = Word.fromString("acb");
    private static final Word<Character> W_3_O = Word.fromString("-xwu");

    // Confluence Bug
    private static final Word<Character> W_B_1 = Word.fromString("aaa");
    private static final Word<Character> W_B_1_O = Word.fromString("-xxx");
    private static final Word<Character> W_B_2 = Word.fromString("bba");
    private static final Word<Character> W_B_2_O = Word.fromString("-xxx");
    private static final Word<Character> W_B_3 = Word.fromString("aabaa");
    private static final Word<Character> W_B_3_O = Word.fromString("-xxxxx");

    private IncrementalMooreBuilder<Character, Character> incMoore;

    @BeforeClass
    public void setUp() {
        this.incMoore = createIncrementalMooreBuilder(TEST_ALPHABET);
    }

    protected abstract <I, O> IncrementalMooreBuilder<I, O> createIncrementalMooreBuilder(Alphabet<I> alphabet);

    protected abstract String getDOTResource();

    @Test
    public void testConfluenceBug() {
        incMoore.insert(W_B_1, W_B_1_O);
        incMoore.insert(W_B_2, W_B_2_O);
        incMoore.insert(W_B_3, W_B_3_O);

        Assert.assertFalse(incMoore.lookup(Word.fromString("aababaa"), new ArrayList<>()));
        // reset for further tests
        this.incMoore = createIncrementalMooreBuilder(TEST_ALPHABET);
    }

    @Test(dependsOnMethods = "testConfluenceBug")
    public void testLookup() {
        Assert.assertFalse(incMoore.hasDefinitiveInformation(W_1));
        Assert.assertFalse(incMoore.hasDefinitiveInformation(W_2));
        Assert.assertFalse(incMoore.hasDefinitiveInformation(W_3));

        incMoore.insert(W_1, W_1_O);
        Assert.assertTrue(incMoore.hasDefinitiveInformation(W_1));
        Assert.assertTrue(incMoore.hasDefinitiveInformation(W_1.prefix(2)));
        Assert.assertFalse(incMoore.hasDefinitiveInformation(W_1.append('a')));

        WordBuilder<Character> wb = new WordBuilder<>();

        Assert.assertTrue(incMoore.lookup(W_1, wb));
        Assert.assertEquals(wb.toWord(), W_1_O);
        wb.clear();
        Assert.assertTrue(incMoore.lookup(W_1.prefix(2), wb));
        Assert.assertEquals(wb.toWord(), W_1_O.prefix(3));
        wb.clear();
        Assert.assertFalse(incMoore.hasDefinitiveInformation(W_2));
        Assert.assertFalse(incMoore.hasDefinitiveInformation(W_3));

        incMoore.insert(W_2, W_2_O);
        Assert.assertTrue(incMoore.hasDefinitiveInformation(W_1));
        Assert.assertTrue(incMoore.hasDefinitiveInformation(W_2));
        Assert.assertFalse(incMoore.hasDefinitiveInformation(W_3));

        Assert.assertTrue(incMoore.lookup(W_2, wb));
        Assert.assertEquals(wb.toWord(), W_2_O);
        wb.clear();
        Assert.assertTrue(incMoore.lookup(W_2.prefix(1), wb));
        Assert.assertEquals(wb.toWord(), W_2_O.prefix(2));
        wb.clear();
        Assert.assertTrue(incMoore.lookup(W_1, wb));
        Assert.assertEquals(wb.toWord(), W_1_O);
        wb.clear();

        incMoore.insert(W_3, W_3_O);
        Assert.assertTrue(incMoore.hasDefinitiveInformation(W_1));
        Assert.assertTrue(incMoore.hasDefinitiveInformation(W_2));
        Assert.assertTrue(incMoore.hasDefinitiveInformation(W_3));

        Assert.assertTrue(incMoore.lookup(W_3, wb));
        Assert.assertEquals(wb.toWord(), W_3_O);
        wb.clear();
        Assert.assertTrue(incMoore.lookup(W_3.prefix(2), wb));
        Assert.assertEquals(wb.toWord(), W_3_O.prefix(3));
        wb.clear();
        Assert.assertTrue(incMoore.lookup(W_1, wb));
        Assert.assertEquals(wb.toWord(), W_1_O);
        wb.clear();
        Assert.assertTrue(incMoore.lookup(W_2, wb));
        Assert.assertEquals(wb.toWord(), W_2_O);
        wb.clear();
    }

    @Test(dependsOnMethods = "testLookup")
    public void testInsertSame() {
        incMoore.insert(W_1, W_1_O);
    }

    @Test(expectedExceptions = ConflictException.class, dependsOnMethods = "testLookup")
    public void testConflict() {
        incMoore.insert(W_1, W_3_O);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testFindSeparatingWord() {
        CompactMoore<Character, Character> testMoore = new CompactMoore<>(TEST_ALPHABET);

        Word<Character> sepWord;
        sepWord = incMoore.findSeparatingWord(testMoore, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incMoore.findSeparatingWord(testMoore, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.epsilon());

        int s0 = testMoore.addInitialState('-');
        int s1 = testMoore.addState('x');
        int s2 = testMoore.addState('u');
        int s3 = testMoore.addState('y');
        int s4 = testMoore.addState('z');
        int s5 = testMoore.addState('w');

        testMoore.addTransition(s0, 'a', s1);
        testMoore.addTransition(s0, 'b', s2);
        testMoore.addTransition(s1, 'b', s3);
        testMoore.addTransition(s3, 'c', s4);
        testMoore.addTransition(s1, 'c', s5);

        sepWord = incMoore.findSeparatingWord(testMoore, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incMoore.findSeparatingWord(testMoore, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("acb"));

        testMoore.addTransition(s5, 'b', s2);
        sepWord = incMoore.findSeparatingWord(testMoore, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incMoore.findSeparatingWord(testMoore, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        testMoore.setTransition((Integer) s5, (Character) 'b', (Integer) s3);

        sepWord = incMoore.findSeparatingWord(testMoore, TEST_ALPHABET, true);
        Assert.assertEquals(sepWord, Word.fromString("acb"));
        sepWord = incMoore.findSeparatingWord(testMoore, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("acb"));
    }

    @Test(dependsOnMethods = "testLookup")
    public void testVisualization() throws IOException {
        final StringWriter dotWriter = new StringWriter();
        final StringWriter expectedWriter = new StringWriter();

        try (Reader reader = IOUtil.asBufferedUTF8Reader(AbstractIncrementalMooreBuilderTest.class.getResourceAsStream(
                getDOTResource()))) {

            GraphDOT.write(incMoore.asGraph(), dotWriter);
            IOUtil.copy(reader, expectedWriter);

            Assert.assertEquals(dotWriter.toString(), expectedWriter.toString());
        }
    }

    @Test(dependsOnMethods = "testLookup")
    public void testTSView() {
        final MooreTransitionSystem<?, Character, ?, Character> tsView = incMoore.asTransitionSystem();
        final WordBuilder<Character> wb = new WordBuilder<>();

        tsView.trace(W_1, wb);
        Assert.assertEquals(wb.toWord(), W_1_O);
        wb.clear();

        tsView.trace(W_2, wb);
        Assert.assertEquals(wb.toWord(), W_2_O);
        wb.clear();

        tsView.trace(W_3, wb);
        Assert.assertEquals(wb.toWord(), W_3_O);
    }

    @Test
    public void testCounterexampleOfLengthOne() {
        final IncrementalMooreBuilder<Character, Character> incMoore = createIncrementalMooreBuilder(TEST_ALPHABET);
        incMoore.insert(Word.fromLetter('a'), Word.fromString("-x"));

        final CompactMoore<Character, Character> moore = new CompactMoore<>(TEST_ALPHABET);
        final Integer q0 = moore.addInitialState('-');
        final Integer q1 = moore.addState('y');

        moore.addTransition(q0, 'a', q1);

        final Word<Character> ce = incMoore.findSeparatingWord(moore, TEST_ALPHABET, false);
        Assert.assertNotNull(ce);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testNewInputSymbol() {
        final GrowingAlphabet<Character> alphabet = new GrowingMapAlphabet<>(TEST_ALPHABET);
        final IncrementalMooreBuilder<Character, Character> growableBuilder = createIncrementalMooreBuilder(alphabet);

        growableBuilder.addAlphabetSymbol('d');
        growableBuilder.addAlphabetSymbol('d');

        final Word<Character> input1 = Word.fromString("dcba");
        final Word<Character> output1 = Word.fromString("-1234");

        growableBuilder.insert(input1, output1);

        Assert.assertTrue(growableBuilder.hasDefinitiveInformation(input1));
        Assert.assertEquals(growableBuilder.lookup(input1), output1);

        growableBuilder.addAlphabetSymbol('e');

        final Word<Character> input2 = Word.fromString("ddee");

        Assert.assertFalse(growableBuilder.hasDefinitiveInformation(input2));
        Assert.assertEquals(growableBuilder.lookup(input2), Word.fromString("-1"));
    }

}
