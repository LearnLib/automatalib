/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.incremental.mealy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.mealy.tree.dynamic.StateLocalInputIncrementalMealyTreeBuilder;
import net.automatalib.util.automata.transducers.StateLocalInputMealyUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class StateLocalInputIncrementalMealyTreeBuilderTest {

    private static final Alphabet<Character> TEST_ALPHABET = Alphabets.characters('a', 'c');
    private static final Word<Character> W_1 = Word.fromString("abc");
    private static final Word<OutputAndLocalInputs<Character, Character>> W_1_O = toCompletelyEnabledTrace("xyz");
    private static final Word<Character> W_2 = Word.fromString("ac");
    private static final Word<OutputAndLocalInputs<Character, Character>> W_2_O = toCompletelyEnabledTrace("xw");
    private static final Word<Character> W_3 = Word.fromString("acb");
    private static final Word<OutputAndLocalInputs<Character, Character>> W_3_O = toCompletelyEnabledTrace("xwu");

    // Confluence Bug
    private static final Word<Character> W_B_1 = Word.fromString("aaa");
    private static final Word<OutputAndLocalInputs<Character, Character>> W_B_1_O = toCompletelyEnabledTrace("xxx");
    private static final Word<Character> W_B_2 = Word.fromString("bba");
    private static final Word<OutputAndLocalInputs<Character, Character>> W_B_2_O = toCompletelyEnabledTrace("xxx");
    private static final Word<Character> W_B_3 = Word.fromString("aabaa");
    private static final Word<OutputAndLocalInputs<Character, Character>> W_B_3_O = toCompletelyEnabledTrace("xxxxx");

    private StateLocalInputIncrementalMealyTreeBuilder<Character, Character> incMealy;

    private <I, O> StateLocalInputIncrementalMealyTreeBuilder<I, O> createIncrementalMealyBuilder(Alphabet<I> alphabet) {
        return new StateLocalInputIncrementalMealyTreeBuilder<>(alphabet);
    }

    private static Word<OutputAndLocalInputs<Character, Character>> toCompletelyEnabledTrace(String output) {
        final WordBuilder<OutputAndLocalInputs<Character, Character>> wb = new WordBuilder<>(output.length());

        for (int i = 0; i < output.length(); i++) {
            wb.add(new OutputAndLocalInputs<>(output.charAt(i), TEST_ALPHABET));
        }

        return wb.toWord();
    }

    @BeforeClass
    public void setUp() {
        this.incMealy = createIncrementalMealyBuilder(TEST_ALPHABET);
    }

    @Test
    public void testConfluenceBug() {
        incMealy.insert(W_B_1, W_B_1_O);
        incMealy.insert(W_B_2, W_B_2_O);
        incMealy.insert(W_B_3, W_B_3_O);

        Assert.assertFalse(incMealy.lookup(Word.fromString("aababaa"), new ArrayList<>()));
        // reset for further tests
        this.incMealy = createIncrementalMealyBuilder(TEST_ALPHABET);
    }

    @Test(dependsOnMethods = "testConfluenceBug")
    public void testLookup() {
        Assert.assertFalse(incMealy.hasDefinitiveInformation(W_1));
        Assert.assertFalse(incMealy.hasDefinitiveInformation(W_2));
        Assert.assertFalse(incMealy.hasDefinitiveInformation(W_3));

        incMealy.insert(W_1, W_1_O);
        Assert.assertTrue(incMealy.hasDefinitiveInformation(W_1));
        Assert.assertTrue(incMealy.hasDefinitiveInformation(W_1.prefix(2)));
        Assert.assertFalse(incMealy.hasDefinitiveInformation(W_1.append('a')));

        WordBuilder<OutputAndLocalInputs<Character, Character>> wb = new WordBuilder<>();

        Assert.assertTrue(incMealy.lookup(W_1, wb));
        Assert.assertEquals(wb.toWord(), W_1_O);
        wb.clear();
        Assert.assertTrue(incMealy.lookup(W_1.prefix(2), wb));
        Assert.assertEquals(wb.toWord(), W_1_O.prefix(2));
        wb.clear();
        Assert.assertFalse(incMealy.hasDefinitiveInformation(W_2));
        Assert.assertFalse(incMealy.hasDefinitiveInformation(W_3));

        incMealy.insert(W_2, W_2_O);
        Assert.assertTrue(incMealy.hasDefinitiveInformation(W_1));
        Assert.assertTrue(incMealy.hasDefinitiveInformation(W_2));
        Assert.assertFalse(incMealy.hasDefinitiveInformation(W_3));

        Assert.assertTrue(incMealy.lookup(W_2, wb));
        Assert.assertEquals(wb.toWord(), W_2_O);
        wb.clear();
        Assert.assertTrue(incMealy.lookup(W_2.prefix(1), wb));
        Assert.assertEquals(wb.toWord(), W_2_O.prefix(1));
        wb.clear();
        Assert.assertTrue(incMealy.lookup(W_1, wb));
        Assert.assertEquals(wb.toWord(), W_1_O);
        wb.clear();

        incMealy.insert(W_3, W_3_O);
        Assert.assertTrue(incMealy.hasDefinitiveInformation(W_1));
        Assert.assertTrue(incMealy.hasDefinitiveInformation(W_2));
        Assert.assertTrue(incMealy.hasDefinitiveInformation(W_3));

        Assert.assertTrue(incMealy.lookup(W_3, wb));
        Assert.assertEquals(wb.toWord(), W_3_O);
        wb.clear();
        Assert.assertTrue(incMealy.lookup(W_3.prefix(2), wb));
        Assert.assertEquals(wb.toWord(), W_3_O.prefix(2));
        wb.clear();
        Assert.assertTrue(incMealy.lookup(W_1, wb));
        Assert.assertEquals(wb.toWord(), W_1_O);
        wb.clear();
        Assert.assertTrue(incMealy.lookup(W_2, wb));
        Assert.assertEquals(wb.toWord(), W_2_O);
        wb.clear();
    }

    @Test(dependsOnMethods = "testLookup")
    public void testInsertSame() {
        incMealy.insert(W_1, W_1_O);
    }

    @Test(expectedExceptions = ConflictException.class, dependsOnMethods = "testLookup")
    public void testConflict() {
        incMealy.insert(W_1, W_3_O);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testFindSeparatingWord() {
        CompactMealy<Character, Character> testMealy = new CompactMealy<>(TEST_ALPHABET);

        int s0 = testMealy.addInitialState();
        int s1 = testMealy.addState();
        int s2 = testMealy.addState();
        int s3 = testMealy.addState();
        int s4 = testMealy.addState();
        int s5 = testMealy.addState();

        testMealy.addTransition(s0, 'a', s1, 'x');
        testMealy.addTransition(s0, 'b', s2, 'u');
        testMealy.addTransition(s1, 'b', s3, 'y');
        testMealy.addTransition(s3, 'c', s4, 'z');
        testMealy.addTransition(s1, 'c', s5, 'w');

        StateLocalInputMealyMachine<?, Character, ?, OutputAndLocalInputs<Character, Character>> transformedMealy =
                SLIMockUp.fromMealy(testMealy);

        // our wrapper is always complete, so non-/partial evaluation should not make a difference
        Word<Character> sepWord;
        sepWord = incMealy.findSeparatingWord(transformedMealy, TEST_ALPHABET, true);
        Assert.assertEquals(sepWord, Word.fromString("acb"));
        sepWord = incMealy.findSeparatingWord(transformedMealy, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("acb"));

        testMealy.addTransition(s5, 'b', s4, 'u');
        transformedMealy = SLIMockUp.fromMealy(testMealy);

        sepWord = incMealy.findSeparatingWord(transformedMealy, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incMealy.findSeparatingWord(transformedMealy, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        testMealy.setTransition(s5, (Character) 'b', testMealy.getSuccessor(s5, (Character) 'b'), (Character) 'w');
        transformedMealy = SLIMockUp.fromMealy(testMealy);

        sepWord = incMealy.findSeparatingWord(transformedMealy, TEST_ALPHABET, true);
        Assert.assertEquals(sepWord, Word.fromString("acb"));
        sepWord = incMealy.findSeparatingWord(transformedMealy, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("acb"));
    }

    @Test
    public void testCounterexampleOfLengthOne() {
        final StateLocalInputIncrementalMealyTreeBuilder<Character, Character> incMealy =
                createIncrementalMealyBuilder(TEST_ALPHABET);
        incMealy.insert(Word.fromCharSequence("a"), toCompletelyEnabledTrace("x"));

        final CompactMealy<Character, Character> mealy = new CompactMealy<>(TEST_ALPHABET);
        final Integer q0 = mealy.addInitialState();
        final Integer q1 = mealy.addState();

        mealy.addTransition(q0, 'a', q1, 'y');

        StateLocalInputMealyMachine<Integer, Character, ?, OutputAndLocalInputs<Character, Character>>
                transformedMealy = StateLocalInputMealyUtil.partialToObservableOutput(mealy);

        final Word<Character> ce = incMealy.findSeparatingWord(transformedMealy, TEST_ALPHABET, false);
        Assert.assertNotNull(ce);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testNonLocalInputs() {
        final StateLocalInputIncrementalMealyTreeBuilder<Character, Character> builder =
                createIncrementalMealyBuilder(TEST_ALPHABET);

        final Word<Character> input1 = Word.fromCharSequence("dcba");
        Word<OutputAndLocalInputs<Object, Object>> output1 =
                Word.fromList(Collections.nCopies(4, OutputAndLocalInputs.undefined()));

        Assert.assertTrue(builder.hasDefinitiveInformation(input1));
        Assert.assertEquals(builder.lookup(input1), output1);

        final Word<Character> input2 = Word.fromCharSequence("cba");
        final Word<OutputAndLocalInputs<Character, Character>> output2 = toCompletelyEnabledTrace("123");

        builder.insert(input2, output2);
        Assert.assertEquals(builder.lookup(input2), output2);

        final Word<Character> input3 = Word.fromCharSequence("cbda");

        Assert.assertTrue(builder.hasDefinitiveInformation(input3));
        Assert.assertEquals(builder.lookup(input3),
                            Word.fromSymbols(new OutputAndLocalInputs<>('1', TEST_ALPHABET),
                                             new OutputAndLocalInputs<>('2', TEST_ALPHABET),
                                             OutputAndLocalInputs.undefined(),
                                             OutputAndLocalInputs.undefined()));
    }

    private static class SLIMockUp<S, T, O>
            implements StateLocalInputMealyMachine<S, Character, T, OutputAndLocalInputs<Character, O>> {

        private final StateLocalInputMealyMachine<S, Character, T, OutputAndLocalInputs<Character, O>> reference;

        SLIMockUp(StateLocalInputMealyMachine<S, Character, T, OutputAndLocalInputs<Character, O>> reference) {
            this.reference = reference;
        }

        @Override
        public Collection<Character> getLocalInputs(S state) {
            return reference.getLocalInputs(state);
        }

        @Override
        public Collection<S> getStates() {
            return reference.getStates();
        }

        @Override
        public @Nullable OutputAndLocalInputs<Character, O> getTransitionOutput(T transition) {
            O orig = reference.getTransitionOutput(transition).getOutput();

            if (orig == null) {
                return null;
            }
            return new OutputAndLocalInputs<>(orig, TEST_ALPHABET);
        }

        @Override
        public @Nullable T getTransition(S state, Character input) {
            return reference.getTransition(state, input);
        }

        @Override
        public S getSuccessor(T transition) {
            return reference.getSuccessor(transition);
        }

        @Override
        public @Nullable S getInitialState() {
            return reference.getInitialState();
        }

        private static <O> StateLocalInputMealyMachine<?, Character, ?, OutputAndLocalInputs<Character, O>> fromMealy(
                CompactMealy<Character, O> qwe) {
            return new SLIMockUp<>(StateLocalInputMealyUtil.partialToObservableOutput(qwe));
        }
    }
}