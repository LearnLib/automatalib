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
package net.automatalib.incremental.mealy.tree;

/**
 * An edge in the tree internally used by {@link IncrementalMealyTreeBuilder}.
 *
 * @param <O>
 *         output symbol type
 */
final class Edge<N, O> {

    private final O output;
    private final N target;

    Edge(O output, N target) {
        this.output = output;
        this.target = target;
    }

    O getOutput() {
        return output;
    }

    N getTarget() {
        return target;
    }
}
