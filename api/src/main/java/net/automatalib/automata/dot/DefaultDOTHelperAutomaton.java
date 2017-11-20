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
package net.automatalib.automata.dot;

import java.util.Collection;
import java.util.Map;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.graphs.dot.DefaultDOTHelper;

public class DefaultDOTHelperAutomaton<S, I, T, A extends Automaton<S, I, T>>
        extends DefaultDOTHelper<S, TransitionEdge<I, T>> {

    protected final A automaton;

    public DefaultDOTHelperAutomaton(A automaton) {
        this.automaton = automaton;
    }

    @Override
    protected Collection<S> initialNodes() {
        return automaton.getInitialStates();
    }

    @Override
    public boolean getEdgeProperties(S src, TransitionEdge<I, T> edge, S tgt, Map<String, String> properties) {
        if (!super.getEdgeProperties(src, edge, tgt, properties)) {
            return false;
        }
        String label = String.valueOf(edge.getInput());
        properties.put(EdgeAttrs.LABEL, label);
        return true;
    }

}
