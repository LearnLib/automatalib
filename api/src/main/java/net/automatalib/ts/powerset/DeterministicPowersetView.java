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
package net.automatalib.ts.powerset;

import java.util.Collection;
import java.util.Collections;

import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.PowersetViewTS;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A thin {@link PowersetViewTS} wrapper that may be used if the given transition system already is deterministic.
 *
 * @param <S>
 *         (original) state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         (original) transition type
 */
public class DeterministicPowersetView<S, I, T>
        implements DeterministicTransitionSystem<S, I, T>, PowersetViewTS<S, I, T, S, T> {

    private final DeterministicTransitionSystem<S, I, T> delegate;

    public DeterministicPowersetView(DeterministicTransitionSystem<S, I, T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public @Nullable T getTransition(S state, I input) {
        return delegate.getTransition(state, input);
    }

    @Override
    public S getSuccessor(T transition) {
        return delegate.getSuccessor(transition);
    }

    @Override
    public @Nullable S getInitialState() {
        return delegate.getInitialState();
    }

    @Override
    public Collection<S> getOriginalStates(S state) {
        return Collections.singleton(state);
    }

    @Override
    public Collection<T> getOriginalTransitions(T transition) {
        return Collections.singleton(transition);
    }

}
