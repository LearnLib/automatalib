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
package net.automatalib.incremental.moore.dag;

/**
 * A transition in the DAG internally used by {@link IncrementalMooreDAGBuilder}.
 *
 * @param <O>
 *         output symbol type
 */
final class Transition<O> {

    final State<O> state;
    final int transIdx;

    Transition(State<O> state, int transIdx) {
        this.state = state;
        this.transIdx = transIdx;
    }

}
