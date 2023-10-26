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
package net.automatalib.incremental.moore;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.automatalib.incremental.moore.IncrementalMooreBuilder.GraphView;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;

public abstract class AbstractGraphView<I, O, N, E> implements GraphView<I, O, N, E> {

    @Override
    public VisualizationHelper<N, E> getVisualizationHelper() {
        return new DefaultVisualizationHelper<N, E>() {

            @Override
            public Collection<N> initialNodes() {
                final N initialNode = getInitialNode();
                return initialNode == null ? Collections.emptyList() : Collections.singleton(initialNode);
            }

            @Override
            public boolean getNodeProperties(N node, Map<String, String> properties) {
                super.getNodeProperties(node, properties);

                O output = getOutputSymbol(node);
                properties.put(NodeAttrs.LABEL, String.valueOf(output));

                return true;
            }

            @Override
            public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
                super.getEdgeProperties(src, edge, tgt, properties);

                I input = getInputSymbol(edge);
                properties.put(EdgeAttrs.LABEL, String.valueOf(input));

                return true;
            }
        };
    }
}
