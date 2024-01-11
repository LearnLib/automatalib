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
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class IterableUtil {

    private IterableUtil() {
        // prevent instantiation
    }

    @SafeVarargs
    public static <T> Iterable<T> concat(Iterable<? extends T>... iterables) {
        return () -> new ConcatIterable<>(iterables);
    }

    public static <D, R> Iterable<R> map(Iterable<D> src, Function<? super D, ? extends R> mapping) {
        return () -> new MappingIterator<>(src.iterator(), mapping);
    }

    public static <T> int size(Iterable<T> iter) {
        if (iter instanceof Collection) {
            return ((Collection<?>) iter).size();
        }

        return IteratorUtil.size(iter.iterator());
    }

    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return IteratorUtil.stream(iterable.iterator());
    }

    public static <T> boolean any(Iterable<T> iterable, Predicate<? super T> predicate) {
        return IteratorUtil.any(iterable.iterator(), predicate);
    }

    public static <T> boolean all(Iterable<T> iterable, Predicate<? super T> predicate) {
        return IteratorUtil.all(iterable.iterator(), predicate);
    }
}
