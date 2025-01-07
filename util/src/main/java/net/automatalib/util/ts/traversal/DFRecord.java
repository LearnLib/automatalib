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

import java.util.Collection;

import org.checkerframework.checker.nullness.qual.Nullable;

public class DFRecord<S, I, T, D> extends SimpleDFRecord<S, I, T> {

    public final D data;
    private @Nullable LastTransition<S, I, T, D> lastTransition;

    public DFRecord(S state, Collection<? extends I> inputs, D data) {
        super(state, inputs);
        this.data = data;
    }

    public @Nullable LastTransition<S, I, T, D> getLastTransition() {
        LastTransition<S, I, T, D> result = lastTransition;
        lastTransition = null;
        return result;
    }

    public void setLastTransition(I input, T transition, S targetState, D tgtData) {
        assert lastTransition == null;
        lastTransition = new LastTransition<>(input, transition, targetState, tgtData);
    }

    public static class LastTransition<S, I, T, D> {

        public final I input;
        public final T transition;
        public final S state;
        public final D data;

        LastTransition(I input, T transition, S state, D data) {
            this.input = input;
            this.transition = transition;
            this.state = state;
            this.data = data;
        }
    }

}
