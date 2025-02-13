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
package net.automatalib.graph;

import java.util.Set;

import net.automatalib.graph.concept.FinalNode;
import net.automatalib.graph.concept.InitialNode;
import net.automatalib.graph.visualization.PMPGVisualizationHelper;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import net.automatalib.visualization.VisualizationHelper;

/**
 * Represents a <i>Procedural Modal Transition System</i> as defined in the paper <a
 * href="http://doi.org/10.1007/978-3-030-00244-2_15">M3C: Modal Meta Model Checking</a>. Note that we use the term
 * <i>process graph</i> from the <a href="https://doi.org/10.1007/BFb0084787">original paper</a> to
 * prevent confusion with AutomataLib's concept of {@link TransitionSystem}s.
 *
 * @param <N>
 *         node type
 * @param <L>
 *         edge label type
 * @param <E>
 *         edge type
 * @param <AP>
 *         atomic proposition type
 * @param <TP>
 *         edge property type
 */
public interface ProceduralModalProcessGraph<N, L, E, AP, TP extends ProceduralModalEdgeProperty>
        extends UniversalGraph<N, E, Set<AP>, TP>,
                FiniteKripkeStructure<N, E, AP>,
                FiniteLabeledGraph<N, E, L>,
                InitialNode<N>,
                FinalNode<N> {

    @Override
    default Set<AP> getAtomicPropositions(N node) {
        return getNodeProperty(node);
    }

    @Override
    default VisualizationHelper<N, E> getVisualizationHelper() {
        return new PMPGVisualizationHelper<>(this);
    }
}
