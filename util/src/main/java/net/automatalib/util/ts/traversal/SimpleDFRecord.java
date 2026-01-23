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
package net.automatalib.util.ts.traversal;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.ts.TransitionSystem;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

class SimpleDFRecord<S, I, T> {

    public final S state;

    private final Iterator<? extends I> inputsIterator;
    private I input;
    private @Nullable Iterator<? extends T> transitionIterator;

    SimpleDFRecord(S state, Collection<? extends I> inputs) {
        this.state = state;
        this.inputsIterator = inputs.iterator();
    }

    public final boolean wasStarted() {
        return transitionIterator != null;
    }

    public boolean start(TransitionSystem<S, ? super I, T> ts) {
        if (transitionIterator != null) {
            return false;
        }

        findNext(ts);
        return true;
    }

    @SideEffectFree // while fields are updated, they never change regarding nullability
    private void findNext(TransitionSystem<S, ? super I, T> ts) {
        if (transitionIterator != null && transitionIterator.hasNext()) {
            return;
        }
        while (inputsIterator.hasNext()) {
            input = inputsIterator.next();
            Collection<T> transitions = ts.getTransitions(state, input);
            if (!transitions.isEmpty()) {
                transitionIterator = transitions.iterator();
                break;
            }
        }
    }

    @EnsuresNonNullIf(expression = "transitionIterator", result = true)
    public boolean hasNextTransition(TransitionSystem<S, ? super I, T> ts) {
        if (transitionIterator == null) {
            return false;
        }
        if (!transitionIterator.hasNext()) {
            findNext(ts);
        }
        return transitionIterator.hasNext();
    }

    @RequiresNonNull("transitionIterator")
    public void advance(TransitionSystem<S, ? super I, T> ts) {
        if (transitionIterator.hasNext()) {
            return;
        }
        findNext(ts);
    }

    public void advanceInput(TransitionSystem<S, ? super I, T> ts) {
        transitionIterator = null;
        findNext(ts);
    }

    @Pure
    public I input() {
        return input;
    }

    @RequiresNonNull("transitionIterator")
    @SideEffectFree // while state changes, never does nullability
    public T transition() {
        return transitionIterator.next();
    }

}
