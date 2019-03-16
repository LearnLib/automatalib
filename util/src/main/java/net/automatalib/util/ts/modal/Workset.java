/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.util.ts.modal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.automatalib.commons.util.Pair;

/**
 * @author msc
 */
public final class Workset {

    private Workset() {
        // prevent instantiation
    }

    public static <T, E> E process(WorksetAlgorithm<T, E> algo) {

        final Deque<T> stack = new ArrayDeque<>(algo.expectedElementCount());
        final Set<T> tracking = Sets.newHashSetWithExpectedSize(algo.expectedElementCount());

        algo.initialize(stack);
        tracking.addAll(stack);

        while (!stack.isEmpty()) {

            T current = stack.pop();
            tracking.remove(current);

            Collection<T> discovered = algo.update(current);

            for (T element : discovered) {
                if (!tracking.contains(element)) {
                    tracking.add(element);
                    stack.addLast(element);
                }
            }

        }

        return algo.result();
    }

    public static <T, E, D> Pair<Map<T, E>, D> map(WorksetMappingAlgorithm<T, E, D> algo) {

        final Deque<T> stack = new ArrayDeque<>(algo.expectedElementCount());
        final Set<T> tracking = Sets.newHashSetWithExpectedSize(algo.expectedElementCount());
        final Map<T, E> mapping = Maps.newHashMapWithExpectedSize(algo.expectedElementCount());

        algo.initialize(stack, mapping);
        tracking.addAll(stack);

        while (!stack.isEmpty()) {

            T currentT = stack.pop();
            E currentE = mapping.get(currentT);
            tracking.remove(currentT);

            final Collection<T> discovered = algo.update(mapping, currentT, currentE);

            for (T element : discovered) {
                if (!tracking.contains(element)) {
                    tracking.add(element);
                    stack.addLast(element);
                }
            }
        }

        return Pair.of(mapping, algo.result());
    }

}
