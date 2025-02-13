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
package net.automatalib.common.util.comparison;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Various methods for dealing with the comparison of objects.
 */
public final class CmpUtil {

    private CmpUtil() {
        // prevent instantiation
    }

    /**
     * Compares two {@link List}s with respect to canonical ordering.
     * <p>
     * In canonical ordering, a sequence {@code o1} is less than a sequence {@code o2} if {@code o1} is shorter than
     * {@code o2}, or if they have the same length and {@code o1} is lexicographically smaller than {@code o2}.
     *
     * @param o1
     *         the first list
     * @param o2
     *         the second list
     * @param elemComparator
     *         the comparator for comparing the single elements
     * @param <U>
     *         element type
     *
     * @return the result of the comparison
     */
    public static <U> int canonicalCompare(List<? extends U> o1,
                                           List<? extends U> o2,
                                           Comparator<? super U> elemComparator) {
        int siz1 = o1.size(), siz2 = o2.size();

        if (siz1 != siz2) {
            return siz1 - siz2;
        }

        return lexCompare(o1, o2, elemComparator);
    }

    public static int canonicalCompare(int[] a1, int[] a2) {
        int ldiff = a1.length - a2.length;
        if (ldiff != 0) {
            return ldiff;
        }
        return lexCompare(a1, a2);
    }

    /**
     * Compares two {@link List}s of {@link Comparable} elements with respect to canonical ordering.
     * <p>
     * In canonical ordering, a sequence {@code o1} is less than a sequence {@code o2} if {@code o1} is shorter than
     * {@code o2}, or if they have the same length and {@code o1} is lexicographically smaller than {@code o2}.
     *
     * @param o1
     *         the first list
     * @param o2
     *         the second list
     * @param <U>
     *         element type
     *
     * @return the result of the comparison
     */
    public static <U extends Comparable<? super U>> int canonicalCompare(List<? extends U> o1, List<? extends U> o2) {
        int siz1 = o1.size(), siz2 = o2.size();
        if (siz1 != siz2) {
            return siz1 - siz2;
        }

        return lexCompare(o1, o2);
    }

    /**
     * Lexicographically compares two {@link Iterable}s. Comparison of the elements is done using the specified
     * comparator.
     *
     * @param o1
     *         the first iterable.
     * @param o2
     *         the second iterable.
     * @param elemComparator
     *         the comparator.
     * @param <U>
     *         element type
     *
     * @return {@code < 0} iff o1 is lexicographically smaller, {@code 0} if o1 equals o2 and {@code > 0} otherwise.
     */
    public static <U> int lexCompare(Iterable<? extends U> o1,
                                     Iterable<? extends U> o2,
                                     Comparator<? super U> elemComparator) {
        Iterator<? extends U> it1 = o1.iterator(), it2 = o2.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            int cmp = elemComparator.compare(it1.next(), it2.next());
            if (cmp != 0) {
                return cmp;
            }
        }

        if (it1.hasNext()) {
            return 1;
        } else if (it2.hasNext()) {
            return -1;
        }
        return 0;
    }

    public static int lexCompare(int[] a1, int[] a2) {
        int i = 0;
        int len1 = a1.length, len2 = a2.length;

        while (i < len1 && i < len2) {
            int cmp = a1[i] - a2[i];
            if (cmp != 0) {
                return cmp;
            }
            i++;
        }

        if (i < len1) {
            return 1;
        }
        if (i < len2) {
            return -1;
        }
        return 0;
    }

    /**
     * Lexicographically compares two {@link Iterable}s using the inert comparability of their elements.
     *
     * @param o1
     *         the first iterable.
     * @param o2
     *         the second iterable.
     * @param <U>
     *         element type
     *
     * @return {@code < 0} iff o1 is lexicographically smaller, {@code 0} if o1 equals o2 and {@code > 0} otherwise.
     */
    public static <U extends Comparable<? super U>> int lexCompare(Iterable<? extends U> o1, Iterable<? extends U> o2) {
        Iterator<? extends U> it1 = o1.iterator(), it2 = o2.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            int cmp = it1.next().compareTo(it2.next());
            if (cmp != 0) {
                return cmp;
            }
        }

        if (it1.hasNext()) {
            return 1;
        } else if (it2.hasNext()) {
            return -1;
        }
        return 0;
    }

    /**
     * Retrieves a lexicographical comparator for the given type.
     *
     * @param elemComp
     *         the comparator to use for comparing the elements.
     * @param <U>
     *         element type
     * @param <T>
     *         iterable type
     *
     * @return a comparator for comparing iterables of type {@code U} based on lexicographical ordering.
     */
    public static <T extends Iterable<U>, U> Comparator<T> lexComparator(Comparator<? super U> elemComp) {
        return new LexComparator<>(elemComp);
    }

    /**
     * Retrieves a lexicographical comparator for the given type, which has to be an {@link Iterable} of
     * {@link Comparable} types.
     *
     * @param <U>
     *         element type
     * @param <T>
     *         iterable type
     *
     * @return the lexicographical comparator.
     */
    public static <U extends Comparable<U>, T extends Iterable<U>> Comparator<T> lexComparator() {
        return CmpUtil::lexCompare;
    }

    /**
     * Retrieves a canonical comparator for the given list type.
     *
     * @param elemComp
     *         the comparator to use for comparing the elements.
     * @param <U>
     *         element type
     * @param <T>
     *         list type
     *
     * @return a comparator for comparing iterables of type {@code U} based on canonical ordering.
     */
    public static <T extends List<? extends U>, U> Comparator<T> canonicalComparator(Comparator<? super U> elemComp) {
        return new CanonicalComparator<>(elemComp);
    }

    /**
     * Retrieves a canonical comparator for the given type, which has to be a {@link List} of {@link Comparable} types.
     *
     * @param <U>
     *         element type
     * @param <T>
     *         list type
     *
     * @return the canonical comparator
     *
     * @see #canonicalCompare(List, List)
     */
    public static <T extends List<U>, U extends Comparable<U>> Comparator<T> canonicalComparator() {
        return CmpUtil::canonicalCompare;
    }

}
