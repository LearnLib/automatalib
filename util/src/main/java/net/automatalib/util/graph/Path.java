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
package net.automatalib.util.graph;

import java.util.AbstractList;
import java.util.List;

import net.automatalib.graph.IndefiniteGraph;

public class Path<N, E> extends AbstractList<E> {

    private final IndefiniteGraph<N, E> graph;
    private final N start;
    private final List<? extends E> edgeList;

    Path(IndefiniteGraph<N, E> graph, N start, List<? extends E> edgeList) {
        this.graph = graph;
        this.start = start;
        this.edgeList = edgeList;
    }

    @Override
    public E get(int index) {
        return edgeList.get(index);
    }

    @Override
    public int size() {
        return edgeList.size();
    }

    public List<N> getNodes() {
        return new NodeList();
    }

    private final class NodeList extends AbstractList<N> {

        @Override
        public N get(int index) {
            if (index == 0) {
                return start;
            }
            final E edge = edgeList.get(index - 1);
            return graph.getTarget(edge);
        }

        @Override
        public int size() {
            return edgeList.size() + 1;
        }
    }
}
