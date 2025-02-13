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

import java.util.Collections;
import java.util.Set;

import net.automatalib.graph.concept.MutableEdgeLabels;
import net.automatalib.graph.concept.MutableKripkeInterpretation;
import net.automatalib.ts.modal.transition.MutableProceduralModalEdgeProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A mutable version of the {@link ProceduralModalProcessGraph}.
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
 *         edge proposition type
 */
public interface MutableProceduralModalProcessGraph<N, L, E, AP, TP extends MutableProceduralModalEdgeProperty> extends
                                                                                                                ProceduralModalProcessGraph<N, L, E, AP, TP>,
                                                                                                                MutableGraph<N, E, Set<AP>, TP>,
                                                                                                                MutableKripkeInterpretation<N, AP>,
                                                                                                                MutableEdgeLabels<E, L> {

    void setInitialNode(@Nullable N initialNode);

    void setFinalNode(@Nullable N finalNode);

    @Override
    default void setAtomicPropositions(N node, Set<AP> atomicPropositions) {
        setNodeProperty(node, atomicPropositions);
    }

    @Override
    default N addNode() {
        return addNode(Collections.emptySet());
    }

}
