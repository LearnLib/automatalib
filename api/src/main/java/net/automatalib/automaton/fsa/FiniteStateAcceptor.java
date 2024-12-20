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
package net.automatalib.automaton.fsa;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.concept.OutputAutomaton;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.graph.UniversalAutomatonGraphView;
import net.automatalib.automaton.visualization.FSAVisualizationHelper;
import net.automatalib.common.util.collection.IterableUtil;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.ts.acceptor.AcceptorTS;
import net.automatalib.visualization.VisualizationHelper;

/**
 * {@code FiniteStateAcceptor}s accept regular languages.
 */
public interface FiniteStateAcceptor<S, I> extends AcceptorTS<S, I>,
                                                   UniversalAutomaton<S, I, S, Boolean, Void>,
                                                   OutputAutomaton<S, I, S, Boolean>,
                                                   SuffixOutput<I, Boolean> {

    List<Boolean> STATE_PROPERTIES = Arrays.asList(Boolean.FALSE, Boolean.TRUE);
    List<Void> TRANSITION_PROPERTIES = Collections.singletonList(null);

    @Override
    default Boolean computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
        Iterable<I> input = IterableUtil.concat(prefix, suffix);
        return computeOutput(input);
    }

    @Override
    default Boolean computeOutput(Iterable<? extends I> input) {
        return accepts(input);
    }

    @Override
    default UniversalGraph<S, TransitionEdge<I, S>, Boolean, TransitionEdge.Property<I, Void>> transitionGraphView(
            Collection<? extends I> inputs) {
        return new FSAGraphView<>(this, inputs);
    }

    class FSAGraphView<S, I, A extends FiniteStateAcceptor<S, I>>
            extends UniversalAutomatonGraphView<S, I, S, Boolean, Void, A> {

        public FSAGraphView(A automaton, Collection<? extends I> inputs) {
            super(automaton, inputs);
        }

        @Override
        public VisualizationHelper<S, TransitionEdge<I, S>> getVisualizationHelper() {
            return new FSAVisualizationHelper<>(automaton);
        }
    }
}
