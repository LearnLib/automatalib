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
package net.automatalib.util.graph.copy;

import java.util.Collection;

import net.automatalib.common.util.Holder;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.graph.IndefiniteGraph;
import net.automatalib.graph.MutableGraph;
import net.automatalib.util.graph.traversal.GraphTraversal;
import net.automatalib.util.graph.traversal.GraphTraversalAction;
import net.automatalib.util.graph.traversal.GraphTraversalVisitor;
import net.automatalib.util.traversal.TraversalOrder;

final class TraversalGraphCopy<N1, E1, N2, E2, NP2, EP2>
        extends AbstractGraphCopy<N1, E1, N2, E2, NP2, EP2, IndefiniteGraph<N1, E1>>
        implements GraphTraversalVisitor<N1, E1, N2> {

    private final TraversalOrder traversalOrder;
    private final Collection<? extends N1> initNodes;
    private final int limit;

    TraversalGraphCopy(TraversalOrder traversalOrder,
                       int limit,
                       IndefiniteGraph<N1, E1> inGraph,
                       Collection<? extends N1> initNodes,
                       MutableGraph<N2, E2, NP2, EP2> outGraph,
                       Mapping<? super N1, ? extends NP2> npMapping,
                       Mapping<? super E1, ? extends EP2> epMapping) {
        super(inGraph, outGraph, npMapping, epMapping);
        this.limit = limit;
        this.traversalOrder = traversalOrder;
        this.initNodes = initNodes;
    }

    @Override
    public void doCopy() {
        GraphTraversal.traverse(traversalOrder, inGraph, limit, initNodes, this);
    }

    @Override
    public GraphTraversalAction processInitial(N1 initialNode, Holder<N2> holder) {
        holder.value = copyNode(initialNode);
        return GraphTraversalAction.EXPLORE;
    }

    @Override
    public GraphTraversalAction processEdge(N1 srcNode, N2 srcData, E1 edge, N1 tgtNode, Holder<N2> tgtHolder) {
        N2 freshTgt = copyEdgeChecked(srcData, edge, tgtNode);
        if (freshTgt != null) {
            tgtHolder.value = freshTgt;
            return GraphTraversalAction.EXPLORE;
        }
        return GraphTraversalAction.IGNORE;
    }

}
