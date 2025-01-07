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
public final class CollectionUtil {

    private CollectionUtil() {
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

    public static <T> List<T> randomAccessList(Collection<T> coll) {
        if (coll instanceof List && coll instanceof RandomAccess) {
            return (List<T>) coll;
        }
        return new ArrayList<>(coll);
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

    /**
     * Adds all elements of the given iterator to the given collection.
     *
     * @param collection
     *         the collection to add elements to
     * @param iterator
     *         the iterator to provide the elements to add
     * @param <T>
     *         element type
     *
     * @return {@code true} of the collection has {@link Collection#add(Object) changed}, {@code false} otherwise.
     */
    public static <T> boolean add(Collection<T> collection, Iterator<T> iterator) {
        boolean changed = false;
        while (iterator.hasNext()) {
            changed |= collection.add(iterator.next());
        }
        return changed;
    }
}
