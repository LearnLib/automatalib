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
package net.automatalib.util.automaton.builder;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.automaton.transducer.impl.CompactSST;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AutomatonBuilderTest {

    @Test
    public void testNFABuilder() {
        final Alphabet<Integer> alphabet = Alphabets.integers(1, 6);

        // @formatter:off
        final CompactNFA<Integer> nfa = AutomatonBuilders.newNFA(alphabet)
                .withInitial("i0", "i1", "i2")
                .withAccepting("s0")
                .from("i0").on(1, 2).to("sink", "s0")
                .from("i1", "i2").on(3, 4, 5, 6).to("sink", "s0")
                .from("s0").on(1, 2, 3, 4, 5, 6).to("sink")
                .create();
        // @formatter:on

        Assert.assertEquals(nfa.size(), 5);
        Assert.assertFalse(nfa.accepts(Word.epsilon()));

        for (Integer i1 : alphabet) {
            final Word<Integer> w = Word.fromLetter(i1);
            Assert.assertTrue(nfa.accepts(w));
            Assert.assertEquals(nfa.getStates(w).size(), 2);
            for (Integer i2 : alphabet) {
                Assert.assertFalse(nfa.accepts(Word.fromSymbols(i1, i2)));
            }
        }
    }

    @Test
    public void testMooreBuilder() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'b');

        // @formatter:off
        final CompactMoore<Character, String> moore = AutomatonBuilders.<Character, String>newMoore(alphabet)
                                                                     .withInitial("s0", "Hello")
                                                                     .withOutput("s1", "World")
                                                                     .withOutput("s2", "!")
                                                                     .from("s0").on('a').to("s1")
                                                                     .from("s1").on('a').to("s2")
                                                                     .from("s2").on('b').loop()
                                                                     .create();
        // @formatter:on

        Assert.assertEquals(moore.size(), 3);
        Assert.assertEquals(moore.computeOutput(Word.fromString("aa")), Word.fromSymbols("Hello", "World", "!"));
        Assert.assertEquals(moore.computeOutput(Word.fromString("aaa")), Word.fromSymbols("Hello", "World", "!"));
        Assert.assertEquals(moore.computeOutput(Word.fromString("aab")), Word.fromSymbols("Hello", "World", "!", "!"));
        Assert.assertEquals(moore.computeOutput(Word.fromString("ab")), Word.fromSymbols("Hello", "World"));
        Assert.assertEquals(moore.computeOutput(Word.fromString("a")), Word.fromSymbols("Hello", "World"));
        Assert.assertEquals(moore.computeOutput(Word.fromString("b")), Word.fromLetter("Hello"));
        Assert.assertEquals(moore.computeOutput(Word.epsilon()), Word.fromLetter("Hello"));
    }

    @Test
    public void testSSTBuilder() {
        final Alphabet<Integer> alphabet = Alphabets.integers(1, 2);

        // @formatter:off
        final CompactSST<Integer, Character> sst = AutomatonBuilders.<Integer, Character>newSST(alphabet)
                                                                    .withInitial("s0")
                                                                    .withStateProperty("s0", Word.fromString("Hel"))
                                                                    .withStateProperty("s1", Word.fromString("Wo"))
                                                                    .withStateProperty("s2", Word.fromLetter('!'))
                                                                    .from("s0").on(1).withProperty(Word.fromString("lo")).to("s1")
                                                                    .from("s1").on(1).withProperty(Word.fromString("rld")).to("s2")
                                                                    .from("s2").on(2).withProperty(Word.epsilon()).loop()
                                                                    .create();
        // @formatter:on

        Assert.assertEquals(sst.size(), 3);
        Assert.assertEquals(sst.computeOutput(Word.fromSymbols(1, 1)), Word.fromString("lorld!"));
        Assert.assertEquals(sst.computeOutput(Word.fromSymbols(1, 1, 1)), Word.fromString("lorld"));
        Assert.assertEquals(sst.computeOutput(Word.fromSymbols(1, 1, 2)), Word.fromString("lorld!"));
        Assert.assertEquals(sst.computeOutput(Word.fromSymbols(1, 2)), Word.fromString("lo"));
        Assert.assertEquals(sst.computeOutput(Word.fromSymbols(1)), Word.fromString("loWo"));
        Assert.assertEquals(sst.computeOutput(Word.fromSymbols(2)), Word.epsilon());
        Assert.assertEquals(sst.computeOutput(Word.epsilon()), Word.fromString("Hel"));

    }
}
