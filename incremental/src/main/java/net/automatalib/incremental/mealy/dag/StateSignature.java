/* Copyright (C) 2013-2018 TU Dortmund
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

import java.util.Arrays;
import java.util.Objects;

final class StateSignature {

    public final State[] successors;
    public final Object[] outputs;
    private int hashCode;

    StateSignature(int numSuccs) {
        this.successors = new State[numSuccs];
        this.outputs = new Object[numSuccs];
    }

    StateSignature(StateSignature other) {
        this.successors = other.successors.clone();
        this.outputs = other.outputs.clone();
    }

    public StateSignature duplicate() {
        return new StateSignature(this);
    }

    public void updateHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(outputs);
        result = prime * result + Arrays.hashCode(successors);
        hashCode = result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != StateSignature.class) {
            return false;
        }
        StateSignature other = (StateSignature) obj;
        if (hashCode != other.hashCode) {
            return false;
        }
        for (int i = 0; i < successors.length; i++) {
            if (successors[i] != other.successors[i]) {
                return false;
            }
        }
        for (int i = 0; i < outputs.length; i++) {
            if (!Objects.equals(outputs[i], other.outputs[i])) {
                return false;
            }
        }
        return true;
    }

}
