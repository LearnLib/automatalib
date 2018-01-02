/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.commons.util.comparison;

import java.util.Comparator;

/**
 * A {@link Comparator} that compares elements according to their natural ordering (i.e., they have to implement the
 * {@link Comparable} interface). If this comparator is used on objects that don't implement this interface, this may
 * result in a {@link ClassCastException}.
 * <p>
 * This class is a singleton, since due to type erasure, different instantiations won't really differ from each other.
 *
 * @param <T>
 *         element class.
 *
 * @author Malte Isberner
 */
final class NaturalOrderingComparator<T extends Comparable<T>> implements Comparator<T> {

    private static final NaturalOrderingComparator<? extends Comparable<?>> INSTANCE =
            new NaturalOrderingComparator<Integer>();

    /**
     * Singleton instance access method.
     *
     * @param <T>
     *         element class.
     *
     * @return the natural ordering comparator.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> NaturalOrderingComparator<T> getInstance() {
        return (NaturalOrderingComparator<T>) INSTANCE;
    }

    @Override
    public int compare(T o1, T o2) {
        return o1.compareTo(o2);
    }
}
