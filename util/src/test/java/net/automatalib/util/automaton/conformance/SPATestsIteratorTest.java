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
package net.automatalib.util.automaton.conformance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.impl.StackSPA;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SPATestsIteratorTest {

    private final SPA<?, Character> spa;

    public SPATestsIteratorTest() {
        final Random random = new Random(42);
        final ProceduralInputAlphabet<Character> alphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('a', 'e'),
                                                     Alphabets.characters('A', 'C'),
                                                     'R');
        this.spa = RandomAutomata.randomSPA(random, alphabet, 10);
    }

    @Test
    public void testNonMinimalSPA() {

        final ProceduralInputAlphabet<Character> alphabet = this.spa.getInputAlphabet();

        final Alphabet<Character> extendedCalls = Alphabets.characters('A', 'D');
        final DefaultProceduralInputAlphabet<Character> extendedAlphabet =
                new DefaultProceduralInputAlphabet<>(alphabet.getInternalAlphabet(),
                                                     extendedCalls,
                                                     alphabet.getReturnSymbol());
        final SPA<?, Character> extendedSPA =
                new StackSPA<>(extendedAlphabet, this.spa.getInitialProcedure(), this.spa.getProcedures());

        final SPATestsIterator<Character> iter = new SPATestsIterator<>(extendedSPA, WpMethodTestsIterator::new);

        while (iter.hasNext()) {
            Word<Character> w = iter.next();
            Assert.assertEquals(extendedSPA.accepts(w), this.spa.accepts(w));
        }
    }

    @Test
    public void testWMethodVariant() {
        testIterator(WMethodTestsIterator::new);
    }

    @Test
    public void testWpMethodVariant() {
        testIterator(WpMethodTestsIterator::new);
    }

    private void testIterator(BiFunction<DFA<?, Character>, Collection<Character>, Iterator<Word<Character>>> conformanceTestProvider) {
        final List<Word<Character>> testTraces =
                Lists.newArrayList(new SPATestsIterator<>(this.spa, conformanceTestProvider));

        for (Entry<Character, DFA<?, Character>> e : this.spa.getProcedures().entrySet()) {
            verifyProcedure(e.getKey(), e.getValue(), this.spa.getInputAlphabet(), testTraces, conformanceTestProvider);
        }
    }

    private <I> void verifyProcedure(I procedure,
                                     DFA<?, I> dfa,
                                     ProceduralInputAlphabet<I> alphabet,
                                     Collection<Word<I>> globalTraces,
                                     BiFunction<DFA<?, I>, Collection<I>, Iterator<Word<I>>> conformanceTestProvider) {

        final Alphabet<I> proceduralAlphabet = alphabet.getProceduralAlphabet();
        final List<Word<I>> localTraces = new ArrayList<>();
        Iterators.addAll(localTraces, conformanceTestProvider.apply(dfa, proceduralAlphabet));

        for (Word<I> trace : globalTraces) {
            for (int i = 0; i < trace.length(); i++) {
                final I sym = trace.getSymbol(i);

                if (Objects.equals(procedure, sym)) {
                    final int returnIdx = alphabet.findReturnIndex(trace, i + 1);
                    final Word<I> wellMatched = trace.subWord(i + 1, returnIdx);
                    final Word<I> normalized = alphabet.project(wellMatched, 0);
                    localTraces.remove(normalized);
                }
            }
        }

        Assert.assertTrue(localTraces.isEmpty());
    }
}
