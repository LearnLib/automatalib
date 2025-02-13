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

import java.util.Objects;

import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.common.util.array.ArrayStorage;

/**
 * Default implementation for n-SEVPAs.
 *
 * @param <I>
 *         input symbol type
 */
public class DefaultNSEVPA<I> extends AbstractDefaultSEVPA<I> {

    private final Location[] moduleEntries;
    private final ArrayStorage<I> moduleMapping;

    public DefaultNSEVPA(VPAlphabet<I> alphabet) {
        this(alphabet, DEFAULT_SIZE);
    }

    public DefaultNSEVPA(VPAlphabet<I> alphabet, int capacityHint) {
        super(alphabet, capacityHint);
        this.moduleEntries = new Location[alphabet.getNumCalls()];
        this.moduleMapping = new ArrayStorage<>();
    }

    @Override
    public Location addLocation(boolean accepting) {
        final Location loc = super.addLocation(accepting);
        this.moduleMapping.ensureCapacity(loc.getIndex() + 1);
        return loc;
    }

    public Location addLocation(I module, boolean accepting) {
        final Location result = this.addLocation(accepting);
        this.moduleMapping.set(result.getIndex(), module);
        return result;
    }

    public Location addModuleEntryLocation(I callSym, boolean accepting) {
        final Location loc = this.addLocation(callSym, accepting);
        this.moduleEntries[alphabet.getCallSymbolIndex(callSym)] = loc;
        return loc;
    }

    @Override
    public void setInternalSuccessor(Location loc, I intSym, Location succ) {
        final I srcModule = this.moduleMapping.get(loc.getIndex());
        final I tgtModule = this.moduleMapping.get(succ.getIndex());
        if (!Objects.equals(srcModule, tgtModule)) {
            throw new IllegalArgumentException("Cannot set internal success across different modules");
        }
        super.setInternalSuccessor(loc, intSym, succ);
    }

    @Override
    public void setReturnSuccessor(Location loc, I retSym, int stackSym, Location succ) {
        final Location callLoc = super.getStackLoc(stackSym);
        final I srcModule = this.moduleMapping.get(callLoc.getIndex());
        final I tgtModule = this.moduleMapping.get(succ.getIndex());
        if (!Objects.equals(srcModule, tgtModule)) {
            throw new IllegalArgumentException("Must return to the module that was called");
        }
        super.setReturnSuccessor(loc, retSym, stackSym, succ);
    }

    @Override
    public Location getModuleEntry(I callSym) {
        return this.moduleEntries[alphabet.getCallSymbolIndex(callSym)];
    }
}
