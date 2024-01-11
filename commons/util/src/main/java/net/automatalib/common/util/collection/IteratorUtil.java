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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.automatalib.common.util.HashUtil;

public final class IteratorUtil {

    private IteratorUtil() {
        // prevent instantiation
    }

    public static <T> Iterator<List<T>> batch(Iterator<T> iterator, int batchSize) {
        return new BatchingIterator<>(iterator, batchSize);
    }

    @SafeVarargs
    public static <T> Iterator<T> concat(Iterator<? extends T>... iterators) {
        return new ConcatIterator<>(iterators);
    }

    public static <D, R> Iterator<R> map(Iterator<D> src, Function<? super D, ? extends R> mapping) {
        return new MappingIterator<>(src, mapping);
    }

    public static <T> int size(Iterator<T> iter) {
        int size = 0;
        while (iter.hasNext()) {
            size++;
            iter.next();
        }

        return size;
    }

    public static <T> Stream<T> stream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }

    public static <T> boolean any(Iterator<T> iterator, Predicate<? super T> predicate) {
        while (iterator.hasNext()) {
            if (predicate.test(iterator.next())) {
                return true;
            }
        }

        return false;
    }

    public static <T> boolean all(Iterator<T> iterator, Predicate<? super T> predicate) {
        while (iterator.hasNext()) {
            if (!predicate.test(iterator.next())) {
                return false;
            }
        }

        return true;
    }

    public static <T> List<T> list(Iterator<T> iterator) {
        return collection(new ArrayList<>(), iterator);
    }

    public static <T> List<T> list(Iterator<T> iterator, int expectedSize) {
        return collection(new ArrayList<>(expectedSize), iterator);
    }

    public static <T> Set<T> set(Iterator<T> iterator) {
        return collection(new HashSet<>(), iterator);
    }

    public static <T> Set<T> set(Iterator<T> iterator, int expectedSize) {
        return collection(new HashSet<>(HashUtil.capacity(expectedSize)), iterator);
    }

    public static <T> Iterator<T> filter(Iterator<T> iterator, Predicate<? super T> predicate) {
        return new FilteringIterator<>(iterator, predicate);
    }

    private static <C extends Collection<T>, T> C collection(C collection, Iterator<T> iterator) {
        while (iterator.hasNext()) {
            collection.add(iterator.next());
        }
        return collection;
    }

    public static <T> Iterator<T> singleton(T element) {
        return new Iterator<T>() {

            private boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public T next() {
                if (hasNext) {
                    hasNext = false;
                    return element;
                }

                throw new NoSuchElementException();
            }
        };
    }
}
