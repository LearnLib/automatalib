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

import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.graph.concept.NodeIDs;

public class NodeIDGrowingMapping<S, V> implements MutableMapping<S, V> {

    private final NodeIDs<S> nodeIDs;
    private final ArrayStorage<V> storage;

    public NodeIDGrowingMapping(NodeIDs<S> nodeIDs, int size) {
        this.nodeIDs = nodeIDs;
        this.storage = new ArrayStorage<>(size);
    }

    @Override
    public V get(S elem) {
        int id = nodeIDs.getNodeId(elem);
        return storage.get(id);
    }

    @Override
    public V put(S key, V value) {
        int id = nodeIDs.getNodeId(key);
        storage.ensureCapacity(id + 1);
        return storage.set(id, value);
    }

}
