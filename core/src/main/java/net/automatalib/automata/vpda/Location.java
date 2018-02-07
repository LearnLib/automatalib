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
package net.automatalib.automata.vpda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.automatalib.commons.util.array.RichArray;
import net.automatalib.words.VPDAlphabet;

/**
 * Location type used for the default 1-SEVPA.
 *
 * @author Malte Isberner
 */
public class Location {

    private final RichArray<Location> intSuccessors;
    private final RichArray<List<Location>> returnSuccessors;
    final int index;
    private boolean accepting;

    public Location(final VPDAlphabet<?> alphabet, final int index, final boolean accepting) {
        this.index = index;
        this.accepting = accepting;
        this.intSuccessors = new RichArray<>(alphabet.getNumInternals());
        this.returnSuccessors = new RichArray<>(alphabet.getNumReturns(), (Supplier<List<Location>>) ArrayList::new);
    }

    public int getIndex() {
        return index;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(final boolean accepting) {
        this.accepting = accepting;
    }

    public Location getReturnSuccessor(final int retSymId, final int stackSym) {
        final List<Location> succList = returnSuccessors.get(retSymId);
        if (succList != null && stackSym < succList.size()) {
            return succList.get(stackSym);
        }
        return null;
    }

    public void setReturnSuccessor(final int retSymId, final int stackSym, final Location succ) {
        List<Location> succList = returnSuccessors.get(retSymId);
        if (succList == null) {
            succList = new ArrayList<>(stackSym + 1);
            returnSuccessors.set(retSymId, succList);
        }
        final int numSuccs = succList.size();
        if (numSuccs <= stackSym) {
            succList.addAll(Collections.nCopies(stackSym + 1 - numSuccs, null));
        }
        succList.set(stackSym, succ);
    }

    public Location getInternalSuccessor(final int intSymId) {
        return intSuccessors.get(intSymId);
    }

    public void setInternalSuccessor(final int intSymId, final Location succ) {
        intSuccessors.set(intSymId, succ);
    }

}
