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

/**
 * Mutable version of a {@link Mapping}, which supports setting keys for given values.
 *
 * @param <D>
 *         domain class
 * @param <R>
 *         range class
 */
public interface MutableMapping<D, R> extends Mapping<D, R> {

    /**
     * Update the mapping by overriding the value which the given key should map to.
     *
     * @param key
     *         the key
     * @param value
     *         the new value to be associated with the key
     *
     * @return the old value mapped by {@code key}
     */
    R put(D key, R value);
}
