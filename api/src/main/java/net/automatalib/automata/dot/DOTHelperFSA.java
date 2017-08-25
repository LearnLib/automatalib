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

import java.util.Map;

import net.automatalib.automata.fsa.FiniteStateAcceptor;

public class DOTHelperFSA<S, I> extends DefaultDOTHelperAutomaton<S, I, S, FiniteStateAcceptor<S, I>> {

    public DOTHelperFSA(FiniteStateAcceptor<S, I> automaton) {
        super(automaton);
    }

    /*
     * (non-Javadoc)
     * @see net.automatalib.graphs.dot.DefaultDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
     */
    @Override
    public boolean getNodeProperties(S node, Map<String, String> properties) {
        if (!super.getNodeProperties(node, properties)) {
            return false;
        }
        if (automaton.isAccepting(node)) {
            String oldShape = properties.getOrDefault(NodeAttrs.SHAPE, "oval");
            properties.put(NodeAttrs.SHAPE, "double" + oldShape);
        }
        return true;
    }

}
