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
package net.automatalib.util.automaton.conformance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.fsa.impl.compact.CompactDFA;
import net.automatalib.common.util.collection.CollectionsUtil;
import net.automatalib.common.util.comparison.CmpUtil;
import net.automatalib.util.automaton.cover.Covers;
import net.automatalib.util.automaton.equivalence.CharacterizingSets;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Alphabet;
import net.automatalib.word.Word;
import net.automatalib.word.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WpMethodTestsIteratorTest {

    private final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
    private final CompactDFA<Integer> dfa = RandomAutomata.randomDFA(new Random(42), 10, alphabet);

    @Test
    public void testEpsilonDiscriminator() {

        final CompactDFA<Integer> dfa1 = new CompactDFA<>(dfa);
        final Integer oldInit1 = dfa1.getInitialState();
        dfa1.setInitial(oldInit1, false);
        dfa1.addInitialState(true);

        final CompactDFA<Integer> dfa2 = new CompactDFA<>(dfa);
        final Integer oldInit2 = dfa2.getInitialState();
        dfa2.setInitial(oldInit2, false);
        dfa2.addInitialState(false);

        final List<Word<Integer>> testWords = Lists.newArrayList(new WpMethodTestsIterator<>(dfa1, alphabet, 0));

        for (Word<Integer> t : testWords) {
            if (dfa1.accepts(t) != dfa2.accepts(t)) {
                return;
            }
        }

        Assert.fail("WpMethod did not detect difference in two different automata");
    }

    @Test
    public void testIteratorWithoutMiddleParts() {
        final List<Word<Integer>> iteratorWords = Lists.newArrayList(new WpMethodTestsIterator<>(dfa, alphabet, 0));
        final List<Word<Integer>> wpMethodWords =
                generateWpMethodTest(dfa, alphabet, Collections.singletonList(Word.epsilon()));

        checkTestWords(iteratorWords, wpMethodWords);
    }

    @Test
    public void testIteratorWithMiddleParts() {

        final int depth = 3;

        final List<Word<Integer>> iteratorWords = Lists.newArrayList(new WpMethodTestsIterator<>(dfa, alphabet, depth));
        final List<Word<Integer>> wpMethodWords = generateWpMethodTest(dfa,
                                                                       alphabet,
                                                                       Streams.stream(CollectionsUtil.allTuples(alphabet,
                                                                                                                0,
                                                                                                                depth))
                                                                              .map(Word::fromList)
                                                                              .collect(Collectors.toList()));

        checkTestWords(iteratorWords, wpMethodWords);
    }

    private <I extends Comparable<I>> void checkTestWords(List<Word<I>> actual, List<Word<I>> expected) {
        actual.sort(CmpUtil.lexComparator());
        expected.sort(CmpUtil.lexComparator());

        Assert.assertEquals(actual, expected);

    }

    private <S, I> List<Word<I>> generateWpMethodTest(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                      Collection<? extends I> inputs,
                                                      List<Word<I>> middleParts) {

        final List<Word<I>> stateCover = new ArrayList<>(automaton.size());
        final List<Word<I>> transitionCover = new ArrayList<>(automaton.size() * inputs.size());
        final List<Word<I>> characterizingSet = new ArrayList<>();

        Covers.cover(automaton, inputs, stateCover, transitionCover);
        CharacterizingSets.findCharacterizingSet(automaton, inputs, characterizingSet);

        Assert.assertFalse(stateCover.isEmpty());
        Assert.assertFalse(transitionCover.isEmpty());
        Assert.assertFalse(characterizingSet.isEmpty());

        final List<Word<I>> result = new ArrayList<>();

        // Phase 1: state cover * middle part * global suffixes
        Lists.cartesianProduct(stateCover, middleParts, characterizingSet)
             .stream()
             .map(Word::fromWords)
             .forEach(result::add);

        // Phase 2: transitions (not in state cover) * middle part * local suffixes
        transitionCover.removeAll(stateCover);

        for (Word<I> prefix : transitionCover) {
            for (Word<I> middle : middleParts) {
                final Word<I> prefixWithMiddle = prefix.concat(middle);
                final S s = automaton.getState(prefixWithMiddle);

                final List<Word<I>> suffixes = new ArrayList<>();
                CharacterizingSets.findCharacterizingSet(automaton, inputs, s, suffixes);
                assert !suffixes.isEmpty();

                for (Word<I> suffix : suffixes) {
                    result.add(prefixWithMiddle.concat(suffix));
                }
            }
        }

        return result;
    }
}
