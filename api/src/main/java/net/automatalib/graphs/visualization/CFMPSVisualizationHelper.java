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
package net.automatalib.graphs.visualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import net.automatalib.visualization.DefaultVisualizationHelper;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CFMPSVisualizationHelper<N, L, E, AP> extends DefaultVisualizationHelper<Pair<L, N>, Pair<L, E>> {

    private final Map<L, ProceduralModalProcessGraph<N, L, E, AP, ?>> pmpgs;

    // cast is fine, because we make sure to only query nodes/edges belonging to the respective procedures
    @SuppressWarnings("unchecked")
    public CFMPSVisualizationHelper(Map<L, ? extends ProceduralModalProcessGraph<? extends N, L, ? extends E, AP, ?>> pmpgs) {
        this.pmpgs = (Map<L, ProceduralModalProcessGraph<N, L, E, AP, ?>>) pmpgs;
    }

    @Override
    protected Collection<Pair<L, N>> initialNodes() {
        final List<Pair<L, N>> initialNodes = new ArrayList<>(this.pmpgs.size());

        for (Entry<L, ProceduralModalProcessGraph<N, L, E, AP, ?>> e : this.pmpgs.entrySet()) {
            final N init = e.getValue().getInitialNode();
            if (init != null) {
                initialNodes.add(Pair.of(e.getKey(), init));
            }
        }

        return initialNodes;
    }

    @Override
    public boolean getNodeProperties(Pair<L, N> node, Map<String, String> properties) {

        if (!super.getNodeProperties(node, properties)) {
            return false;
        }

        final L process = node.getFirst();
        @SuppressWarnings("assignment.type.incompatible") // we only use identifier for which procedures exists
        final @NonNull ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg = this.pmpgs.get(process);
        final Set<AP> aps = pmpg.getNodeProperty(node.getSecond());

        if (aps.isEmpty()) {
            properties.put(NodeAttrs.LABEL, "");
        } else {
            properties.put(NodeAttrs.LABEL, aps.toString());
        }

        properties.put(NodeAttrs.SHAPE, NodeShapes.CIRCLE);

        return true;
    }

    @Override
    public boolean getEdgeProperties(Pair<L, N> src, Pair<L, E> edge, Pair<L, N> tgt, Map<String, String> properties) {

        if (!super.getEdgeProperties(src, edge, tgt, properties)) {
            return false;
        }

        final L process = edge.getFirst();
        @SuppressWarnings("assignment.type.incompatible") // we only use identifier for which procedures exists
        final @NonNull ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg = this.pmpgs.get(process);
        final ProceduralModalEdgeProperty prop = pmpg.getEdgeProperty(edge.getSecond());
        final StringJoiner styleJoiner = new StringJoiner(",");

        if (prop.isMayOnly()) {
            styleJoiner.add(EdgeStyles.DASHED);
        }

        if (prop.isProcess()) {
            styleJoiner.add(EdgeStyles.BOLD);
        }

        properties.put(EdgeAttrs.LABEL, String.valueOf(pmpg.getEdgeLabel(edge.getSecond())));
        properties.put(EdgeAttrs.STYLE, styleJoiner.toString());

        return true;
    }

}
