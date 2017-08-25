/* Copyright (C) 2013-2017 TU Dortmund
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
package net.automatalib.commons.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * Various methods for operating on collections.
 *
 * @author Malte Isberner
 */
public final class CollectionsUtil {

    // Prevent instantiation.
    private CollectionsUtil() {
    }

    /**
     * Adds all elements from an iterable to a collection.
     *
     * @param <E>
     *         element class.
     * @param coll
     *         the collection to which the elements are added.
     * @param it
     *         the iterable providing the elements to add.
     *
     * @return <code>true</code> if the collection was modified, <code>false</code> otherwise.
     */
    public static <E> boolean addAll(Collection<E> coll, Iterable<? extends E> it) {
        boolean modified = false;
        for (E e : it) {
            modified = coll.add(e) || modified;
        }
        return modified;
    }

    /**
     * Retrieves an unmodifiable list only containing null values of the given size.
     *
     * @param size
     *         the size
     *
     * @return a list consisting of the specified number of <tt>null</tt> values
     */
    @SuppressWarnings("unchecked")
    public static <E> List<E> nullList(int size) {
        return (List<E>) new NullList(size);
    }

    public static <E> E removeReplace(List<E> list, int index) {
        int lastIdx = list.size() - 1;
        E last = list.remove(lastIdx);
        if (lastIdx != index) {
            list.set(index, last);
            return last;
        }
        return null;
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

    public static <T> Iterable<List<T>> allTuples(final Iterable<T> domain, final int length) {
        return allTuples(domain, length, length);
    }

    public static <T> Iterable<List<T>> allTuples(final Iterable<T> domain, final int minLength, final int maxLength) {
        // Check if domain is empty
        // If it is, then the empty tuple (if not excluded by minLength > 0) is still part of the result
        // Otherwise, the result is empty
        if (!domain.iterator().hasNext()) {
            if (minLength == 0) {
                return Collections.singletonList(Collections.<T>emptyList());
            }
            return Collections.emptyList();
        }

        return () -> new AllTuplesIterator<>(domain, minLength, maxLength);
    }

    @SafeVarargs
    public static <T> Iterable<List<T>> allCombinations(final Iterable<T>... iterables) {
        if (iterables.length == 0) {
            return Collections.singletonList(Collections.<T>emptyList());
        }

        return () -> {
            try {
                return new AllCombinationsIterator<>(iterables);
            } catch (NoSuchElementException ex) {
                // FIXME: Special case if one of the iterables is empty, then the whole set
                // of combinations is empty. Maybe handle this w/o exception?
                return Collections.<List<T>>emptySet().iterator();
            }
        };
    }

}
