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
package net.automatalib.incremental.moore.dag;

import java.util.Objects;

import net.automatalib.common.util.array.ArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A signature of a DAG state internally used by {@link IncrementalMooreDAGBuilder}.
 *
 * @param <O>
 *         output symbol type
 */
final class StateSignature<O> {

    final ArrayStorage<State<O>> successors;
    final O output;
    private int hashCode;

    StateSignature(int numSuccs, O output) {
        this.successors = new ArrayStorage<>(numSuccs);
        this.output = output;
        updateHashCode();
    }

    StateSignature(StateSignature<O> other) {
        this.successors = new ArrayStorage<>(other.successors);
        this.output = other.output;
        updateHashCode();
    }

    StateSignature<O> duplicate() {
        return new StateSignature<>(this);
    }

    void updateHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + successors.hashCode();
        result = prime * result + Objects.hashCode(output);
        hashCode = result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StateSignature)) {
            return false;
        }

        final StateSignature<?> other = (StateSignature<?>) obj;

        return hashCode == other.hashCode && Objects.equals(output, other.output) &&
               successors.equals(other.successors);
    }

}
