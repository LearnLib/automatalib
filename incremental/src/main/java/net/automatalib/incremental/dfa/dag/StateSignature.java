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
package net.automatalib.incremental.dfa.dag;

import java.util.Objects;

import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.incremental.dfa.Acceptance;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Signature of a state. A signature consists of the list of all successor states for all alphabet symbols, and the
 * acceptance status.
 */
final class StateSignature {

    final ArrayStorage<State> successors;
    Acceptance acceptance;
    private int hashCode;

    StateSignature(int numSuccs, Acceptance acceptance) {
        this.successors = new ArrayStorage<>(numSuccs);
        this.acceptance = acceptance;
        updateHashCode();
    }

    StateSignature(StateSignature other) {
        this.successors = new ArrayStorage<>(other.successors);
        this.acceptance = other.acceptance;
        updateHashCode();
    }

    void updateHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + acceptance.hashCode();
        result = prime * result + successors.hashCode();
        hashCode = result;
    }

    StateSignature duplicate() {
        return new StateSignature(this);
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

        final StateSignature other = (StateSignature) obj;

        return hashCode == other.hashCode && acceptance == other.acceptance &&
               Objects.equals(successors, other.successors);
    }

}
