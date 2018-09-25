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
package net.automatalib.util.automata.conformance;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IncrementalWMethodTestsIteratorTest {

    private static final int MAX_DEPTH = 2;

    private Alphabet<Character> alphabet;
    private CompactMealy<Character, Character> mealy;

    private IncrementalWMethodTestsIterator<Character> incIt;

    private Set<Word<Character>> initialWMethodTests;

    @BeforeClass
    public void setUp() throws Exception {
        alphabet = Alphabets.characters('a', 'c');

        // @formatter:off
        mealy = AutomatonBuilders.<Character, Character>newMealy(alphabet)
                .withInitial("q0")
                .from("q0")
                    .on('a', 'c').withOutput('x').to("q1")
                    .on('b').withOutput('y').loop()
                .from("q1")
                    .on('a').withOutput('z').to("q2")
                    .on('b').withOutput('y').loop()
                    .on('c').withOutput('w').to("q0")
                .from("q2")
                    .on('a', 'b', 'c').withOutput('u').loop()
                .create();
        // @formatter:on

        incIt = new IncrementalWMethodTestsIterator<>(alphabet);
        incIt.setMaxDepth(MAX_DEPTH);
        incIt.update(mealy);

        initialWMethodTests = computeWMethodTests();
    }

    @Test
    public void testInitialCover() {
        final Set<Word<Character>> iteratorTests = computeIteratorTests();

        Assert.assertEquals(initialWMethodTests, iteratorTests);
    }

    @Test(dependsOnMethods = "testInitialCover")
    public void testIncrementalCover() {

        final Integer q2 = 2;
        final Integer q3 = mealy.addState();

        mealy.addTransition(q3, 'a', q3, 'x');
        mealy.addTransition(q3, 'b', q3, 'y');
        mealy.addTransition(q3, 'c', q3, 'z');
        mealy.setTransition(q2, (Character) 'c', q3, mealy.getOutput(q2, 'c'));

        incIt.update(mealy);

        final Set<Word<Character>> wMethodTests = computeWMethodTests();
        final Set<Word<Character>> iteratorTests = computeIteratorTests();

        wMethodTests.removeAll(initialWMethodTests);
        iteratorTests.removeAll(initialWMethodTests);

        Assert.assertEquals(wMethodTests, iteratorTests);
    }

    private Set<Word<Character>> computeWMethodTests() {

        final List<Word<Character>> characterizingSet = Automata.characterizingSet(mealy, alphabet);
        final List<Word<Character>> allMidTuples = Streams.stream(CollectionsUtil.allTuples(alphabet, 0, MAX_DEPTH))
                                                          .map(Word::fromList)
                                                          .collect(Collectors.toList());
        final List<Word<Character>> transitionCover = Automata.transitionCover(mealy, alphabet);

        final Iterable<List<Word<Character>>> wMethodIter =
                CollectionsUtil.allCombinations(transitionCover, allMidTuples, characterizingSet);

        return Streams.stream(wMethodIter).map(l -> {
            final WordBuilder<Character> wb = new WordBuilder<>();
            l.forEach(wb::append);
            return wb.toWord();
        }).collect(Collectors.toSet());
    }

    private Set<Word<Character>> computeIteratorTests() {
        return Streams.stream(incIt).collect(Collectors.toSet());
    }

}
