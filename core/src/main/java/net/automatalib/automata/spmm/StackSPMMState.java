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
package net.automatalib.automata.spmm;

import net.automatalib.automata.spa.StackSPA;
import net.automatalib.automata.transducers.MealyMachine;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A state in a {@link StackSPA}. Consists of a back-reference to the previous stack-element a values for the current
 * top-of-stack configuration.
 *
 * @param <I>
 *         input symbol type
 * @param <S>
 *         hypothesis state type
 *
 * @author frohme
 */
public final class StackSPMMState<S, I, T, O> {

    private static final StackSPMMState<?, ?, ?, ?> INIT = new StackSPMMState<>();
    private static final StackSPMMState<?, ?, ?, ?> SINK = new StackSPMMState<>();
    private static final StackSPMMState<?, ?, ?, ?> TERM = new StackSPMMState<>();

    private final @Nullable StackSPMMState<S, I, T, O> prev;
    private final @Nullable MealyMachine<S, I, T, O> procedure;
    private final @Nullable S procedureState;

    private StackSPMMState() {
        this.prev = null;
        this.procedure = null;
        this.procedureState = null;
    }

    private StackSPMMState(StackSPMMState<S, I, T, O> prev, MealyMachine<S, I, T, O> procedure, S procedureState) {
        this.prev = prev;
        this.procedure = procedure;
        this.procedureState = procedureState;
    }

    public StackSPMMState<S, I, T, O> push(MealyMachine<S, I, T, O> newProcedure, S newState) {
        return new StackSPMMState<>(this, newProcedure, newState);
    }

    public StackSPMMState<S, I, T, O> pop() {
        assert !isStatic() : "This method should never be called on static states";
        return prev;
    }

    public StackSPMMState<S, I, T, O> updateState(S dfaState) {
        assert !isStatic() : "This method should never be called on static states";
        return new StackSPMMState<>(prev, procedure, dfaState);
    }

    public MealyMachine<S, I, T, O> getProcedure() {
        assert !isStatic() : "This method should never be called on static states";
        return procedure;
    }

    public S getCurrentState() {
        assert !isStatic() : "This method should never be called on static states";
        return procedureState;
    }

    @SuppressWarnings("unchecked")
    public static <S, I, T, O> StackSPMMState<S, I, T, O> sink() {
        return (StackSPMMState<S, I, T, O>) SINK;
    }

    public boolean isSink() {
        return this == SINK;
    }

    @SuppressWarnings("unchecked")
    public static <S, I, T, O> StackSPMMState<S, I, T, O> init() {
        return (StackSPMMState<S, I, T, O>) INIT;
    }

    public boolean isInit() {
        return this == INIT;
    }

    @SuppressWarnings("unchecked")
    public static <S, I, T, O> StackSPMMState<S, I, T, O> term() {
        return (StackSPMMState<S, I, T, O>) TERM;
    }

    public boolean isTerm() {
        return this == TERM;
    }

    // contract is satisfied by definition of constructors
    @SuppressWarnings("contracts.conditional.postcondition.not.satisfied")
    @EnsuresNonNullIf(expression = {"this.prev", "this.procedure", "this.procedureState"}, result = false)
    private boolean isStatic() {
        return isInit() || isTerm() || isSink();
    }

}
