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
package net.automatalib.automata.base;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.commons.util.mappings.MutableMapping;

public class StateIDGrowingMapping<S, V> implements MutableMapping<S, V> {

    private final Automaton<S, ?, ?> automaton;
    private final StateIDs<S> stateIds;
    private final ResizingObjectArray storage;

    public StateIDGrowingMapping(Automaton<S, ?, ?> automaton, StateIDs<S> stateIds) {
        this.automaton = automaton;
        this.stateIds = stateIds;
        this.storage = new ResizingObjectArray(automaton.size());
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(S elem) {
        int id = stateIds.getStateId(elem);
        if (id >= 0 && id < storage.array.length) {
            return (V) storage.array[id];
        }
        return null;
    }

    @Override
    public V put(S key, V value) {
        int id = stateIds.getStateId(key);
        if (id >= storage.array.length) {
            storage.ensureCapacity(automaton.size());
        }
        @SuppressWarnings("unchecked")
        V old = (V) storage.array[id];
        storage.array[id] = value;
        return old;
    }

}
