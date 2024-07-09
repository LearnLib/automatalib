/*
 * Copyright (C) 2014-2015 The LearnLib Contributors
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package net.automatalib.data;

import net.automatalib.data.SymbolicDataValue.Register;

/**
 * maps symbolic data values to symbolic data values.
 *
 * @param <K>
 * @param <V>
 *
 * @author falk
 */
public class VarMapping<K extends SymbolicDataValue<?>, V extends SymbolicDataValue<?>> extends Mapping<K, V> {

    public VarMapping() {}

    public VarMapping(K k1, V v1) {
        put(k1, v1);
    }

    @Override
    public V put(K key, V value) {
        if (!key.getType().equals(value.getType())) {
            throw new IllegalArgumentException("Types of key and value do not match");
        }
        return super.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> SymbolicDataValue<T> get(SymbolicDataValue<T> key) {
        return (SymbolicDataValue<T>) super.get(key);
    }

    public static class RegMapping<K extends SymbolicDataValue<?>> extends VarMapping<K, Register<?>> {

        @Override
        public <T> Register<T> get(SymbolicDataValue<T> key) {
            return (Register<T>) super.get(key);
        }
    }

}
