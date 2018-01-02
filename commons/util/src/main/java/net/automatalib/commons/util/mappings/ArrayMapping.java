/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.commons.util.mappings;

import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.commons.util.nid.IDChangeListener;
import net.automatalib.commons.util.nid.NumericID;

public final class ArrayMapping<K extends NumericID, V> implements MutableMapping<K, V>, IDChangeListener<K> {

    private final ResizingObjectArray storage;

    public ArrayMapping() {
        storage = new ResizingObjectArray();
    }

    public ArrayMapping(int initialSize) {
        storage = new ResizingObjectArray(initialSize);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K elem) {
        int id = elem.getId();
        if (id >= storage.array.length) {
            return null;
        }
        return (V) storage.array[id];
    }

    @Override
    public V put(K key, V value) {
        int id = key.getId();
        storage.ensureCapacity(id + 1);
        @SuppressWarnings("unchecked")
        V old = (V) storage.array[id];
        storage.array[id] = value;
        return old;
    }

    @Override
    public void idChanged(K obj, int newId, int oldId) {
        if (newId == -1) {
            if (oldId < storage.array.length) {
                storage.array[oldId] = null;
            }
            return;
        }
        Object oldVal = null;
        if (oldId < storage.array.length) {
            oldVal = storage.array[oldId];
            storage.array[oldId] = oldVal;
        }
        storage.ensureCapacity(newId + 1);
        storage.array[newId] = oldVal;
    }

}
