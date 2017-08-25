/* Copyright (C) 2013-2017 TU Dortmund
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
package net.automatalib.ts.powerset;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

public class FastPowersetState<S> extends AbstractSet<S> {

    private final BitSet bs = new BitSet();
    private final List<S> contents = new ArrayList<>();

    public void add(S state, int id) {
        if (bs.get(id)) {
            return;
        }
        bs.set(id);
        contents.add(state);
    }

    @Override
    public Iterator<S> iterator() {
        return contents.iterator();
    }

    @Override
    public int size() {
        return contents.size();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        FastPowersetState<?> other = (FastPowersetState<?>) obj;
        return bs.equals(other.bs);
    }

    @Override
    public int hashCode() {
        return bs.hashCode();
    }

}
