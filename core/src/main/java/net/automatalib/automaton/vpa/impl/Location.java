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
package net.automatalib.automaton.vpa.impl;

import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.common.util.array.ArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Location type used for the default 1-SEVPA.
 */
public class Location {

    private final Location[] intSuccessors;
    private final ArrayStorage<ArrayStorage<Location>> returnSuccessors;
    private final int index;
    private boolean accepting;

    public Location(VPAlphabet<?> alphabet, int index, boolean accepting) {
        this.index = index;
        this.accepting = accepting;
        this.intSuccessors = new Location[alphabet.getNumInternals()];
        this.returnSuccessors = new ArrayStorage<>(alphabet.getNumReturns());
    }

    public int getIndex() {
        return index;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    public @Nullable Location getReturnSuccessor(int retSymId, int stackSym) {
        final ArrayStorage<Location> succList = returnSuccessors.get(retSymId);
        if (succList != null && stackSym < succList.size()) {
            return succList.get(stackSym);
        }
        return null;
    }

    public void setReturnSuccessor(int retSymId, int stackSym, Location succ) {
        ArrayStorage<Location> succList = returnSuccessors.get(retSymId);
        if (succList == null) {
            succList = new ArrayStorage<>(stackSym + 1);
            returnSuccessors.set(retSymId, succList);
        }
        succList.ensureCapacity(stackSym + 1);
        succList.set(stackSym, succ);
    }

    public Location getInternalSuccessor(int intSymId) {
        return intSuccessors[intSymId];
    }

    public void setInternalSuccessor(int intSymId, Location succ) {
        intSuccessors[intSymId] = succ;
    }

}
