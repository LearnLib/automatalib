/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.automaton;

import java.util.Collection;

import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.graph.TransitionEdge.Property;
import net.automatalib.automaton.graph.UniversalAutomatonGraphView;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.semantic.FiniteSemantics;
import net.automatalib.ts.UniversalTransitionSystem;

/**
 * A universal automaton is a generalized representation of automata, with unified access to the properties of states
 * and transitions. See {@link UniversalTransitionSystem} for a further explanation of this concept.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 */
public interface UniversalAutomaton<S, I, T, SP, TP>
        extends Automaton<S, I, T>, UniversalTransitionSystem<S, I, T, SP, TP> {

    @Override
    default UniversalGraph<S, TransitionEdge<I, T>, SP, Property<I, TP>> transitionGraphView(Collection<? extends I> inputs) {
        return new UniversalAutomatonGraphView<>(this, inputs);
    }

    /**
     * Convenience interface that describes an automaton with finite syntactic and finite semantic state space. This
     * type links a {@link UniversalAutomaton} with {@link FiniteSemantics}.
     *
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     */
    interface RegularAutomaton<S, I, T, SP, TP>
            extends UniversalAutomaton<S, I, T, SP, TP>, Automaton.RegularAutomaton<S, I, T> {

        @Override
        default UniversalAutomaton<S, I, T, SP, TP> getSemantics() {
            return this;
        }
    }

}
