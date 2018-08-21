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
package net.automatalib.automata.base.fast;

import java.io.Serializable;

import net.automatalib.automata.ShrinkableDeterministic;
import net.automatalib.ts.powerset.DeterministicPowersetView;
import net.automatalib.words.Alphabet;

public abstract class AbstractFastMutableDet<S extends AbstractFastState<T>, I, T, SP, TP>
        extends AbstractFastMutable<S, I, T, SP, TP> implements ShrinkableDeterministic<S, I, T, SP, TP>, Serializable {

    private S initialState;

    public AbstractFastMutableDet(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    public void setTransition(S state, I input, T transition) {
        int inputIdx = inputAlphabet.getSymbolIndex(input);
        state.setTransitionObject(inputIdx, transition);
    }

    @Override
    public S getInitialState() {
        return initialState;
    }

    @Override
    public void setInitialState(S state) {
        this.initialState = state;
    }

    @Override
    public T getTransition(S state, I input) {
        int inputIdx = inputAlphabet.getSymbolIndex(input);
        return state.getTransitionObject(inputIdx);
    }

    @Override
    public void removeState(S state, S replacement) {
        super.removeState(state, replacement);

        if (state.equals(initialState)) {
            this.initialState = replacement;
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.initialState = null;
    }

    @Override
    public DeterministicPowersetView<S, I, T> powersetView() {
        return new DeterministicPowersetView<>(this);
    }
}
