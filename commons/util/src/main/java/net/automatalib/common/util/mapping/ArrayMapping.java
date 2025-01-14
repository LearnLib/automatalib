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
package net.automatalib.common.util.mapping;

import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.common.util.nid.IDChangeListener;
import net.automatalib.common.util.nid.NumericID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ArrayMapping<K extends NumericID, @Nullable V> implements MutableMapping<K, V>, IDChangeListener<K> {

    private final ArrayStorage<V> storage;

    public ArrayMapping() {
        storage = new ArrayStorage<>();
    }

    public ArrayMapping(int initialSize) {
        storage = new ArrayStorage<>(initialSize);
    }

    @Override
    public V get(K elem) {
        int id = elem.getId();
        if (id < 0 || id >= storage.size()) {
            return null;
        }
        return storage.get(id);
    }

    @Override
    public V put(K key, V value) {
        int id = key.getId();
        storage.ensureCapacity(id + 1);
        V old = storage.get(id);
        storage.set(id, value);
        return old;
    }

    @Override
    public void idChanged(K obj, int newId, int oldId) {
        if (newId == -1) {
            if (oldId < storage.size()) {
                storage.set(oldId, null);
            }
            return;
        }
        V oldVal = null;
        if (oldId < storage.size()) {
            oldVal = storage.get(oldId);
            storage.set(oldId, oldVal);
        }
        storage.ensureCapacity(newId + 1);
        storage.set(newId, oldVal);
    }

}
