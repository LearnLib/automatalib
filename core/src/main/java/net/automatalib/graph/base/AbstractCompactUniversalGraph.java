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
package net.automatalib.graph.base;

import net.automatalib.graph.MutableGraph;
import net.automatalib.graph.MutableGraph.IntAbstraction;
import net.automatalib.graph.concept.NodeIDs;

public abstract class AbstractCompactUniversalGraph<E extends CompactEdge<EP>, NP, EP>
        extends AbstractCompactGraph<E, NP, EP>
        implements MutableGraph<Integer, E, NP, EP>, IntAbstraction<E, NP, EP>, NodeIDs<Integer> {

    public AbstractCompactUniversalGraph() {
        // default constructor
    }

    public AbstractCompactUniversalGraph(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public void setEdgeProperty(E edge, EP property) {
        edge.setProperty(property);
    }

    @Override
    public EP getEdgeProperty(E edge) {
        return edge.getProperty();
    }

}
