/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.automata.vpa;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.words.VPAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Basic functionality for {@link Location}-based SEVPA implementations.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 * @author frohme
 */
public abstract class AbstractDefaultSEVPA<I> extends AbstractSEVPA<Location, I> {

    protected static final int DEFAULT_SIZE = 10;

    private final List<Location> locations;
    private Location initLoc;

    public AbstractDefaultSEVPA(VPAlphabet<I> alphabet, int capacity) {
        super(alphabet);
        this.locations = new ArrayList<>(capacity);
    }

    public Location addInitialLocation(boolean accepting) {
        final Location loc = addLocation(accepting);
        setInitialLocation(loc);
        return loc;
    }

    public Location addLocation(boolean accepting) {
        final Location loc = new Location(alphabet, locations.size(), accepting);
        locations.add(loc);
        return loc;
    }

    @Override
    public int size() {
        return locations.size();
    }

    public void setInternalSuccessor(Location loc, I intSym, Location succ) {
        loc.setInternalSuccessor(alphabet.getInternalSymbolIndex(intSym), succ);
    }

    public void setReturnSuccessor(Location loc, I retSym, int stackSym, Location succ) {
        loc.setReturnSuccessor(alphabet.getReturnSymbolIndex(retSym), stackSym, succ);
    }

    @Override
    public Location getInternalSuccessor(Location loc, I intSym) {
        return loc.getInternalSuccessor(alphabet.getInternalSymbolIndex(intSym));
    }

    @Override
    public Location getLocation(int id) {
        return locations.get(id);
    }

    @Override
    public int getLocationId(Location loc) {
        return loc.getIndex();
    }

    @Override
    public List<Location> getLocations() {
        return locations;
    }

    @Override
    public @Nullable Location getReturnSuccessor(Location loc, I retSym, int stackSym) {
        return loc.getReturnSuccessor(alphabet.getReturnSymbolIndex(retSym), stackSym);
    }

    @Override
    public boolean isAcceptingLocation(Location loc) {
        return loc.isAccepting();
    }

    @Override
    public Location getInitialLocation() {
        return initLoc;
    }

    public void setInitialLocation(Location loc) {
        this.initLoc = loc;
    }

}
