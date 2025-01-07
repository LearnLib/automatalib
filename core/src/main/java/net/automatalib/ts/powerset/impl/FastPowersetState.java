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
package net.automatalib.ts.powerset.impl;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import net.automatalib.common.util.collection.IteratorUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

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
        return IteratorUtil.immutable(contents.iterator());
    }

    @Override
    public int size() {
        return contents.size();
    }

    @Override
    public final boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FastPowersetState)) {
            return false;
        }

        final FastPowersetState<?> that = (FastPowersetState<?>) o;
        return bs.equals(that.bs);
    }

    @Override
    public final int hashCode() {
        return bs.hashCode();
    }
}
