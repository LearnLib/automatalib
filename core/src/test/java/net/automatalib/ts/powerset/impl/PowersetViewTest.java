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
package net.automatalib.ts.powerset.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.fsa.impl.FastNFA;
import net.automatalib.automaton.fsa.impl.FastNFAState;
import net.automatalib.ts.AcceptorPowersetViewTS;
import net.automatalib.ts.PowersetViewTS;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.acceptor.AcceptorTS;
import net.automatalib.ts.powerset.AcceptorPowersetView;
import net.automatalib.ts.powerset.DeterministicAcceptorPowersetView;
import net.automatalib.ts.powerset.DeterministicPowersetView;
import net.automatalib.ts.powerset.PowersetView;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PowersetViewTest {

    @Test
    public void testPowerset() {
        final FastNFA<Character> system = constructSystem(FastNFA::new);
        final PowersetView<FastNFAState, Character, FastNFAState> powersetView = new PowersetView<>(system);
        checkConstructedSystem(system, powersetView, system.getInputAlphabet());
        checkConstructedSystem(powersetView, powersetView.powersetView(), system.getInputAlphabet());
    }

    @Test
    public void testDeterministicPowerset() {
        final FastNFA<Character> system = constructSystem(FastNFA::new);
        final AcceptorPowersetViewTS<Set<FastNFAState>, Character, FastNFAState> detSystem = system.powersetView();
        final DeterministicPowersetView<Set<FastNFAState>, Character, Set<FastNFAState>> powersetView =
                new DeterministicPowersetView<>(detSystem);
        checkConstructedSystem(detSystem, powersetView, system.getInputAlphabet());
        checkConstructedSystem(powersetView, powersetView.powersetView(), system.getInputAlphabet());
    }

    @Test
    public void testAcceptorPowerset() {
        final FastNFA<Character> system = constructSystem(FastNFA::new);
        final AcceptorPowersetView<FastNFAState, Character> powersetView = new AcceptorPowersetView<>(system);
        checkConstructedSystem(system, powersetView, system.getInputAlphabet());
        checkConstructedSystem(powersetView, powersetView.powersetView(), system.getInputAlphabet());
    }

    @Test
    public void testDeterministicAcceptorPowerset() {
        final FastNFA<Character> system = constructSystem(FastNFA::new);
        final AcceptorPowersetViewTS<Set<FastNFAState>, Character, FastNFAState> detSystem = system.powersetView();
        final DeterministicAcceptorPowersetView<Set<FastNFAState>, Character> powersetView =
                new DeterministicAcceptorPowersetView<>(detSystem);
        checkConstructedSystem(detSystem, powersetView, system.getInputAlphabet());
        checkConstructedSystem(powersetView, powersetView.powersetView(), system.getInputAlphabet());
    }

    @Test
    public void testCompactNFAPowerset() {
        final CompactNFA<Character> system = constructSystem(CompactNFA::new);
        checkConstructedSystem(system, system.powersetView(), system.getInputAlphabet());
    }

    @Test
    public void testFastNFAPowerset() {
        final FastNFA<Character> system = constructSystem(FastNFA::new);
        checkConstructedSystem(system, system.powersetView(), system.getInputAlphabet());
    }

    @Test
    public void testFastPowersetDTS() {
        final FastNFA<Character> system = constructSystem(FastNFA::new);
        checkConstructedSystem(system, new FastPowersetDTS<>(system), system.getInputAlphabet());
    }

    private static <S, A extends MutableNFA<S, Character>> A constructSystem(AutomatonCreator<A, Character> creator) {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final A nfa = creator.createAutomaton(alphabet);

        final S q1 = nfa.addInitialState(false);
        final S q2 = nfa.addState(true);
        final S q3 = nfa.addState(false);

        nfa.setTransitions(q1, 'a', Arrays.asList(q1, q2, q3));
        nfa.setTransitions(q2, 'b', Arrays.asList(q2, q3));
        nfa.setTransitions(q3, 'c', Collections.singleton(q3));

        return nfa;
    }

    private static <S, T, SO, TO> void checkConstructedSystem(TransitionSystem<SO, Character, TO> transitionSystem,
                                                              PowersetViewTS<S, Character, T, SO, TO> view,
                                                              Alphabet<Character> alphabet) {
        Assert.assertEquals(view.getOriginalStates(view.getInitialState()), transitionSystem.getInitialStates());

        final List<Word<Character>> traces = Arrays.asList(Word.epsilon(),
                                                           Word.fromLetter('a'),
                                                           Word.fromSymbols('a', 'b'),
                                                           Word.fromSymbols('a', 'b', 'c'));

        for (Word<Character> trace : traces) {
            final Set<SO> states = transitionSystem.getStates(trace);
            final S state = view.getState(trace);

            // check equality without order
            Assert.assertEquals(view.getOriginalStates(state).size(), states.size(), trace.toString());
            Assert.assertTrue(view.getOriginalStates(state).containsAll(states), trace.toString());
            Assert.assertTrue(states.containsAll(view.getOriginalStates(state)), trace.toString());

            for (Character i : alphabet) {
                final List<TO> transitions = new ArrayList<>();
                for (SO s : states) {
                    transitions.addAll(transitionSystem.getTransitions(s, i));
                }
                final T transition = view.getTransition(state, i);

                // check equality without order
                Assert.assertEquals(transitions.size(), view.getOriginalTransitions(transition).size(), trace.toString());
                Assert.assertTrue(view.getOriginalTransitions(transition).containsAll(transitions), trace.toString());
                Assert.assertTrue(transitions.containsAll(view.getOriginalTransitions(transition)), trace.toString());
            }

            if (transitionSystem instanceof AcceptorTS && view instanceof AcceptorPowersetViewTS) {
                @SuppressWarnings("unchecked")
                final AcceptorTS<SO, Character> acceptorTS = (AcceptorTS<SO, Character>) transitionSystem;
                @SuppressWarnings("unchecked")
                final AcceptorPowersetViewTS<S, Character, SO> acceptorPowersetViewTS =
                        (AcceptorPowersetViewTS<S, Character, SO>) view;

                Assert.assertEquals(acceptorTS.isAccepting(states),
                                    acceptorPowersetViewTS.isAccepting(state),
                                    trace.toString());
            }
        }
    }

}
