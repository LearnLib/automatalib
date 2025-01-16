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

import java.util.Map;
import java.util.function.Function;

/**
 * An interface for mapping objects of a certain domain type to objects of a certain range type.
 * <p>
 * A mapping is very much like a {@link Map}, but the perspective is a different one: Whereas a map is a (particularly
 * finite) key/value collection, a mapping is more like a function: it does not support retrieval of all keys or values,
 * because it does not require them to be stored at all. Instead, they can be calculated on the fly upon an invocation
 * of {@link #get(Object)}.
 *
 * @param <D>
 *         domain type.
 * @param <R>
 *         range type.
 */
@FunctionalInterface
public interface Mapping<D, R> extends Function<D, R> {

    @Override
    default R apply(D elem) {
        return get(elem);
    }

    /**
     * Get the range object {@code elem} maps to. Implementations may decide how to deal with unmapped values (default
     * values, exceptions, etc.)
     *
     * @param elem
     *         object from the domain.
     *
     * @return the object from the range corresponding to {@code elem}.
     */
    R get(D elem);
}
