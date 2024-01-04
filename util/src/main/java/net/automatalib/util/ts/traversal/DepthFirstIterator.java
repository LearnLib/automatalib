/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.util.ts.traversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import com.google.common.collect.AbstractIterator;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.traversal.VisitedState;
import org.checkerframework.checker.nullness.qual.Nullable;

final class DepthFirstIterator<S, I, T> extends AbstractIterator<S> {

    private final MutableMapping<S, @Nullable VisitedState> visited;
    private final Deque<SimpleDFRecord<S, I, T>> dfsStack = new ArrayDeque<>();
    private final TransitionSystem<S, I, T> ts;
    private final Collection<? extends I> inputs;

    DepthFirstIterator(TransitionSystem<S, I, T> ts, Collection<? extends I> inputs) {
        this.ts = ts;
        this.inputs = inputs;
        this.visited = ts.createStaticStateMapping();
        for (S init : ts.getInitialStates()) {
            dfsStack.push(new SimpleDFRecord<>(init, inputs));
        }
    }

    @Override
    protected S computeNext() {
        SimpleDFRecord<S, I, T> rec;
        while ((rec = dfsStack.peek()) != null) {
            if (!rec.wasStarted()) {
                visited.put(rec.state, VisitedState.VISITED);
                rec.start(ts);
                return rec.state;
            } else if (rec.hasNextTransition(ts)) {
                T t = rec.transition();
                S succ = ts.getSuccessor(t);
                if (visited.get(succ) != VisitedState.VISITED) {
                    dfsStack.push(new SimpleDFRecord<>(succ, inputs));
                }
            } else {
                dfsStack.pop();
            }
        }
        return endOfData();
    }

}
