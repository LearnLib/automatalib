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
package net.automatalib.util.automaton.conformance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.ProceduralOutputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.impl.DefaultProceduralOutputAlphabet;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.procedural.impl.StackSPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.copy.AutomatonCopyMethod;
import net.automatalib.util.automaton.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automaton.procedural.ATSequences;
import net.automatalib.util.automaton.procedural.SPMMs;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class SPMMWMethodTestsIteratorTest {

    private final Random random;
    private final SPMM<?, Character, ?, Character> spmm;

    public SPMMWMethodTestsIteratorTest() {
        final ProceduralInputAlphabet<Character> inputAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('a', 'e'),
                                                     Alphabets.characters('A', 'C'),
                                                     'R');
        final ProceduralOutputAlphabet<Character> outputAlphabet =
                new DefaultProceduralOutputAlphabet<>(Alphabets.characters('x', 'z'), '-');

        this.random = new Random(42);
        this.spmm = RandomAutomata.randomSPMM(this.random, inputAlphabet, outputAlphabet, 10);
    }

    @Test
    public void testNonMinimalSPMM() {

        final ProceduralInputAlphabet<Character> alphabet = this.spmm.getInputAlphabet();

        final Alphabet<Character> extendedCalls = Alphabets.characters('A', 'D');
        final ProceduralInputAlphabet<Character> extendedAlphabet =
                new DefaultProceduralInputAlphabet<>(alphabet.getInternalAlphabet(),
                                                     extendedCalls,
                                                     alphabet.getReturnSymbol());
        final Character initialOutput =
                this.spmm.computeOutput(Word.fromLetter(this.spmm.getInitialProcedure())).firstSymbol();

        final SPMM<?, Character, ?, Character> extendedSPMM = new StackSPMM<>(extendedAlphabet,
                                                                              this.spmm.getInitialProcedure(),
                                                                              initialOutput,
                                                                              this.spmm.getErrorOutput(),
                                                                              this.spmm.getProcedures());

        final SPMMWMethodTestsIterator<Character, Character> iter = new SPMMWMethodTestsIterator<>(extendedSPMM);

        while (iter.hasNext()) {
            Word<Character> w = iter.next();
            Assert.assertEquals(extendedSPMM.computeOutput(w), this.spmm.computeOutput(w));
        }
    }

    @Test
    public void testIterator() {
        final List<Word<Character>> testWords = IteratorUtil.list(new SPMMWMethodTestsIterator<>(this.spmm));

        final ProceduralInputAlphabet<Character> inputAlphabet = this.spmm.getInputAlphabet();
        final ATSequences<Character> atSequences = SPMMs.computeATSequences(this.spmm);
        final List<Character> continuableSymbols = new ArrayList<>(inputAlphabet.size() - 1);
        continuableSymbols.addAll(inputAlphabet.getInternalAlphabet());
        continuableSymbols.addAll(atSequences.terminatingSequences.keySet());

        for (Entry<Character, MealyMachine<?, Character, ?, Character>> e : spmm.getProcedures().entrySet()) {
            testWithModifiedProcedure(this.spmm, e.getKey(), e.getValue(), continuableSymbols, testWords, this.random);
        }
    }

    private <S, I, O> void testWithModifiedProcedure(SPMM<?, I, ?, O> spmm,
                                                     I procedure,
                                                     MealyMachine<S, I, ?, O> mealy,
                                                     Collection<I> continuableSymbols,
                                                     List<Word<I>> testWords,
                                                     Random random) {

        final ProceduralInputAlphabet<I> inputAlphabet = spmm.getInputAlphabet();
        final I init = spmm.getInitialProcedure();

        for (S s : mealy) {
            if (!isSink(mealy, inputAlphabet, s)) { // ensure validity of modified SPMM
                for (I i : continuableSymbols) {
                    if (!spmm.isErrorOutput(mealy.getOutput(s, i))) {
                        final CompactMealy<I, O> newMealy = new CompactMealy<>(inputAlphabet);
                        final Mapping<S, Integer> mapping =
                                AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE,
                                                           mealy,
                                                           inputAlphabet,
                                                           newMealy);

                        final int oldTarget = newMealy.getSuccessor(mapping.get(s), i);
                        int newTarget;

                        do { // do not remap identically
                            newTarget = random.nextInt(newMealy.size());
                        } while (oldTarget == newTarget);

                        newMealy.setTransition(mapping.get(s), i, Integer.valueOf(newTarget), mealy.getOutput(s, i));

                        final Map<I, MealyMachine<?, I, ?, O>> newP = new HashMap<>(spmm.getProcedures());
                        newP.put(procedure, newMealy);

                        final SPMM<?, I, ?, O> copy = new StackSPMM<>(inputAlphabet,
                                                                      init,
                                                                      spmm.computeOutput(Word.fromLetter(init))
                                                                          .lastSymbol(),
                                                                      spmm.getErrorOutput(),
                                                                      newP);

                        Assert.assertFalse(Automata.testEquivalence(mealy, newMealy, inputAlphabet));
                        Assert.assertFalse(SPMMs.testEquivalence(spmm, copy, inputAlphabet));
                        Assert.assertFalse(testEquivalence(spmm, copy, testWords));
                    }
                }
            }
        }
    }

    private <S, I, T, O> boolean isSink(MealyMachine<S, I, T, O> mealy, ProceduralInputAlphabet<I> inputAlphabet, S s) {

        for (I i : inputAlphabet) {
            final T t = mealy.getTransition(s, i);

            if (!Objects.equals(mealy.getSuccessor(t), s)) {
                return false;
            }
        }

        return true;
    }

    private <I, O> boolean testEquivalence(SPMM<?, I, ?, O> orig,
                                           SPMM<?, I, ?, O> copy,
                                           Collection<Word<I>> testWords) {
        for (Word<I> w : testWords) {
            if (!Objects.equals(orig.computeOutput(w), copy.computeOutput(w))) {
                return false;
            }
        }

        return true;
    }
}
