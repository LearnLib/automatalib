/* Copyright (C) 2013-2026 TU Dortmund University
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

import java.util.ArrayList;
import java.util.List;

import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.concept.StateIDs;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Basic functionality for {@link Location}-based SEVPA implementations.
 *
 * @param <I>
 *         input symbol type
 */
public abstract class AbstractDefaultSEVPA<I> extends AbstractSEVPA<Location, I> {

    protected static final int DEFAULT_SIZE = 10;

    private final List<Location> locations;
    private Location initLoc;

    public AbstractDefaultSEVPA(VPAlphabet<I> alphabet, int capacity) {
        super(alphabet);
        this.locations = new ArrayList<>(capacity);
    }

    public Location addInitialState(boolean accepting) {
        final Location loc = addState(accepting);
        setInitialState(loc);
        return loc;
    }

    public Location addState(boolean accepting) {
        final Location loc = new Location(alphabet, locations.size(), accepting);
        locations.add(loc);
        return loc;
    }

    @Override
    public StateIDs<Location> stateIDs() {
        return this;
    }

    @Override
    public int getStateId(Location state) {
        return state.getIndex();
    }

    @Override
    public Location getState(int id) {
        return this.locations.get(id);
    }

    @Override
    public List<Location> getStates() {
        return locations;
    }

    @Override
    public Boolean getStateProperty(Location state) {
        return state.isAccepting();
    }

    @Override
    public Location getInternalSuccessor(Location loc, I intSym) {
        return loc.getInternalSuccessor(alphabet.getInternalSymbolIndex(intSym));
    }

    public void setInternalSuccessor(Location loc, I intSym, Location succ) {
        loc.setInternalSuccessor(alphabet.getInternalSymbolIndex(intSym), succ);
    }

    @Override
    public @Nullable Location getReturnSuccessor(Location loc, I retSym, int stackSym) {
        return loc.getReturnSuccessor(alphabet.getReturnSymbolIndex(retSym), stackSym);
    }

    public void setReturnSuccessor(Location loc, I retSym, int stackSym, Location succ) {
        loc.setReturnSuccessor(alphabet.getReturnSymbolIndex(retSym), stackSym, succ);
    }

    @Override
    public Location getInitialState() {
        return initLoc;
    }

    public void setInitialState(Location loc) {
        this.initLoc = loc;
    }

}
