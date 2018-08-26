/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.automata.base.compact;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.ts.powerset.DeterministicPowersetView;
import net.automatalib.words.Alphabet;

@ParametersAreNonnullByDefault
public abstract class AbstractCompactDeterministic<I, T, SP, TP> extends AbstractCompact<I, T, SP, TP> implements
                                                                                                       MutableDeterministic<Integer, I, T, SP, TP>,
                                                                                                       MutableDeterministic.StateIntAbstraction<I, T, SP, TP>,
                                                                                                       MutableDeterministic.FullIntAbstraction<T, SP, TP>,
                                                                                                       Serializable {

    private int initial = AbstractCompact.INVALID_STATE;

    public AbstractCompactDeterministic(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public AbstractCompactDeterministic(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
    }

    public AbstractCompactDeterministic(Alphabet<I> alphabet, AbstractCompactDeterministic<?, ?, ?, ?> other) {
        super(alphabet, other);
        this.initial = other.initial;
    }

    @Override
    public void setInitialState(@Nullable Integer state) {
        setInitialState(getId(state));
    }

    @Override
    public void setInitialState(int stateId) {
        initial = stateId;
    }

    @Nullable
    @Override
    public SP getStateProperty(Integer state) {
        return getStateProperty(state.intValue());
    }

    @Override
    public int getIntInitialState() {
        return initial;
    }

    @Override
    public Integer getInitialState() {
        return makeId(initial);
    }

    @Override
    public T getTransition(int state, I input) {
        return getTransition(state, alphabet.getSymbolIndex(input));
    }

    @Nullable
    @Override
    public T getTransition(Integer state, @Nullable I input) {
        return getTransition(state.intValue(), input);
    }

    @Override
    public void setTransition(Integer state, @Nullable I input, @Nullable T transition) {
        setTransition(state.intValue(), alphabet.getSymbolIndex(input), transition);
    }

    @Override
    public void setTransition(int state, I input, T transition) {
        setTransition(state, alphabet.getSymbolIndex(input), transition);
    }

    @Override
    public void setTransition(int state, I input, int successor, TP property) {
        setTransition(state, alphabet.getSymbolIndex(input), successor, property);
    }

    @Override
    public void setStateProperty(Integer state, @Nullable SP property) {
        setStateProperty(state.intValue(), property);
    }

    @Nonnull
    @Override
    public T createTransition(Integer successor, @Nullable TP properties) {
        return createTransition(successor.intValue(), properties);
    }

    @Override
    public int addIntState(@Nullable SP property) {
        return addState(property);
    }

    @Override
    public int addIntInitialState(@Nullable SP property) {
        return addInitialState(property);
    }

    @Override
    public int numInputs() {
        return alphabetSize;
    }

    @Override
    public Integer getSuccessor(T transition) {
        return makeId(getIntSuccessor(transition));
    }

    @Override
    public void clear() {
        this.initial = AbstractCompact.INVALID_STATE;
        super.clear();
    }

    @Override
    public FullIntAbstraction<T, SP, TP> fullIntAbstraction(Alphabet<I> alphabet) {
        if (Objects.equals(this.alphabet, alphabet)) {
            return this;
        }
        return MutableDeterministic.super.fullIntAbstraction(alphabet);
    }

    @Override
    public StateIntAbstraction<I, T, SP, TP> stateIntAbstraction() {
        return this;
    }

    @Override
    public DeterministicPowersetView<Integer, I, T> powersetView() {
        return new DeterministicPowersetView<>(this);
    }

}
