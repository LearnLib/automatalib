/*
 * Copyright (C) 2014-2015 The LearnLib Contributors
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package net.automatalib.automaton.ra;

import static java.util.Collections.emptySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.automatalib.symbol.ParameterizedSymbol;

/**
 *
 * @author falk
 */
public class RALocation {

    private final int id;

    private boolean accepting;

    private final Map<ParameterizedSymbol, Collection<Transition>> out = new LinkedHashMap<>();

    public RALocation(int id, boolean accepting) {
        this.id = id;
        this.accepting = accepting;
    }

    public RALocation(int id) {
        this(id, true);
    }

    public Collection<Transition> getOut(ParameterizedSymbol ps) {
        return out.getOrDefault(ps, emptySet());
    }

    public Collection<Transition> getOut() {
        ArrayList<Transition> ret = new ArrayList<>();
        for (Collection<Transition> col : out.values()) {
            ret.addAll(col);
        }
        return ret;
    }

    public void addOut(Transition t) {
        Collection<Transition> c = out.get(t.getLabel());
        if (c == null) {
            c = new ArrayList<>();
            out.put(t.getLabel(), c);
        }
        c.add(t);
    }

    public void clear() {
        this.out.clear();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RALocation other = (RALocation) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "l" + id + " (" + (this.accepting ? "+" : "-") +")";
    }

    public String getName() {
        return "l" + id;
    }

    public boolean isAccepting() {
        return this.accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }
}
