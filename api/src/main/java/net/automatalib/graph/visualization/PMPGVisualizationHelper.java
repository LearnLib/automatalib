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
package net.automatalib.graph.visualization;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import net.automatalib.graph.ProceduralModalProcessGraph;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import net.automatalib.visualization.DefaultVisualizationHelper;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PMPGVisualizationHelper<N, L, E, AP> extends DefaultVisualizationHelper<N, E> {

    private final @Nullable L label;
    private final boolean isMain;
    private final ProceduralModalProcessGraph<N, ?, E, AP, ?> pmpg;

    public PMPGVisualizationHelper(ProceduralModalProcessGraph<N, ?, E, AP, ?> pmpg) {
        this(null, false, pmpg);
    }

    public PMPGVisualizationHelper(@Nullable L label, boolean isMain, ProceduralModalProcessGraph<N, ?, E, AP, ?> pmpg) {
        this.label = label;
        this.isMain = isMain;
        this.pmpg = pmpg;
    }

    @Override
    protected Collection<N> initialNodes() {
        final N initialNode = pmpg.getInitialNode();

        if (initialNode == null) {
            return Collections.emptySet();
        }

        return Collections.singleton(initialNode);
    }

    @Override
    public boolean getNodeProperties(N node, Map<String, String> properties) {
        super.getNodeProperties(node, properties);

        final Set<AP> aps = pmpg.getNodeProperty(node);

        if (aps.isEmpty()) {
            properties.put(PMPGNodeAttrs.LABEL, "");
        } else {
            properties.put(PMPGNodeAttrs.LABEL, aps.toString());
        }

        if (label != null) {
            properties.put(PMPGNodeAttrs.PROCESS, label.toString());
            if (isMain) {
                properties.put(PMPGNodeAttrs.MAIN, "true");
            }
        }

        if (Objects.equals(pmpg.getInitialNode(), node)) {
            properties.put(PMPGNodeAttrs.SHAPE, NodeShapes.OCTAGON);
            properties.put(PMPGNodeAttrs.INITIAL, "true");
        } else if (Objects.equals(pmpg.getFinalNode(), node)) {
            properties.put(PMPGNodeAttrs.SHAPE, NodeShapes.BOX);
            properties.put(PMPGNodeAttrs.FINAL, "true");
        } else {
            properties.put(PMPGNodeAttrs.SHAPE, NodeShapes.CIRCLE);
        }

        return true;
    }

    @Override
    public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
        super.getEdgeProperties(src, edge, tgt, properties);

        final ProceduralModalEdgeProperty prop = pmpg.getEdgeProperty(edge);
        final StringJoiner styleJoiner = new StringJoiner(",");

        if (prop.isMayOnly()) {
            styleJoiner.add(EdgeStyles.DASHED);
        }

        if (prop.isProcess()) {
            styleJoiner.add(EdgeStyles.BOLD);
        }

        properties.put(PMPGEdgeAttrs.LABEL, String.valueOf(pmpg.getEdgeLabel(edge)));
        properties.put(PMPGEdgeAttrs.MODALITY, prop.getModalType().toString());
        properties.put(PMPGEdgeAttrs.PROCEDURALITY, prop.getProceduralType().toString());
        properties.put(PMPGEdgeAttrs.STYLE, styleJoiner.toString());

        return true;
    }

}
