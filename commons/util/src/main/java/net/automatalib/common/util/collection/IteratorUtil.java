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

/**
 * Utility methods for {@link Iterator}s.
 */
public final class IteratorUtil {

    private IteratorUtil() {
        // prevent instantiation
    }

    /**
     * Checks whether any element provided by the given iterator satisfies the given predicate. Note that the iterator
     * is consumed in this process.
     *
     * @param iterator
     *         the iterator to provide the elements
     * @param predicate
     *         the predicate to test
     * @param <T>
     *         element type
     *
     * @return {@code true} if at least one element satisfies the predicate, {@code false} otherwise
     */
    public static <T> boolean any(Iterator<T> iterator, Predicate<? super T> predicate) {
        while (iterator.hasNext()) {
            if (predicate.test(iterator.next())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns an iterator that aggregates elements of a given source iterator in batches of a given size. While
     * elements are collected eagerly within a batch, the overall batches are computed lazily. The source iterator is
     * consumed in this process.
     * <p>
     * Note that subsequent calls to the returned iterator's {@link Iterator#next()} method return a reference to the
     * same batch object, and only update its contents. If you plan to reuse intermediate results, you'll need to
     * explicitly copy them.
     * <p>
     * If the batch size is not a divisor of the number of elements that the iterator provides, the last constructed
     * batch may not be filled completely.
     *
     * @param iterator
     *         the iterator to read the original elements from
     * @param batchSize
     *         the size of batches that should be constructed
     * @param <T>
     *         element type
     *
     * @return an iterator that provides the original elements in batches of the given size
     */
    public static <T> Iterator<List<T>> batch(Iterator<T> iterator, int batchSize) {
        return new BatchingIterator<>(iterator, batchSize);
    }

    /**
     * Returns an iterator that iterates over all elements of the given source iterators.
     *
     * @param iterators
     *         the source iterators
     * @param <T>
     *         element type
     *
     * @return the concatenated iterator
     */
    @SafeVarargs
    public static <T> Iterator<T> concat(Iterator<? extends T>... iterators) {
        return new ConcatIterator<>(iterators);
    }

    /**
     * A filtered view on the given iterator that only returns elements that satisfy the given predicate.
     *
     * @param iterator
     *         the iterator to provide the elements
     * @param predicate
     *         the size hint for constructing the list
     * @param <T>
     *         element type
     *
     * @return an iterator that only returns elements that satisfy the given predicate
     */
    public static <T> Iterator<T> filter(Iterator<T> iterator, Predicate<? super T> predicate) {
        return new FilteringIterator<>(iterator, predicate);
    }

    /**
     * Returns an immutable view on the given iterator so that calls to {@link Iterator#remove()} throw a
     * {@link UnsupportedOperationException}.
     *
     * @param iterator
     *         the iterator to provide the elements
     * @param <T>
     *         element type
     *
     * @return an iterator that does not support the removal of elements
     */
    public static <T> Iterator<T> immutable(Iterator<T> iterator) {
        return new ImmutableIterator<>(iterator);
    }

    /**
     * Collects the elements of the given iterator into a list. Note that the iterator is consumed in this process.
     *
     * @param iterator
     *         the iterator to provide the elements
     * @param <T>
     *         element type
     *
     * @return a list containing the elements of the iterator
     */
    public static <T> List<T> list(Iterator<T> iterator) {
        return collection(new ArrayList<>(), iterator);
    }

    /**
     * Collects the elements of the given iterator into a list that is initialized with the given size hint. Note that
     * the iterator is consumed in this process.
     *
     * @param iterator
     *         the iterator to provide the elements
     * @param expectedSize
     *         the size hint for constructing the list
     * @param <T>
     *         element type
     *
     * @return a list containing the elements of the iterator
     */
    public static <T> List<T> list(Iterator<T> iterator, int expectedSize) {
        return collection(new ArrayList<>(expectedSize), iterator);
    }

    /**
     * Returns a (mutable) view on the given iterator that transforms its elements as specified by the given mapping.
     *
     * @param iterator
     *         the source iterator
     * @param mapping
     *         the transformation function
     * @param <D>
     *         mapping domain type
     * @param <R>
     *         mapping range type
     *
     * @return the mapped view on the given iterator
     */
    public static <D, R> Iterator<R> map(Iterator<D> iterator, Function<? super D, ? extends R> mapping) {
        return new MappingIterator<>(iterator, mapping);
    }

    /**
     * Collects the elements of the given iterator into a set. Note that the iterator is consumed in this process.
     *
     * @param iterator
     *         the iterator to provide the elements
     * @param <T>
     *         element type
     *
     * @return a set containing the elements of the iterator
     */
    public static <T> Set<T> set(Iterator<T> iterator) {
        return collection(new HashSet<>(), iterator);
    }

    /**
     * Collects the elements of the given iterator into a set that is initialized with the given size hint. Note that
     * the iterator is consumed in this process.
     *
     * @param iterator
     *         the iterator to provide the elements
     * @param expectedSize
     *         the size hint for constructing the list
     * @param <T>
     *         element type
     *
     * @return a set containing the elements of the iterator
     */
    public static <T> Set<T> set(Iterator<T> iterator, int expectedSize) {
        return collection(new HashSet<>(HashUtil.capacity(expectedSize)), iterator);
    }

    /**
     * Returns an iterator that only iterates over the given element.
     *
     * @param element
     *         the element to iterate over
     * @param <T>
     *         element type
     *
     * @return the iterator
     */
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

    /**
     * Returns the number of elements returned by the iterator. Note that this consumes the iterator in the process.
     *
     * @param iterator
     *         the iterator whose elements should be counted
     * @param <T>
     *         element type
     *
     * @return the number of elements returned by the iterator
     */
    public static <T> int size(Iterator<T> iterator) {
        int size = 0;
        while (iterator.hasNext()) {
            size++;
            iterator.next();
        }

        return size;
    }

    /**
     * Transforms the given iterator into a stream.
     *
     * @param iterator
     *         the source iterator
     * @param <T>
     *         element type
     *
     * @return the stream-based view on the iterator
     */
    public static <T> Stream<T> stream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }

    private static <C extends Collection<T>, T> C collection(C collection, Iterator<T> iterator) {
        while (iterator.hasNext()) {
            collection.add(iterator.next());
        }
        return collection;
    }
}
