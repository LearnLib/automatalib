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
package net.automatalib.graph.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.common.util.HashUtil;
import net.automatalib.graph.SimpleGraph;
import net.automatalib.graph.concept.NodeIDs;

public class SimpleNodeIDs<N> implements NodeIDs<N> {

    private final Map<N, Integer> nodeIds;
    private final List<N> nodes;

    public SimpleNodeIDs(SimpleGraph<N> graph) {
        this.nodes = new ArrayList<>(graph.getNodes());
        int numNodes = this.nodes.size();
        this.nodeIds = new HashMap<>(HashUtil.capacity(numNodes));

        for (int i = 0; i < numNodes; i++) {
            N node = this.nodes.get(i);
            nodeIds.put(node, i);
        }
    }

    @Override
    public int getNodeId(N node) {
        final Integer id = nodeIds.get(node);

        if (id == null) {
            throw new IllegalArgumentException();
        }

        return id;
    }

    @Override
    public N getNode(int id) {
        if (id < 0 || id >= nodes.size()) {
            throw new IllegalArgumentException();
        }
        return nodes.get(id);
    }
}
