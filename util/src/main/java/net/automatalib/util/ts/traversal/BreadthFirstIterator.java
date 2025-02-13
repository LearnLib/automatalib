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
package net.automatalib.util.ts.traversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.ts.simple.SimpleTS;
import net.automatalib.util.traversal.VisitedState;

final class BreadthFirstIterator<S, I> implements Iterator<S> {

    private final Queue<S> bfsQueue = new ArrayDeque<>();
    private final MutableMapping<S, VisitedState> seen;
    private final SimpleTS<S, I> ts;
    private final Collection<? extends I> inputs;

    BreadthFirstIterator(SimpleTS<S, I> ts, Collection<? extends I> inputs) {
        this.ts = ts;
        this.inputs = inputs;
        seen = ts.createStaticStateMapping();
        Collection<S> initial = ts.getInitialStates();
        bfsQueue.addAll(initial);
        for (S state : initial) {
            seen.put(state, VisitedState.VISITED);
        }
    }

    @Override
    public boolean hasNext() {
        return !bfsQueue.isEmpty();
    }

    @Override
    public S next() {
        S state = bfsQueue.poll();

        if (state == null) {
            throw new NoSuchElementException();
        }

        for (I input : inputs) {
            Collection<S> succs = ts.getSuccessors(state, input);
            for (S succ : succs) {
                if (seen.put(succ, VisitedState.VISITED) != VisitedState.VISITED) {
                    bfsQueue.add(succ);
                }
            }
        }

        return state;
    }

}
