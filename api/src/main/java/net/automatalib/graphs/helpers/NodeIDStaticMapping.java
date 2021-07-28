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
package net.automatalib.graphs.helpers;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.concepts.NodeIDs;

public class NodeIDStaticMapping<S, V> implements MutableMapping<S, V> {

    private final NodeIDs<S> nodeIDs;
    private final V[] storage;

    @SuppressWarnings("unchecked")
    public NodeIDStaticMapping(NodeIDs<S> nodeIDs, int size) {
        this.nodeIDs = nodeIDs;
        this.storage = (V[]) new Object[size];
    }

    @Override
    public V get(S elem) {
        return storage[nodeIDs.getNodeId(elem)];
    }

    @Override
    public V put(S key, V value) {
        final int nodeId = nodeIDs.getNodeId(key);
        final V old = storage[nodeId];
        storage[nodeId] = value;
        return old;
    }

}
