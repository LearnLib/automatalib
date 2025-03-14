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

import net.automatalib.incremental.dfa.Acceptance;

/**
 * State data structure. Note that states are generally unique throughout the algorithm, hence comparisons are always
 * identity comparisons.
 */
final class State {

    static final State SINK = new State(new StateSignature(0, Acceptance.FALSE));

    private final StateSignature signature;
    private int numIncoming;

    /**
     * Constructor. Initializes the state with a given signature.
     *
     * @param signature
     *         the signature
     */
    State(StateSignature signature) {
        this.signature = signature;
        this.numIncoming = 0;
    }

    /**
     * Increases the number of incoming transitions.
     */
    void increaseIncoming() {
        numIncoming++;
    }

    /**
     * Decreases the number of incoming transitions.
     */
    void decreaseIncoming() {
        numIncoming--;
    }

    /**
     * Checks whether this node is a confluence node (i.e. has more than one incoming transitions).
     *
     * @return {@code true} if this node is a confluence node, {@code false} otherwise.
     */
    boolean isConfluence() {
        return numIncoming > 1;
    }

    /**
     * Retrieves the ternary acceptance status of this node.
     *
     * @return the acceptance status of this node.
     */
    Acceptance getAcceptance() {
        if (signature == null) {
            return Acceptance.FALSE;
        }
        return signature.acceptance;
    }

    /**
     * Retrieves the successor for the given input index.
     *
     * @param idx
     *         the input index
     *
     * @return the successor state for the given index
     */
    State getSuccessor(int idx) {
        return signature.successors.get(idx);
    }

    /**
     * Retrieves the signature of this state.
     *
     * @return the state's signature
     */
    StateSignature getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        if (isSink()) {
            return "sink";
        }
        return "s";
    }

    boolean isSink() {
        return this == SINK;
    }

    void ensureInputCapacity(int capacity) {
        signature.successors.ensureCapacity(capacity);
    }
}
