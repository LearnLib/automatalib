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
package net.automatalib.automata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMoore;
import net.automatalib.automata.transout.impl.FastProbMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.probabilistic.ProbabilisticOutput;
import net.automatalib.commons.util.random.RandomUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class MutableAutomatonTest {

    private static final Alphabet<Integer> ALPHABET = Alphabets.integers(1, 6);
    private static final int SIZE = 10;
    private static final List<Boolean> STATE_PROPS = Arrays.asList(false, true);
    private static final List<Character> TRANS_PROPS = Arrays.asList('a', 'b', 'c');
    private static final List<ProbabilisticOutput<Character>> PROB_TRANS_PROPS =
            TRANS_PROPS.stream().map(p -> new ProbabilisticOutput<>(0.5f, p)).collect(Collectors.toList());
    private static final List<Void> EMPTY_PROPS = Collections.emptyList();
    private static final Random RANDOM = new Random(42);

    @Test
    public void testCompactDFA() {
        this.checkAutomaton(new CompactDFA.Creator<>(), ALPHABET, STATE_PROPS, EMPTY_PROPS);
    }

    @Test
    public void testCompactNFA() {
        this.checkAutomaton(new CompactNFA.Creator<>(), ALPHABET, STATE_PROPS, EMPTY_PROPS);
    }

    @Test
    public void testFastDFA() {
        this.checkAutomaton(FastDFA::new, ALPHABET, STATE_PROPS, EMPTY_PROPS);
    }

    @Test
    public void testFastNFA() {
        this.checkAutomaton(FastNFA::new, ALPHABET, STATE_PROPS, EMPTY_PROPS);
    }

    @Test
    public void testCompactMealy() {
        this.checkAutomaton(new CompactMealy.Creator<>(), ALPHABET, EMPTY_PROPS, TRANS_PROPS);
    }

    @Test
    public void testFastMealy() {
        this.checkAutomaton(FastMealy::new, ALPHABET, EMPTY_PROPS, TRANS_PROPS);
    }

    @Test
    public void testFastProbMealy() {
        this.checkAutomaton(FastProbMealy::new, ALPHABET, EMPTY_PROPS, PROB_TRANS_PROPS);
    }

    @Test
    public void testFastMoore() {
        this.checkAutomaton(FastMoore::new, ALPHABET, STATE_PROPS, EMPTY_PROPS);
    }

    private <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> void checkAutomaton(AutomatonCreator<M, I> creator,
                                                                                               Alphabet<I> alphabet,
                                                                                               List<SP> stateProps,
                                                                                               List<TP> transProps) {
        final M automaton = createAndCheckInitialAutomaton(creator, alphabet);

        fillRandomly(automaton, alphabet, stateProps, transProps);

        removeSingleTransitionAndCheck(automaton, alphabet);
        removeAllTransitionAndCheck(automaton, alphabet);
        clearAndCheck(automaton, alphabet);
    }

    private <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> M createAndCheckInitialAutomaton(
            AutomatonCreator<M, I> creator,
            Alphabet<I> alphabet) {

        final M automaton = creator.createAutomaton(alphabet, SIZE);

        for (int i = 0; i < SIZE; i++) {
            automaton.addState();
        }

        checkEmptyProperties(automaton, alphabet);

        return automaton;
    }

    private <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> void fillRandomly(M automaton,
                                                                                             Alphabet<I> alphabet,
                                                                                             List<SP> stateProps,
                                                                                             List<TP> transProps) {
        final StateIDs<S> stateIDs = automaton.stateIDs();

        for (final S s : automaton) {
            for (final I i : alphabet) {
                final TP tProp = RandomUtil.choose(transProps, RANDOM);
                final S succ = stateIDs.getState(RANDOM.nextInt(automaton.size()));
                final T trans = automaton.createTransition(succ, null);

                automaton.setTransitionProperty(trans, tProp);
                automaton.setTransitions(s, i, Collections.singleton(trans));
            }

            final SP sProp = RandomUtil.choose(stateProps, RANDOM);
            automaton.setStateProperty(s, sProp);
        }
    }

    private <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> void removeSingleTransitionAndCheck(M automaton,
                                                                                                               Alphabet<I> alphabet) {

        final int stateIndex = RANDOM.nextInt(automaton.size());
        final int inputIndex = RANDOM.nextInt(alphabet.size());
        final S s = automaton.stateIDs().getState(stateIndex);
        final I i = alphabet.getSymbol(inputIndex);

        final Object[] oldTpProps = buildTPSignature(automaton, alphabet);

        automaton.removeAllTransitions(s, i);

        Assert.assertTrue(automaton.getSuccessors(s, i).isEmpty());
        Assert.assertTrue(automaton.getTransitions(s, i).isEmpty());

        if (automaton instanceof MutableDeterministic) {
            @SuppressWarnings("unchecked")
            MutableDeterministic<S, I, T, SP, TP> detAutomaton = (MutableDeterministic<S, I, T, SP, TP>) automaton;

            Assert.assertNull(detAutomaton.getSuccessor(s, i));
            Assert.assertNull(detAutomaton.getTransition(s, i));
        }

        final Object[] newTpProps = buildTPSignature(automaton, alphabet);
        checkSignature(oldTpProps, newTpProps, stateIndex * alphabet.size() + inputIndex, 1);
    }

    private <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> void removeAllTransitionAndCheck(M automaton,
                                                                                                            Alphabet<I> alphabet) {

        final int stateIndex = RANDOM.nextInt(automaton.size());
        final S s = automaton.stateIDs().getState(stateIndex);

        final Object[] oldTpProps = buildTPSignature(automaton, alphabet);

        automaton.removeAllTransitions(s);

        for (final I i : alphabet) {
            Assert.assertTrue(automaton.getSuccessors(s, i).isEmpty());
            Assert.assertTrue(automaton.getTransitions(s, i).isEmpty());

            if (automaton instanceof MutableDeterministic) {
                @SuppressWarnings("unchecked")
                MutableDeterministic<S, I, T, SP, TP> detAutomaton = (MutableDeterministic<S, I, T, SP, TP>) automaton;

                Assert.assertNull(detAutomaton.getSuccessor(s, i));
                Assert.assertNull(detAutomaton.getTransition(s, i));
                Assert.assertNull(detAutomaton.getTransitionProperty(s, i));
            }
        }

        final Object[] newTpProps = buildTPSignature(automaton, alphabet);
        checkSignature(oldTpProps, newTpProps, stateIndex * alphabet.size(), alphabet.size());
    }

    private <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> void clearAndCheck(M automaton,
                                                                                              Alphabet<I> alphabet) {
        automaton.clear();

        Assert.assertTrue(automaton.getStates().isEmpty());

        for (int i = 0; i < SIZE; i++) {
            automaton.addState();
        }

        checkEmptyProperties(automaton, alphabet);

    }

    private <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> void checkEmptyProperties(M automaton,
                                                                                                     Alphabet<I> alphabet) {
        for (final S s : automaton) {
            for (final I i : alphabet) {
                Assert.assertTrue(automaton.getSuccessors(s, i).isEmpty());
                Assert.assertTrue(automaton.getTransitions(s, i).isEmpty());

                if (automaton instanceof MutableDeterministic) {
                    @SuppressWarnings("unchecked")
                    MutableDeterministic<S, I, T, SP, TP> detAutomaton =
                            (MutableDeterministic<S, I, T, SP, TP>) automaton;

                    Assert.assertNull(detAutomaton.getSuccessor(s, i));
                    Assert.assertNull(detAutomaton.getTransition(s, i));
                }
            }
        }

        Assert.assertTrue(automaton.getInitialStates().isEmpty());

        final Object[] oldTpProps = buildTPSignature(automaton, alphabet);
        checkSignature(oldTpProps, new Object[automaton.size() * alphabet.size()], 0, 0);
    }

    private <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> Object[] buildTPSignature(M automaton,
                                                                                                     Alphabet<I> alphabet) {

        final StateIDs<S> stateIDs = automaton.stateIDs();
        final Object[] result = new Object[automaton.size() * alphabet.size()];

        for (int i = 0; i < automaton.size(); i++) {
            for (int j = 0; j < alphabet.size(); j++) {
                final S state = stateIDs.getState(i);
                final I input = alphabet.getSymbol(j);
                final Object tpObject;

                if (automaton instanceof MutableDeterministic) {
                    @SuppressWarnings("unchecked")
                    MutableDeterministic<S, I, T, SP, TP> detAutomaton =
                            (MutableDeterministic<S, I, T, SP, TP>) automaton;
                    tpObject = detAutomaton.getTransitionProperty(state, input);
                } else {
                    final Collection<T> transitions = automaton.getTransitions(state, input);
                    if (transitions.isEmpty()) {
                        tpObject = null;
                    } else {
                        tpObject =
                                transitions.stream().map(automaton::getTransitionProperty).collect(Collectors.toList());
                    }

                }

                result[i * alphabet.size() + j] = tpObject;
            }
        }

        return result;
    }

    private void checkSignature(Object[] oldSignature, Object[] newSignature, int idxOfRemoval, int lengthOfRemoval) {

        Assert.assertEquals(newSignature.length, oldSignature.length);

        final int removalStart = idxOfRemoval;
        final int removalStop = idxOfRemoval + lengthOfRemoval;

        for (int i = 0; i < newSignature.length; i++) {
            if (i >= removalStart && i < removalStop) {
                Assert.assertNull(newSignature[i]);
            } else {
                Assert.assertEquals(newSignature[i], oldSignature[i]);
            }
        }
    }

}
