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
package net.automatalib.common.util;

/**
 * Utility class for hash-based datastructures.
 */
public final class HashUtil {

    private static final double DEFAULT_LOADFACTOR = 0.75d;

    private HashUtil() {
        // prevent instantiation
    }

    /**
     * Computes the capacity of a hash structure for given number of expected elements. This methods calls
     * {@link #capacity(int, double)} using {@code 0.75f} as default load factor.
     *
     * @param expectedSize
     *         the number of expected elements
     *
     * @return the required capacity of the data structure.
     */
    public static int capacity(int expectedSize) {
        return capacity(expectedSize, DEFAULT_LOADFACTOR);
    }

    /**
     * Computes the capacity of a hash structure for given number of expected elements. This methods essentially
     * backports the <a
     * href="https://github.com/openjdk/jdk/blob/jdk-19-ga/src/java.base/share/classes/java/util/HashMap.java#L2563">calculateHashMapCapacity(int)</a>
     * call from newer JDKs.
     *
     * @param expectedSize
     *         the number of expected elements
     * @param loadFactor
     *         the load factor of the hash structure
     *
     * @return the required capacity of the data structure.
     */
    public static int capacity(int expectedSize, double loadFactor) {
        return (int) Math.ceil(expectedSize / loadFactor);
    }
}
