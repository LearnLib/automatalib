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
package net.automatalib.util.automaton.cover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.compact.CompactDFA;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.util.graph.Graphs;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

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

    @Test
    public void testPartialIncrementalCover() {
        // initial configuration
        int q0 = dfa.addInitialState();
        dfa.addTransition(q0, 0, q0);
        dfa.addTransition(q0, 1, q0);

        expectedNewStateCover.add(Word.epsilon());
        expectedNewTransCover.add(Word.fromLetter(0));
        expectedNewTransCover.add(Word.fromLetter(1));
        expectedNewStructuralCover.addAll(expectedNewStateCover);
        expectedNewStructuralCover.addAll(expectedNewTransCover);

        Covers.cover(dfa, alphabet, newStates, newTransitions);
        Covers.structuralCover(dfa, alphabet, newStructural);

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
        Covers.incrementalStructuralCover(dfa, alphabet, structuralCover, newStructural);

        checkCovers();
        updateAndClearCovers();

        // third configuration
        int q2 = dfa.addState();
        dfa.addTransition(q2, 0, q2);
        dfa.addTransition(q2, 1, q2);
        dfa.addTransition(q2, 2, q2);

        dfa.setTransition(q1, 2, q2);
        dfa.setTransition(q0, 2, q2);

        expectedNewStateCover.add(Word.fromSymbols(0, 2));
        expectedNewTransCover.add(Word.fromLetter(2));
        expectedNewTransCover.add(Word.fromSymbols(0, 2, 0));
        expectedNewTransCover.add(Word.fromSymbols(0, 2, 1));
        expectedNewTransCover.add(Word.fromSymbols(0, 2, 2));
        expectedNewStructuralCover.addAll(expectedNewTransCover);

        Covers.incrementalCover(dfa, alphabet, stateCover, transCover, newStates, newTransitions);
        Covers.incrementalStructuralCover(dfa, alphabet, structuralCover, newStructural);

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

    @Test
    public void testCovers() {
        final Random random = new Random(42);
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final DFA<?, Integer> dfa = RandomAutomata.randomDFA(random, 15, alphabet);

        testStateCover(dfa, alphabet, Automata.stateCover(dfa, alphabet));
        testTransitionCover(dfa, alphabet, Automata.transitionCover(dfa, alphabet));
    }

    @Test
    public void testPartialIterators() {
        final Random random = new Random(42);
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final CompactDFA<Integer> dfa = new CompactDFA<>(alphabet);

        RandomAutomata.randomDeterministic(random,
                                           15,
                                           Alphabets.integers(0, 4),
                                           DFA.STATE_PROPERTIES,
                                           DFA.TRANSITION_PROPERTIES,
                                           dfa);

        dfa.addState(true);
        dfa.addState(false);

        final List<Word<Integer>> sCov = Lists.newArrayList(Covers.stateCoverIterator(dfa, alphabet));
        final List<Word<Integer>> tCov = Lists.newArrayList(Covers.transitionCoverIterator(dfa, alphabet));

        testStateCover(dfa, alphabet, sCov);
        testTransitionCover(dfa, alphabet, tCov);
    }

    private static <S, I> void testStateCover(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                              Alphabet<I> alphabet,
                                              Collection<Word<I>> cover) {

        final Set<S> states = new HashSet<>(automaton.getStates());

        for (Word<I> w : cover) {
            Assert.assertTrue(states.remove(automaton.getState(w)));
        }

        final Mapping<S, ? extends @Nullable Collection<?>> mapping =
                Graphs.incomingEdges(automaton.transitionGraphView(alphabet));

        for (S s : states) {
            final Collection<?> incoming = mapping.get(s);
            Assert.assertTrue(incoming == null || incoming.isEmpty());
        }
    }

    private static <S, I, T> void testTransitionCover(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton,
                                                      Alphabet<I> alphabet,
                                                      Collection<Word<I>> cover) {

        final List<T> transitions = new ArrayList<>(automaton.size() * alphabet.size());

        for (S s : automaton.getStates()) {
            for (I i : alphabet) {
                T t = automaton.getTransition(s, i);
                if (t != null) {
                    transitions.add(t);
                }
            }
        }

        Assert.assertEquals(cover.size(), transitions.size()); // make sure we only cover defined transitions

        for (Word<I> w : cover) {
            final S s = automaton.getState(w.prefix(-1));
            Assert.assertTrue(transitions.remove(automaton.getTransition(s, w.lastSymbol())));
        }

        Assert.assertTrue(transitions.isEmpty()); // make sure we cover all defined transitions
    }
}
