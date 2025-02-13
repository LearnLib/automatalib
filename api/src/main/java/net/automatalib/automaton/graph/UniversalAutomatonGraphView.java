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
package net.automatalib.automaton.graph;

import java.util.Collection;

import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.graph.TransitionEdge.Property;
import net.automatalib.graph.UniversalGraph;

public class UniversalAutomatonGraphView<S, I, T, SP, TP, A extends UniversalAutomaton<S, I, T, SP, TP>>
        extends AutomatonGraphView<S, I, T, A>
        implements UniversalGraph<S, TransitionEdge<I, T>, SP, Property<I, TP>> {

    public UniversalAutomatonGraphView(A automaton, Collection<? extends I> inputs) {
        super(automaton, inputs);
    }

    @Override
    public SP getNodeProperty(S node) {
        return automaton.getStateProperty(node);
    }

    @Override
    public Property<I, TP> getEdgeProperty(TransitionEdge<I, T> edge) {
        return new TransitionEdge.Property<>(edge.getInput(), automaton.getTransitionProperty(edge.getTransition()));
    }

}
