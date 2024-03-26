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
package net.automatalib.ts.powerset.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.common.util.nid.NumericID;
import net.automatalib.ts.PowersetViewTS;
import net.automatalib.ts.TransitionSystem;

public class FastPowersetDTS<S extends NumericID, I, T>
        implements PowersetViewTS<FastPowersetState<S>, I, Collection<T>, S, T> {

    private final TransitionSystem<S, I, T> ts;

    public FastPowersetDTS(TransitionSystem<S, I, T> ts) {
        this.ts = ts;
    }

    @Override
    public FastPowersetState<S> getInitialState() {
        FastPowersetState<S> result = new FastPowersetState<>();
        for (S init : ts.getInitialStates()) {
            result.add(init, init.getId());
        }
        return result;
    }

    @Override
    public FastPowersetState<S> getSuccessor(Collection<T> transition) {
        FastPowersetState<S> succ = new FastPowersetState<>();
        for (T t : transition) {
            S succS = ts.getSuccessor(t);
            succ.add(succS, succS.getId());
        }

        return succ;
    }

    @Override
    public Collection<T> getTransition(FastPowersetState<S> state, I input) {
        List<T> result = new ArrayList<>();
        for (S s : state) {
            result.addAll(ts.getTransitions(s, input));
        }
        return result;
    }

    @Override
    public Collection<S> getOriginalStates(FastPowersetState<S> state) {
        return state;
    }

    @Override
    public Collection<T> getOriginalTransitions(Collection<T> transition) {
        return transition;
    }

}
