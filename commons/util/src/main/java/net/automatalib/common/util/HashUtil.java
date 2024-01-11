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
package net.automatalib.common.util;

public final class HashUtil {

    private static final double DEFAULT_LOADFACTOR = 0.75f;

    private HashUtil() {
        // prevent instantiation
    }

    public static int capacity(int expectedSize) {
        return capacity(expectedSize, DEFAULT_LOADFACTOR);
    }

    /*
     * based on https://github.com/openjdk/jdk/blob/967a28c3d85fdde6d5eb48aa0edd8f7597772469/src/java.base/share/classes/java/util/HashMap.java#L2563
     */
    public static int capacity(int expectedSize, double loadFactor) {
        return (int) Math.ceil(expectedSize / loadFactor);
    }
}
