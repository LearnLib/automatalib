/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.util.ts.modal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.modal.ModalEdgeProperty;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.MutableModalEdgeProperty;
import net.automatalib.ts.modal.MutableModalTransitionSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author msc
 */
class ModalConjunction<A extends MutableModalTransitionSystem<S, I, T, TP>, S, S0, S1, I, T, T0, T1, TP extends MutableModalEdgeProperty, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty>
        implements WorksetMappingAlgorithm<Pair<S0, S1>, S, A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModalConjunction.class);
    private static final float LOAD_FACTOR = 0.5f;

    private final ModalTransitionSystem<S0, I, T0, TP0> mc0;
    private final ModalTransitionSystem<S1, I, T1, TP1> mc1;

    private final A result;

    ModalConjunction(ModalTransitionSystem<S0, I, T0, TP0> mc0,
                     ModalTransitionSystem<S1, I, T1, TP1> mc1,
                     AutomatonCreator<A, I> creator) {

        if (!mc0.getInputAlphabet().equals(mc1.getInputAlphabet())) {
            throw new IllegalArgumentException("conjunction input mts have to have the same input alphabet");
        }

        this.mc0 = mc0;
        this.mc1 = mc1;

        this.result = creator.createAutomaton(mc0.getInputAlphabet());
    }

    @Override
    public int expectedElementCount() {
        return (int) (LOAD_FACTOR * mc0.size() * mc1.size());
    }

    @Override
    public void initialize(Deque<Pair<S0, S1>> stack, Map<Pair<S0, S1>, S> mapping) {
        for (final S0 s0 : mc0.getInitialStates()) {
            for (final S1 s1 : mc1.getInitialStates()) {
                final Pair<S0, S1> init = Pair.of(s0, s1);
                final S newState = result.addInitialState();

                mapping.put(init, newState);
                stack.addLast(init);
            }
        }
    }

    @Override
    public Collection<Pair<S0, S1>> update(Map<Pair<S0, S1>, S> mapping, Pair<S0, S1> currentStatePair, S mappedState) {
        assert mappedState != null;

        LOGGER.debug("current tuple: {} (-> {})", currentStatePair, mappedState);

        ArrayList<Pair<S0, S1>> discovered = new ArrayList<>();

        for (I sym : mc0.getInputAlphabet()) {

            LOGGER.debug("current symbol: {}", sym);

            Collection<T0> transitions0 = mc0.getTransitions(currentStatePair.getFirst(), sym);
            Collection<T1> transitions1 = (mc1.getInputAlphabet().containsSymbol(sym) ?
                    mc1.getTransitions(currentStatePair.getSecond(), sym) :
                    Collections.emptySet());

            if (transitions0.isEmpty()) {
                LOGGER.debug("\tno transition 0 -> continue with next symbol");
            }

            for (T0 transition0 : transitions0) {

                LOGGER.debug("current transition 0: {}", transition0);

                if (mc0.getTransitionProperty(transition0).isMust() && transitions1.isEmpty()) {
                    throw new IllegalArgumentException("error in conjunction");
                }
                for (T1 transition1 : transitions1) {

                    LOGGER.debug("current transition 1: {}", transition1);

                    Pair<S0, S1> newTuple = Pair.of(mc0.getSuccessor(transition0), mc1.getSuccessor(transition1));

                    S newState = mapping.get(newTuple);
                    if (newState == null) {
                        newState = result.addState();
                        mapping.put(newTuple, newState);
                        LOGGER.debug("new mapping: {} -> {}", newTuple, newState);
                        discovered.add(newTuple);
                    }

                    T newT = result.createTransition(newState, null);
                    result.addTransition(mappedState, sym, newT);

                    LOGGER.debug("new transition: {}", newT);

                    if (mc0.getTransitionProperty(transition0).isMust() ||
                        mc1.getTransitionProperty(transition1).isMust()) {
                        result.getTransitionProperty(newT).setMust();
                    } else {
                        result.getTransitionProperty(newT).setMayOnly();
                    }
                }
            }
        }

        for (I sym : mc1.getInputAlphabet()) {

            LOGGER.debug("current symbol: {}", sym);

            Collection<T0> transitions0 = (mc0.getInputAlphabet().containsSymbol(sym) ?
                    mc0.getTransitions(currentStatePair.getFirst(), sym) :
                    Collections.emptySet());
            Collection<T1> transitions1 = mc1.getTransitions(currentStatePair.getSecond(), sym);

            if (transitions1.isEmpty()) {
                LOGGER.debug("\tno transition 1 -> continue with next symbol");
            }

            for (T1 transition1 : transitions1) {

                LOGGER.debug("current transition 1: {}", transition1);

                if (mc1.getTransitionProperty(transition1).isMust() && transitions0.isEmpty()) {
                    throw new IllegalArgumentException("error in conjunction");
                }
            }
        }
        return discovered;
    }

    @Override
    public A result() {
        return result;
    }

}
