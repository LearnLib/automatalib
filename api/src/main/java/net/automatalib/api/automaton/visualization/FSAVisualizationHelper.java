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
package net.automatalib.api.automaton.visualization;

import java.util.Map;

import net.automatalib.api.automaton.fsa.FiniteStateAcceptor;

public class FSAVisualizationHelper<S, I> extends AutomatonVisualizationHelper<S, I, S, FiniteStateAcceptor<S, I>> {

    public FSAVisualizationHelper(FiniteStateAcceptor<S, I> automaton) {
        super(automaton);
    }

    @Override
    public boolean getNodeProperties(S node, Map<String, String> properties) {
        super.getNodeProperties(node, properties);

        if (automaton.isAccepting(node)) {
            String oldShape = properties.getOrDefault(NodeAttrs.SHAPE, NodeShapes.CIRCLE);
            properties.put(NodeAttrs.SHAPE, "double" + oldShape);
        }

        return true;
    }
}
