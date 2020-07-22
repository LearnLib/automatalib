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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.modal.transitions.ModalContractEdgeProperty;
import net.automatalib.ts.modal.transitions.ModalEdgeProperty;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.transitions.MutableModalEdgeProperty;
import net.automatalib.ts.modal.MutableModalTransitionSystem;
import net.automatalib.ts.modal.Transition;
import net.automatalib.util.fixedpoint.WorksetMappingAlgorithm;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author msc
 */
class ModalParallelComposition<A extends MutableModalTransitionSystem<S, I, T, TP>, S, S0, S1, I, T, T0, T1, TP extends MutableModalEdgeProperty, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty>
        implements WorksetMappingAlgorithm<Pair<S0, S1>, S, A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModalParallelComposition.class);
    private static final float LOAD_FACTOR = 0.5f;

    private final ModalTransitionSystem<S0, I, T0, TP0> mc0;
    private final ModalTransitionSystem<S1, I, T1, TP1> mc1;

    private final A result;

    ModalParallelComposition(ModalTransitionSystem<S0, I, T0, TP0> mc0,
                             ModalTransitionSystem<S1, I, T1, TP1> mc1,
                             AutomatonCreator<A, I> output) {

        this.mc0 = mc0;
        this.mc1 = mc1;

        final Alphabet<I> alphabet;

        if (mc0.getInputAlphabet().equals(mc1.getInputAlphabet())) {
            alphabet = mc0.getInputAlphabet();
        } else {
            final GrowingAlphabet<I> growingAlphabet = new GrowingMapAlphabet<>(mc0.getInputAlphabet());
            growingAlphabet.addAll(mc1.getInputAlphabet());
            alphabet = growingAlphabet;
        }

        LOGGER.debug("New alphabet for composition: {}", alphabet);

        result = output.createAutomaton(alphabet);

    }

    @Override
    public int expectedElementCount() {
        return (int) (LOAD_FACTOR * mc0.size() * mc1.size());
    }

    @Override
    public Collection<Pair<S0,S1>> initialize(Map<Pair<S0, S1>, S> mapping) {
        Collection<Pair<S0,S1>> initialElements = new ArrayList<>(mc0.getInitialStates().size() * mc1.getInitialStates().size());
        for (final S0 s0 : mc0.getInitialStates()) {
            for (final S1 s1 : mc1.getInitialStates()) {
                final Pair<S0, S1> init = Pair.of(s0, s1);
                final S newState = result.addInitialState();

                mapping.put(init, newState);
                initialElements.add(init);
            }
        }
        return initialElements;
    }


    @Override
    public Collection<Pair<S0, S1>> update(Map<Pair<S0, S1>, S> mapping, Pair<S0, S1> currentTuple) {
        ArrayList<Pair<S0, S1>> discovered = new ArrayList<>();
        List<Transition<Pair<S0, S1>, I, ModalEdgeProperty.ModalType>> transitions = generateNewTransitions(currentTuple);

        for (Transition<Pair<S0, S1>, I, ModalEdgeProperty.ModalType> transition : transitions) {
            LOGGER.debug("discovered new transition: " + transition);

            S mappedTarget;
            if (mapping.containsKey(transition.getTarget())) {
                mappedTarget = mapping.get(transition.getTarget());
            }
            else {
                mappedTarget = result.addState();
                mapping.put(transition.getTarget(), mappedTarget);
                discovered.add(transition.getTarget());
            }
            result.addModalTransition(mapping.get(currentTuple), transition.getLabel(), mappedTarget, transition.getProperty());
        }
        return discovered;
    }

    protected List<Transition<Pair<S0, S1>, I, ModalEdgeProperty.ModalType>> generateNewTransitions(Pair<S0, S1> productState) {
        List<Transition<Pair<S0, S1>, I, ModalEdgeProperty.ModalType>> newTransitions = new ArrayList<>();

        for (I symbol : mc0.getInputAlphabet()) {
            for (T0 transition : mc0.getTransitions(productState.getFirst(), symbol)) {

                if (!mc1.getInputAlphabet().contains(symbol)) {
                    newTransitions.add(new Transition<>(productState,
                                                        symbol,
                                                        Pair.of(mc0.getTarget(transition),
                                                                productState.getSecond()),
                                                        mc0.getTransitionProperty(transition).getType()));
                }
                else {
                    for (T1 partnerTransition : mc1.getTransitions(productState.getSecond(), symbol)) {
                        newTransitions.add(new Transition<>(productState,
                                                            symbol,
                                                            Pair.of(mc0.getTarget(transition),
                                                                    mc1.getTarget(partnerTransition)),
                                                            minimalCompatibleType(mc0.getTransitionProperty(transition).getType(),
                                                                                  mc1.getTransitionProperty(partnerTransition).getType())));
                    }
                }
            }
        }

        Set<I> alphabetDifference = new HashSet<>(mc1.getInputAlphabet());
        alphabetDifference.removeAll(mc0.getInputAlphabet());

        for (I symbol : alphabetDifference) {
            for (T1 transition : mc1.getTransitions(productState.getSecond(), symbol)) {
                newTransitions.add(new Transition<>(productState,
                                                    symbol,
                                                    Pair.of(productState.getFirst(),
                                                            mc1.getTarget(transition)),
                                                    mc1.getTransitionProperty(transition).getType()));
            }
        }

        return newTransitions;
    }

    private static ModalEdgeProperty.ModalType minimalCompatibleType(ModalContractEdgeProperty.ModalType arg0, ModalContractEdgeProperty.ModalType arg1) {
        if (arg0 == ModalEdgeProperty.ModalType.MUST && arg1 == ModalEdgeProperty.ModalType.MUST) {
            return ModalEdgeProperty.ModalType.MUST;
        }
        else {
            return ModalEdgeProperty.ModalType.MAY;
        }
    }

    @Override
    public A result() {
        return result;
    }

}
