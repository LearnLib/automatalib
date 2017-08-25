/* Copyright (C) 2013-2017 TU Dortmund
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
package net.automatalib.util.automata.asgraph;

import java.util.Collection;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.graphs.UniversalGraph;

public class UniversalAutomatonAsGraph<S, I, T, SP, TP, A extends UniversalAutomaton<S, I, T, SP, TP>>
        extends AutomatonAsGraph<S, I, T, A>
        implements UniversalGraph<S, TransitionEdge<I, T>, SP, TransitionEdge.Property<I, TP>> {

    public UniversalAutomatonAsGraph(A automaton, Collection<? extends I> inputs) {
        super(automaton, inputs);
    }

    @Override
    public SP getNodeProperty(S node) {
        return automaton.getStateProperty(node);
    }

    @Override
    public TransitionEdge.Property<I, TP> getEdgeProperty(TransitionEdge<I, T> edge) {
        return new TransitionEdge.Property<>(edge.getInput(), automaton.getTransitionProperty(edge.getTransition()));
    }

}
