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
package net.automatalib.graphs.base.compact;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractCompactNPGraph<E extends CompactEdge<EP>, @Nullable NP, EP>
        extends AbstractCompactGraph<E, NP, EP> {

    protected final ResizingArrayStorage<NP> npStorage;

    public AbstractCompactNPGraph() {
        this.npStorage = new ResizingArrayStorage<>(Object.class);
    }

    @Override
    public int addIntNode(@Nullable NP properties) {
        int node = super.addIntNode(properties);
        npStorage.ensureCapacity(size);
        npStorage.array[node] = properties;
        return node;
    }

    @Override
    public void setNodeProperty(int node, @Nullable NP property) {
        npStorage.array[node] = property;
    }

    @Override
    public NP getNodeProperties(int node) {
        return npStorage.array[node];
    }

}
