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

import java.util.HashMap;
import java.util.Map;

/**
 * Class that wraps a {@link Mapping} around a {@link Map}.
 *
 * @param <D>
 *         domain type.
 * @param <R>
 *         range type.
 */
public class MapMapping<D, R> implements MutableMapping<D, R> {

    private final Map<D, R> map;

    /**
     * Constructor.
     */
    public MapMapping() {
        this(new HashMap<>());
    }

    /**
     * Constructor.
     *
     * @param map
     *         the underlying {@link Map} object.
     */
    public MapMapping(Map<D, R> map) {
        this(map, false);
    }

    /**
     * Constructor.
     *
     * @param map
     *         the underlying {@link Map} object.
     * @param copy
     *         whether the given map should be copied or stored by reference.
     */
    public MapMapping(Map<D, R> map, boolean copy) {
        if (copy) {
            this.map = new HashMap<>(map);
        } else {
            this.map = map;
        }
    }

    @Override
    public R get(D elem) {
        return map.get(elem);
    }

    @Override
    public R put(D key, R value) {
        return map.put(key, value);
    }
}
