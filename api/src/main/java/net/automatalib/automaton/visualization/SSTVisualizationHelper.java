/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.automaton.visualization;

import java.util.Map;

import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.transducer.SubsequentialTransducer;
import net.automatalib.word.Word;

public class SSTVisualizationHelper<S, I, T, O>
        extends AutomatonVisualizationHelper<S, I, T, SubsequentialTransducer<S, I, T, O>> {

    public SSTVisualizationHelper(SubsequentialTransducer<S, I, T, O> automaton) {
        super(automaton);
    }

    @Override
    public boolean getEdgeProperties(S src, TransitionEdge<I, T> edge, S tgt, Map<String, String> properties) {
        super.getEdgeProperties(src, edge, tgt, properties);

        final Word<O> output = automaton.getTransitionProperty(edge.getTransition());
        properties.put(EdgeAttrs.LABEL, edge.getInput() + " / " + output);

        return true;
    }

    @Override
    public boolean getNodeProperties(S node, Map<String, String> properties) {
        super.getNodeProperties(node, properties);

        final String oldLabel = properties.get(NodeAttrs.LABEL);
        properties.put(NodeAttrs.LABEL, oldLabel + " / " + automaton.getStateProperty(node));

        return true;
    }
}
