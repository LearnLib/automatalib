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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Function;

/**
 * Various methods for operating on {@link Collection}s.
 */
public final class CollectionsUtil {

    private CollectionsUtil() {
        // prevent instantiation.
    }

    public static List<Integer> intRange(int start, int end) {
        return new IntRange(start, end);
    }

    public static List<Integer> intRange(int start, int end, int step) {
        return new IntRange(start, end, step);
    }

    public static List<Character> charRange(char start, char end) {
        return new CharRange(start, end);
    }

    public static List<String> charStringRange(char start, char end) {
        return new CharStringRange(start, end);
    }

    public static <T> List<? extends T> randomAccessList(Collection<? extends T> coll) {
        if (coll instanceof List && coll instanceof RandomAccess) {
            return (List<? extends T>) coll;
        }
        return new ArrayList<>(coll);
    }

    public static <T> Iterable<List<T>> allTuples(Iterable<? extends T> domain, int length) {
        return allTuples(domain, length, length);
    }

    /**
     * Returns an iterator that iterates over all tuples of the given source domain whose length (dimension) is within
     * the specified range. Each intermediate combination of elements is computed lazily.
     * <p>
     * <b>Note:</b> Subsequent calls to the returned iterator's {@link Iterator#next() next()} method return a
     * reference to the same list, and only update the contents of the list. If you plan to reuse intermediate results,
     * you'll need to explicitly copy them.
     *
     * @param domain
     *         the iterables for the source domains
     * @param minLength
     *         the minimal length of the tuple
     * @param maxLength
     *         the maximum length of the tuple
     * @param <T>
     *         type of elements
     *
     * @return an iterator that iterates over all tuples of the given source domain whose length (dimension) is within
     * the specified range
     */
    public static <T> Iterable<List<T>> allTuples(Iterable<? extends T> domain, int minLength, int maxLength) {
        // Check if domain is empty
        // If it is, then the empty tuple (if not excluded by minLength > 0) is still part of the result
        // Otherwise, the result is empty
        if (!domain.iterator().hasNext()) {
            if (minLength == 0) {
                return Collections.singletonList(Collections.emptyList());
            }
            return Collections.emptyList();
        }

        return () -> new AllTuplesIterator<>(domain, minLength, maxLength);
    }

    /**
     * Returns an iterator that iterates over the cartesian product of its given source domains. Each intermediate
     * combination of elements is computed lazily.
     * <p>
     * <b>Note:</b> Subsequent calls to the returned iterator's {@link Iterator#next() next()} method return a
     * reference to the same list, and only update the contents of the list. If you plan to reuse intermediate results,
     * you'll need to explicitly copy them.
     *
     * @param iterables
     *         the iterables for the source domains
     * @param <T>
     *         type of elements
     *
     * @return an iterator that iterates over the cartesian product of its given source domains
     */
    @SafeVarargs
    public static <T> Iterable<List<T>> cartesianProduct(Iterable<T>... iterables) {
        if (iterables.length == 0) {
            return Collections.singletonList(Collections.emptyList());
        }

        return () -> new AllCombinationsIterator<>(iterables);
    }

    /**
     * Returns a (mutable) view on the given collection that transforms its elements as specified by the given mapping.
     *
     * @param collection
     *         the source collection
     * @param mapping
     *         the transformation function
     * @param <D>
     *         mapping domain type
     * @param <R>
     *         mapping range type
     *
     * @return the mapped view on the given collection
     */
    public static <D, R> Collection<R> map(Collection<D> collection, Function<? super D, ? extends R> mapping) {
        return new MappingCollection<>(collection, mapping);
    }

    /**
     * Constructs a list from the given elements.
     *
     * @param first
     *         the first (mandatory) element
     * @param rest
     *         the remaining (optional) elements
     * @param <T>
     *         element type
     *
     * @return a list containing the specified elements
     */
    @SafeVarargs
    public static <T> List<T> list(T first, T... rest) {
        final List<T> result = new ArrayList<>(rest.length + 1);
        result.add(first);
        Collections.addAll(result, rest);
        return result;
    }
}
