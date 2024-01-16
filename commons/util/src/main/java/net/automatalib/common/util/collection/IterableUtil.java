/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.common.util.collection;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public final class IterableUtil {

    private IterableUtil() {
        // prevent instantiation
    }

    /**
     * Returns an iterable that iterates over all elements of the given source iterables.
     *
     * @param iterables
     *         the source iterables
     * @param <T>
     *         element type
     *
     * @return the concatenated iterable
     */
    @SafeVarargs
    public static <T> Iterable<T> concat(Iterable<? extends T>... iterables) {
        return () -> new ConcatIterable<>(iterables);
    }

    /**
     * Returns a view on the given iterable that transforms its elements as specified by the given mapping.
     *
     * @param iterable
     *         the source iterable
     * @param mapping
     *         the transformation function
     * @param <D>
     *         mapping domain type
     * @param <R>
     *         mapping range type
     *
     * @return the mapped view on the given iterable
     */
    public static <D, R> Iterable<R> map(Iterable<D> iterable, Function<? super D, ? extends R> mapping) {
        return () -> new MappingIterator<>(iterable.iterator(), mapping);
    }

    /**
     * Returns the number of elements of the given iterable.
     *
     * @param iterable
     *         the iterable whose elements should be counted
     * @param <T>
     *         element type
     *
     * @return the number of elements of the iterable
     */
    public static <T> int size(Iterable<T> iterable) {
        if (iterable instanceof Collection) {
            return ((Collection<?>) iterable).size();
        }

        return IteratorUtil.size(iterable.iterator());
    }

    /**
     * Transforms the given iterable into a stream.
     *
     * @param iterable
     *         the source iterable
     * @param <T>
     *         element type
     *
     * @return the stream-based view on the iterable
     */
    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return IteratorUtil.stream(iterable.iterator());
    }
}
