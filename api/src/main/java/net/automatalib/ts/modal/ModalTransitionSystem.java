/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
package net.automatalib.ts.modal;

import java.util.Collection;

import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.graph.TransitionEdge.Property;
import net.automatalib.automaton.graph.UniversalAutomatonGraphView;
import net.automatalib.automaton.visualization.MTSVisualizationHelper;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.visualization.VisualizationHelper;

/**
 * A modal transition system is a {@link UniversalAutomaton} in which transitions can be marked as
 * {@link ModalEdgeProperty.ModalType#MAY may} or {@link ModalEdgeProperty.ModalType#MUST must} edges. Since most
 * MTS-based algorithms require a finite transition domain, this includes a finite set input symbols. This is achieved
 * by sub-typing the {@link UniversalFiniteAlphabetAutomaton} interface, which additionally inherits several convenience
 * methods for working with MTSs.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <TP>
 *         (specific) transition property type
 */
public interface ModalTransitionSystem<S, I, T, TP extends ModalEdgeProperty>
        extends UniversalFiniteAlphabetAutomaton<S, I, T, Void, TP> {

    @Override
    default UniversalGraph<S, TransitionEdge<I, T>, Void, Property<I, TP>> transitionGraphView(Collection<? extends I> inputs) {
        return new MTSGraphView<>(this, inputs);
    }

    class MTSGraphView<S, I, T, TP extends ModalEdgeProperty, M extends ModalTransitionSystem<S, I, T, TP>>
            extends UniversalAutomatonGraphView<S, I, T, Void, TP, M> {

        public MTSGraphView(M mts, Collection<? extends I> inputs) {
            super(mts, inputs);
        }

        @Override
        public VisualizationHelper<S, TransitionEdge<I, T>> getVisualizationHelper() {
            return new MTSVisualizationHelper<>(automaton);
        }
    }
}
