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
package net.automatalib.modelchecker.m3c.visualization;

import java.util.Map;
import java.util.Objects;

import net.automatalib.graph.base.CompactEdge;
import net.automatalib.modelchecker.m3c.solver.WitnessTree;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;

/**
 * A base {@link VisualizationHelper} for {@link WitnessTree}s.
 */
abstract class AbstractVisualizationHelper extends DefaultVisualizationHelper<Integer, CompactEdge<String>> {

    protected final WitnessTree<?, ?> resultTree;

    AbstractVisualizationHelper(WitnessTree<?, ?> resultTree) {
        this.resultTree = resultTree;
    }

    @Override
    public boolean getNodeProperties(Integer node, Map<String, String> properties) {
        super.getNodeProperties(node, properties);

        properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
        properties.put(NodeAttrs.LABEL, Objects.toString(resultTree.getNodeProperty(node)));

        return true;
    }

    @Override
    public boolean getEdgeProperties(Integer src,
                                     CompactEdge<String> edge,
                                     Integer tgt,
                                     Map<String, String> properties) {
        super.getEdgeProperties(src, edge, tgt, properties);

        properties.put(EdgeAttrs.ARROWHEAD, "none");
        properties.put(EdgeAttrs.LABEL, Objects.toString(edge.getProperty()));

        return true;
    }
}
