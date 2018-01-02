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
package net.automatalib.incremental.dfa.dag;

import java.util.Arrays;

import net.automatalib.incremental.dfa.Acceptance;

/**
 * Signature of a state. A signature consists of the list of all successor states for all alphabet symbols, and the
 * acceptance status.
 *
 * @author Malte Isberner
 */
final class StateSignature {

    public final State[] successors;
    public Acceptance acceptance;
    private int hashCode;

    StateSignature(int numSuccs, Acceptance acceptance) {
        this.successors = new State[numSuccs];
        this.acceptance = acceptance;
        updateHashCode();
    }

    StateSignature(StateSignature other) {
        this.successors = other.successors.clone();
        this.acceptance = other.acceptance;
    }

    public void updateHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + acceptance.hashCode();
        result = prime * result + Arrays.hashCode(successors);
        hashCode = result;
    }

    public StateSignature duplicate() {
        return new StateSignature(this);
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
        if (acceptance != other.acceptance) {
            return false;
        }
        for (int i = 0; i < successors.length; i++) {
            if (successors[i] != other.successors[i]) {
                return false;
            }
        }
        return true;
    }

}
