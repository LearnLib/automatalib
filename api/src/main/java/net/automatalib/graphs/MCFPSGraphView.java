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
package net.automatalib.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.visualization.MCFPSVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Graph representation of a {@link ModalContextFreeProcessSystem} that displays all nodes of its sub-procedures once,
 * i.e. without incorporating execution semantics such as stack contents.
 *
 * @param <S>
 *         common procedural state type
 * @param <L>
 *         label type
 * @param <E>
 *         edge type
 * @param <AP>
 *         atomic proposition type
 *
 * @author frohme
 */
public class MCFPSGraphView<S, L, E, AP> implements Graph<Pair<L, S>, Pair<L, E>> {

    private final Map<L, ModalProcessGraph<S, L, E, AP, ?>> mpgs;

    // cast is fine, because we make sure to only query nodes/edges belonging to the respective procedures
    @SuppressWarnings("unchecked")
    public MCFPSGraphView(Map<L, ? extends ModalProcessGraph<? extends S, L, ? extends E, AP, ?>> mpgs) {
        this.mpgs = (Map<L, ModalProcessGraph<S, L, E, AP, ?>>) mpgs;
    }

    @Override
    public Collection<Pair<L, E>> getOutgoingEdges(Pair<L, S> node) {
        final L process = node.getFirst();
        @SuppressWarnings("assignment.type.incompatible") // we only use identifier for which mpgs exist
        final @NonNull ModalProcessGraph<S, L, E, AP, ?> mpg = mpgs.get(process);
        final Collection<E> outgoingEdges = mpg.getOutgoingEdges(node.getSecond());

        final Collection<Pair<L, E>> result = new ArrayList<>(outgoingEdges.size());

        for (E e : outgoingEdges) {
            result.add(Pair.of(process, e));
        }

        return result;
    }

    @Override
    public Pair<L, S> getTarget(Pair<L, E> edge) {
        final L process = edge.getFirst();
        @SuppressWarnings("assignment.type.incompatible") // we only use identifier for which mpgs exist
        final @NonNull ModalProcessGraph<S, L, E, AP, ?> mpg = mpgs.get(process);

        return Pair.of(process, mpg.getTarget(edge.getSecond()));
    }

    @Override
    public Collection<Pair<L, S>> getNodes() {
        final int numNodes = this.mpgs.values().stream().mapToInt(Graph::size).sum();
        final List<Pair<L, S>> result = new ArrayList<>(numNodes);

        for (Entry<L, ModalProcessGraph<S, L, E, AP, ?>> e : this.mpgs.entrySet()) {
            final L process = e.getKey();
            for (S s : e.getValue()) {
                result.add(Pair.of(process, s));
            }
        }

        return result;
    }

    @Override
    public VisualizationHelper<Pair<L, S>, Pair<L, E>> getVisualizationHelper() {
        return new MCFPSVisualizationHelper<>(this.mpgs);
    }
}
