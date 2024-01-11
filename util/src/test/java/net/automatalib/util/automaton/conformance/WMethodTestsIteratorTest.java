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
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.common.util.collection.CollectionsUtil;
import net.automatalib.common.util.collection.IterableUtil;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class WMethodTestsIteratorTest {

    private final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
    private final CompactDFA<Integer> dfa = RandomAutomata.randomDFA(new Random(42), 5, alphabet);

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

        final List<Word<Integer>> testWords = IteratorUtil.list(new WMethodTestsIterator<>(dfa1, alphabet, 0));

        for (Word<Integer> t : testWords) {
            if (dfa1.accepts(t) != dfa2.accepts(t)) {
                return;
            }
        }

        Assert.fail("WMethod did not detect difference in two different automata");
    }

    @Test
    public void testVanillaIterator() {
        final List<Word<Integer>> transCover = Automata.transitionCover(dfa, alphabet);
        Assert.assertFalse(transCover.contains(Word.epsilon()));
        transCover.add(Word.epsilon());
        final List<Word<Integer>> characterizingSet = Automata.characterizingSet(dfa, alphabet);

        final List<Word<Integer>> expectedWords =
                IterableUtil.stream(CollectionsUtil.cartesianProduct(transCover, characterizingSet))
                            .map(Word::fromWords)
                            .collect(Collectors.toList());

        this.verifyIterator(new WMethodTestsIterator<>(dfa, alphabet, 0), expectedWords);
    }

    @Test
    public void testIteratorWithLookahead2() {
        final int lookahead = 2;

        final List<Word<Integer>> transCover = Automata.transitionCover(dfa, alphabet);
        Assert.assertFalse(transCover.contains(Word.epsilon()));
        transCover.add(Word.epsilon());
        final Iterable<Word<Integer>> middleTuples =
                IterableUtil.map(CollectionsUtil.allTuples(alphabet, 0, lookahead), Word::fromList);
        final List<Word<Integer>> characterizingSet = Automata.characterizingSet(dfa, alphabet);

        final List<Word<Integer>> expectedWords =
                IterableUtil.stream(CollectionsUtil.cartesianProduct(transCover, middleTuples, characterizingSet))
                            .map(Word::fromWords)
                            .collect(Collectors.toList());

        this.verifyIterator(new WMethodTestsIterator<>(dfa, alphabet, lookahead), expectedWords);
    }

    private void verifyIterator(WMethodTestsIterator<Integer> iter, Collection<Word<Integer>> expectedTests) {
        final List<Word<Integer>> expectedWMethodWords = new ArrayList<>(expectedTests);
        final List<Word<Integer>> wMethodWords = IteratorUtil.list(iter);

        // Order may be different, but that is ok
        expectedWMethodWords.sort(Word.canonicalComparator(Integer::compare));
        wMethodWords.sort(Word.canonicalComparator(Integer::compare));

        Assert.assertEquals(wMethodWords, expectedWMethodWords);
    }
}
