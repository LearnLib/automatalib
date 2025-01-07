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
package net.automatalib.common.util.fixpoint;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.Pair;

public final class Worksets {

    private Worksets() {
        // prevent instantiation
    }

    public static <T, R> R process(WorksetAlgorithm<T, R> algorithm) {

        final int expectedElementCount = algorithm.expectedElementCount();
        final Deque<T> queue = new ArrayDeque<>(expectedElementCount);
        final Set<T> tracking = new HashSet<>(HashUtil.capacity(expectedElementCount));

        final Collection<T> initialElements = algorithm.initialize();
        queue.addAll(initialElements);
        tracking.addAll(initialElements);

        while (!queue.isEmpty()) {

            T current = queue.remove();
            tracking.remove(current);

            final Collection<T> discovered = algorithm.update(current);

            for (T element : discovered) {
                if (tracking.add(element)) {
                    queue.add(element);
                }
            }

        }

        return algorithm.result();
    }

    public static <T, E, R> Pair<Map<T, E>, R> map(WorksetMappingAlgorithm<T, E, R> algorithm) {

        final Deque<T> queue = new ArrayDeque<>(algorithm.expectedElementCount());
        final Set<T> tracking = new HashSet<>(HashUtil.capacity(algorithm.expectedElementCount()));
        final Map<T, E> mapping = new HashMap<>(HashUtil.capacity(algorithm.expectedElementCount()));

        final Collection<T> initialElements = algorithm.initialize(mapping);
        queue.addAll(initialElements);
        tracking.addAll(initialElements);

        while (!queue.isEmpty()) {

            T currentT = queue.remove();
            tracking.remove(currentT);

            final Collection<T> discovered = algorithm.update(mapping, currentT);

            for (T element : discovered) {
                if (tracking.add(element)) {
                    queue.add(element);
                }
            }
        }

        return Pair.of(mapping, algorithm.result());
    }

}
