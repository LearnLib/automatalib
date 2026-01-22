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
package net.automatalib.automaton.procedural.impl;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A stack-based state in a procedural system. Consists of a back-reference to the previous stack-element and values for
 * the current top-of-stack configuration.
 *
 * @param <I>
 *         input symbol type
 * @param <S>
 *         hypothesis state type
 * @param <P>
 *         hypothesis type
 */
public final class StackState<S, I, P> {

    private static final StackState<?, ?, ?> INIT = new StackState<>();
    private static final StackState<?, ?, ?> TERM = new StackState<>();

    private final @Nullable StackState<S, I, P> prev;
    private final @Nullable P procedure;
    private final @Nullable S procedureState;

    private StackState() {
        this.prev = null;
        this.procedure = null;
        this.procedureState = null;
    }

    private StackState(StackState<S, I, P> prev, P procedure, S procedureState) {
        this.prev = prev;
        this.procedure = procedure;
        this.procedureState = procedureState;
    }

    StackState<S, I, P> push(P newProcedure, S newState) {
        return new StackState<>(this, newProcedure, newState);
    }

    @SuppressWarnings("return") // since this method is package-private, we are happy to only assert nullability
    StackState<S, I, P> pop() {
        assert !isStatic() : "This method should never be called on static states";
        return prev;
    }

    @SuppressWarnings("type") // since this method is package-private, we are happy to only assert nullability
    StackState<S, I, P> updateState(S state) {
        assert !isStatic() : "This method should never be called on static states";
        return new StackState<>(prev, procedure, state);
    }

    @SuppressWarnings("return") // since this method is package-private, we are happy to only assert nullability
    P getProcedure() {
        assert !isStatic() : "This method should never be called on static states";
        return procedure;
    }

    @SuppressWarnings("return") // since this method is package-private, we are happy to only assert nullability
    S getCurrentState() {
        assert !isStatic() : "This method should never be called on static states";
        return procedureState;
    }

    @SuppressWarnings("unchecked")
    static <I, S, P> StackState<S, I, P> init() {
        return (StackState<S, I, P>) INIT;
    }

    boolean isInit() {
        return this == INIT;
    }

    @SuppressWarnings("unchecked")
    static <I, S, P> StackState<S, I, P> term() {
        return (StackState<S, I, P>) TERM;
    }

    boolean isTerm() {
        return this == TERM;
    }

    private boolean isStatic() {
        return isInit() || isTerm();
    }

}
