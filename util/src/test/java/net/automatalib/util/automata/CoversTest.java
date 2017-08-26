/* Copyright (C) 2013-2017 TU Dortmund
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
package net.automatalib.util.automata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Malte Isberner
 * @author frohme
 */
@Test
public class CoversTest {

    private final Alphabet<Integer> alphabet = Alphabets.integers(0, 2);
    final CompactDFA<Integer> dfa = new CompactDFA<>(alphabet);

    private final List<Word<Integer>> stateCover = new ArrayList<>();
    private final List<Word<Integer>> transCover = new ArrayList<>();
    private final List<Word<Integer>> structuralCover = new ArrayList<>();

    private final List<Word<Integer>> newStates = new ArrayList<>();
    private final List<Word<Integer>> newTransitions = new ArrayList<>();
    private final List<Word<Integer>> newStructural = new ArrayList<>();

    private final List<Word<Integer>> expectedNewStateCover = new ArrayList<>();
    private final List<Word<Integer>> expectedNewTransCover = new ArrayList<>();
    private final List<Word<Integer>> expectedNewStructuralCover = new ArrayList<>();

    private static final List<Word<Integer>> EMPTY = Collections.emptyList();

    @Test
    public void testIncrementalCover() {
        // initial configuration
        int q0 = dfa.addInitialState();
        dfa.addTransition(q0, 0, q0);
        dfa.addTransition(q0, 1, q0);
        dfa.addTransition(q0, 2, q0);

        expectedNewStateCover.add(Word.epsilon());
        expectedNewTransCover.add(Word.fromLetter(0));
        expectedNewTransCover.add(Word.fromLetter(1));
        expectedNewTransCover.add(Word.fromLetter(2));
        expectedNewStructuralCover.addAll(expectedNewStateCover);
        expectedNewStructuralCover.addAll(expectedNewTransCover);

        Covers.incrementalCover(dfa, alphabet, EMPTY, EMPTY, newStates, newTransitions);
        Covers.incrementalCover(dfa, alphabet, EMPTY, EMPTY, newStructural, newStructural);

        checkCovers();
        updateAndClearCovers();

        // second configuration
        int q1 = dfa.addState();
        dfa.addTransition(q1, 0, q1);
        dfa.addTransition(q1, 1, q0);
        dfa.addTransition(q1, 2, q1);

        dfa.setTransition(q0, 0, q1);

        expectedNewStateCover.add(Word.fromLetter(0));
        expectedNewTransCover.add(Word.fromSymbols(0, 0));
        expectedNewTransCover.add(Word.fromSymbols(0, 1));
        expectedNewTransCover.add(Word.fromSymbols(0, 2));
        expectedNewStructuralCover.addAll(expectedNewTransCover);

        Covers.incrementalCover(dfa, alphabet, stateCover, transCover, newStates, newTransitions);
        Covers.incrementalCover(dfa, alphabet, structuralCover, EMPTY, newStructural, newStructural);

        checkCovers();
        updateAndClearCovers();

        // third configuration
        int q2 = dfa.addState();
        dfa.addTransition(q2, 0, q2);
        dfa.addTransition(q2, 1, q2);
        dfa.addTransition(q2, 2, q2);

        dfa.setTransition(q1, 2, q2);
        dfa.setTransition(q0, 2, q2);

        expectedNewStateCover.add(Word.fromLetter(2));
        expectedNewTransCover.add(Word.fromSymbols(2, 0));
        expectedNewTransCover.add(Word.fromSymbols(2, 1));
        expectedNewTransCover.add(Word.fromSymbols(2, 2));
        expectedNewStructuralCover.addAll(expectedNewTransCover);

        Covers.incrementalCover(dfa, alphabet, stateCover, transCover, newStates, newTransitions);
        Covers.incrementalCover(dfa, alphabet, structuralCover, EMPTY, newStructural, newStructural);

        checkCovers();
    }

    private void checkCovers() {
        Assert.assertEquals(newStates, expectedNewStateCover);
        Assert.assertEquals(newTransitions, expectedNewTransCover);
        Assert.assertEquals(newStructural, expectedNewStructuralCover);
    }

    private void updateAndClearCovers() {
        stateCover.addAll(newStates);
        transCover.addAll(newTransitions);
        structuralCover.addAll(newStructural);

        newStates.clear();
        newTransitions.clear();
        newStructural.clear();

        expectedNewStateCover.clear();
        expectedNewTransCover.clear();
        expectedNewStructuralCover.clear();
    }

}
