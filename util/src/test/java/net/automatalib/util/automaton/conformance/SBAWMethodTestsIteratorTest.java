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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.impl.StackSBA;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.copy.AutomatonCopyMethod;
import net.automatalib.util.automaton.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automaton.procedural.ATSequences;
import net.automatalib.util.automaton.procedural.SBAs;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class SBAWMethodTestsIteratorTest {

    private final Random random;
    private final SBA<?, Character> sba;

    public SBAWMethodTestsIteratorTest() {
        final ProceduralInputAlphabet<Character> alphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('a', 'e'),
                                                     Alphabets.characters('A', 'C'),
                                                     'R');

        this.random = new Random(42);
        this.sba = RandomAutomata.randomSBA(random, alphabet, 10);
    }

    @Test
    public void testNonMinimalSBA() {

        final ProceduralInputAlphabet<Character> alphabet = this.sba.getInputAlphabet();

        final Alphabet<Character> extendedCalls = Alphabets.characters('A', 'D');
        final DefaultProceduralInputAlphabet<Character> extendedAlphabet =
                new DefaultProceduralInputAlphabet<>(alphabet.getInternalAlphabet(),
                                                     extendedCalls,
                                                     alphabet.getReturnSymbol());
        final SBA<?, Character> extendedSBA =
                new StackSBA<>(extendedAlphabet, this.sba.getInitialProcedure(), this.sba.getProcedures());

        final SBAWMethodTestsIterator<Character> iter = new SBAWMethodTestsIterator<>(extendedSBA);

        while (iter.hasNext()) {
            Word<Character> w = iter.next();
            Assert.assertEquals(extendedSBA.accepts(w), this.sba.accepts(w));
        }
    }

    @Test
    public void testIterator() {
        final ProceduralInputAlphabet<Character> alphabet = this.sba.getInputAlphabet();
        final List<Word<Character>> testWords = IteratorUtil.list(new SBAWMethodTestsIterator<>(sba));
        final ATSequences<Character> atSequences = SBAs.computeATSequences(this.sba);
        final List<Character> continuableSymbols = new ArrayList<>(alphabet.size() - 1);
        continuableSymbols.addAll(alphabet.getInternalAlphabet());
        continuableSymbols.addAll(atSequences.terminatingSequences.keySet());

        for (Entry<Character, DFA<?, Character>> e : this.sba.getProcedures().entrySet()) {
            testWithModifiedProcedure(this.sba, e.getKey(), e.getValue(), continuableSymbols, testWords, random);
        }
    }

    private <S, I> void testWithModifiedProcedure(SBA<?, I> sba,
                                                  I procedure,
                                                  DFA<S, I> dfa,
                                                  Collection<I> continuableSymbols,
                                                  List<Word<I>> testWords,
                                                  Random random) {

        final ProceduralInputAlphabet<I> alphabet = sba.getInputAlphabet();

        for (S s : dfa) {
            if (!isSinkOrSuccessSink(dfa, alphabet, s)) { // ensure validity of modified SBA
                for (I i : continuableSymbols) {
                    final CompactDFA<I> newDFA = new CompactDFA<>(alphabet);
                    final Mapping<S, Integer> mapping =
                            AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, dfa, alphabet, newDFA);

                    final int oldTarget = newDFA.getSuccessor(mapping.get(s), i);
                    int newTarget;

                    do { // do not remap identically
                        newTarget = random.nextInt(newDFA.size());
                    } while (oldTarget == newTarget);

                    newDFA.setTransition(mapping.get(s), i, Integer.valueOf(newTarget));

                    final Map<I, DFA<?, I>> newP = new HashMap<>(sba.getProcedures());
                    newP.put(procedure, newDFA);

                    final StackSBA<?, I> copy = new StackSBA<>(alphabet, sba.getInitialProcedure(), newP);

                    Assert.assertFalse(Automata.testEquivalence(dfa, newDFA, alphabet));
                    Assert.assertFalse(SBAs.testEquivalence(sba, copy, alphabet));
                    Assert.assertFalse(testEquivalence(sba, copy, testWords));
                }
            }
        }
    }

    private <S, I> boolean isSinkOrSuccessSink(DFA<S, I> dfa, Alphabet<I> alphabet, S s) {

        final S toAnalyze;

        if (dfa.isAccepting(s)) {
            final S succ = dfa.getSuccessor(s, alphabet.getSymbol(0));
            for (I i : alphabet) {
                if (!Objects.equals(dfa.getSuccessor(succ, i), succ)) {
                    return false;
                }
            }
            toAnalyze = succ;
        } else {
            toAnalyze = s;
        }

        if (dfa.isAccepting(toAnalyze)) {
            return false;
        }

        for (I i : alphabet) {
            if (!Objects.equals(dfa.getSuccessor(toAnalyze, i), toAnalyze)) {
                return false;
            }
        }

        return true;
    }

    private <I> boolean testEquivalence(SBA<?, I> orig, SBA<?, I> copy, Collection<Word<I>> testWords) {
        for (Word<I> w : testWords) {
            if (orig.accepts(w) != copy.accepts(w)) {
                return false;
            }
        }

        return true;
    }
}
