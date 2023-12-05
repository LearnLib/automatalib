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
package net.automatalib.automaton.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.FastNFA;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.automaton.transducer.impl.CompactSST;
import net.automatalib.automaton.transducer.impl.FastMealy;
import net.automatalib.automaton.transducer.impl.FastMoore;
import net.automatalib.automaton.transducer.probabilistic.ProbabilisticOutput;
import net.automatalib.automaton.transducer.probabilistic.impl.FastProbMealy;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.ts.modal.impl.CompactMTS;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.MutableModalEdgeProperty;
import net.automatalib.ts.modal.transition.impl.ModalEdgePropertyImpl;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MutableAutomatonTest {

    static final Random RANDOM = new Random(42);
    static final Alphabet<Integer> ALPHABET = Alphabets.integers(1, 6);
    static final int SIZE = 10;
    static final List<Boolean> STATE_PROPS = Arrays.asList(false, true);
    static final List<Character> TRANS_PROPS = Arrays.asList('a', 'b', 'c');
    static final List<ProbabilisticOutput<Character>> PROB_TRANS_PROPS =
            TRANS_PROPS.stream().map(p -> new ProbabilisticOutput<>(0.5f, p)).collect(Collectors.toList());
    static final List<MutableModalEdgeProperty> MTS_TRANS_PROPS = TRANS_PROPS.stream()
                                                                             .map(p -> new ModalEdgePropertyImpl(RANDOM.nextBoolean() ?
                                                                                                                         ModalType.MUST :
                                                                                                                         ModalType.MAY))
                                                                             .collect(Collectors.toList());
    static final List<Void> EMPTY_PROPS = Collections.emptyList();
    static final List<Word<Character>> SST_TRANS_PROPS = Arrays.asList(Word.fromString("xy"), Word.fromString("yz"));
    static final List<Word<Character>> SST_STATE_PROPS = Arrays.asList(Word.fromString("ab"), Word.fromString("bc"));

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
    public void testCompactMoore() {
        this.checkAutomaton(new CompactMoore.Creator<>(), ALPHABET, STATE_PROPS, EMPTY_PROPS);
    }

    @Test
    public void testFastMoore() {
        this.checkAutomaton(FastMoore::new, ALPHABET, STATE_PROPS, EMPTY_PROPS);
    }

    @Test
    public void testCompactMTS() {
        this.checkAutomaton(CompactMTS::new, ALPHABET, EMPTY_PROPS, MTS_TRANS_PROPS);
    }

    @Test
    public void testCompactSST() {
        this.checkAutomaton(CompactSST::new, ALPHABET, SST_STATE_PROPS, SST_TRANS_PROPS);
    }

    protected <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> void checkAutomaton(AutomatonCreator<M, I> creator,
                                                                                                 Alphabet<I> alphabet,
                                                                                                 List<SP> stateProps,
                                                                                                 List<? extends TP> transProps) {
        final M automaton = createInitialAutomaton(creator, alphabet);

        checkEmptyProperties(automaton, alphabet);
        fillRandomly(automaton, alphabet, stateProps, transProps);
        addInitialAndCheck(automaton, stateProps);

        removeSingleTransitionAndCheck(automaton, alphabet);
        removeAllTransitionAndCheck(automaton, alphabet);
        clearAndCheck(automaton, alphabet);
    }

    protected <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> M createInitialAutomaton(AutomatonCreator<M, I> creator,
                                                                                                      Alphabet<I> alphabet) {

        final M automaton = creator.createAutomaton(alphabet, SIZE);

        for (int i = 0; i < SIZE; i++) {
            automaton.addState();
        }

        return automaton;
    }

    static <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> void fillRandomly(M automaton,
                                                                                            Alphabet<I> alphabet,
                                                                                            List<SP> stateProps,
                                                                                            List<? extends TP> transProps) {
        final StateIDs<S> stateIDs = automaton.stateIDs();

        for (S s : automaton) {
            for (I i : alphabet) {
                final TP tProp = RandomUtil.choose(RANDOM, transProps);
                final S succ = stateIDs.getState(RANDOM.nextInt(automaton.size()));
                final T trans = automaton.createTransition(succ, null);

                automaton.setTransitionProperty(trans, tProp);
                automaton.setTransitions(s, i, Collections.singleton(trans));
            }

            final SP sProp = RandomUtil.choose(RANDOM, stateProps);
            automaton.setStateProperty(s, sProp);
        }
    }

    private <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> void addInitialAndCheck(M automaton,
                                                                                                   List<SP> stateProps) {
        final S tmp = RandomUtil.choose(RANDOM, new ArrayList<>(automaton.getStates()));
        final SP tmpSp = automaton.getStateProperty(tmp);
        final SP sp = RandomUtil.choose(RANDOM, stateProps);

        final S init = automaton.addInitialState(sp);

        Assert.assertEquals(automaton.getInitialStates().size(), 1);
        Assert.assertEquals(automaton.getStateProperty(automaton.getInitialStates().iterator().next()), sp);

        final boolean expectException = automaton instanceof UniversalDeterministicAutomaton;

        try {
            final S init2 = automaton.addInitialState();
            Assert.assertEquals(automaton.getInitialStates().size(), 2);
            automaton.setInitial(init2, false);
        } catch (IllegalStateException ise) {
            Assert.assertTrue(expectException);
        }

        automaton.setInitial(init, false);
        automaton.setInitial(tmp, true);
        Assert.assertEquals(automaton.getStateProperty(automaton.getInitialStates().iterator().next()), tmpSp);
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

        for (I i : alphabet) {
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
        for (S s : automaton) {
            for (I i : alphabet) {
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
