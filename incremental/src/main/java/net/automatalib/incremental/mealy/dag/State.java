/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.incremental.mealy.dag;

import java.io.Serializable;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;

public final class State<O> implements Serializable {

    private final StateSignature<O> signature;
    private int numIncoming;

    public State(StateSignature<O> signature) {
        this.signature = signature;
    }

    public void increaseIncoming() {
        numIncoming++;
    }

    public void decreaseIncoming() {
        numIncoming--;
    }

    public int getNumIncoming() {
        return numIncoming;
    }

    public boolean isConfluence() {
        return (numIncoming > 1);
    }

    public State<O> getSuccessor(int idx) {
        return signature.successors.array[idx];
    }

    public O getOutput(int idx) {
        return signature.outputs.array[idx];
    }

    public StateSignature<O> getSignature() {
        return signature;
    }

    /**
     * See {@link ResizingArrayStorage#ensureCapacity(int)}.
     */
    boolean ensureInputCapacity(int capacity) {
        return signature.successors.ensureCapacity(capacity) | signature.outputs.ensureCapacity(capacity);
    }
}
