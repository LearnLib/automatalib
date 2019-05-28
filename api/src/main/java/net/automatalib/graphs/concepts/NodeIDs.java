/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.graphs.concepts;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An interface for translating between graph nodes and their primitive representations as integers.
 *
 * @param <N>
 *         node type of the graph
 */
@ParametersAreNonnullByDefault
public interface NodeIDs<N> {

    /**
     * Returns for a given node of the graph an integer uniquely identifying the node.
     *
     * @param node
     *         the node whose id should be retrieved
     *
     * @return the (positive) id of the given graph node. May return a negative value, if {@code node} does not belong
     * to the graph.
     */
    int getNodeId(N node);

    /**
     * Return for a given id the node of the graph identified by it.
     *
     * @param id
     *         the id of the node to be returned
     *
     * @return the graph node identified by the given {@code id}. May return {@code null} if the given {@code id} does
     * not identify a node of the graph.
     */
    N getNode(int id);
}
