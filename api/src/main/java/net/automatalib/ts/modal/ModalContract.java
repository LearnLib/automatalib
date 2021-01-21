/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.ts.modal;

import java.util.Collection;

import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.graphs.TransitionEdge.Property;
import net.automatalib.automata.graphs.UniversalAutomatonGraphView;
import net.automatalib.automata.visualization.MCVisualizationHelper;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.ts.modal.transition.ModalContractEdgeProperty;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Alphabet;

/**
 * A modal contract is a {@link ModalTransitionSystem} that additionally allows to partition transitions in {@link
 * net.automatalib.ts.modal.transition.ModalContractEdgeProperty.EdgeColor#GREEN green}, {@link
 * net.automatalib.ts.modal.transition.ModalContractEdgeProperty.EdgeColor#RED red} or {@link
 * net.automatalib.ts.modal.transition.ModalContractEdgeProperty.EdgeColor#NONE uncolored} edges.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <TP>
 *         (specific) transition property type
 *
 * @author msc
 */
public interface ModalContract<S, I, T, TP extends ModalContractEdgeProperty>
        extends ModalTransitionSystem<S, I, T, TP> {

    /**
     * Get communication alphabet.
     * <p>
     * Returns the communication alphabet of this contract. It is a subset of {@link #getInputAlphabet()} and contains
     * all symbols, which are shared between parallel components and thus require synchronization.
     *
     * @return A possibly unmodifiable view of the communication alphabet.
     */
    Alphabet<I> getCommunicationAlphabet();

    @Override
    default UniversalGraph<S, TransitionEdge<I, T>, Void, Property<I, TP>> transitionGraphView(Collection<? extends I> inputs) {
        return new MCGraphView<>(this, inputs);
    }

    class MCGraphView<S, I, T, TP extends ModalContractEdgeProperty, M extends ModalContract<S, I, T, TP>>
            extends UniversalAutomatonGraphView<S, I, T, Void, TP, M> {

        public MCGraphView(M mc, Collection<? extends I> inputs) {
            super(mc, inputs);
        }

        @Override
        public VisualizationHelper<S, TransitionEdge<I, T>> getVisualizationHelper() {
            return new MCVisualizationHelper<>(automaton);
        }
    }
}
