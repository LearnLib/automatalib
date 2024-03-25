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
package net.automatalib.ts.powerset;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.common.util.HashUtil;
import net.automatalib.ts.PowersetViewTS;
import net.automatalib.ts.TransitionSystem;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PowersetView<S, I, T> implements PowersetViewTS<Set<S>, I, Set<T>, S, T> {

    private final TransitionSystem<S, I, T> ts;

    public PowersetView(TransitionSystem<S, I, T> ts) {
        this.ts = ts;
    }

    @Override
    public Set<S> getInitialState() {
        return ts.getInitialStates();
    }

    @Override
    public Set<S> getSuccessor(Set<T> transition) {
        Set<S> result = new HashSet<>(HashUtil.capacity(transition.size()));
        for (T trans : transition) {
            result.add(ts.getSuccessor(trans));
        }
        return result;
    }

    @Override
    public @Nullable Set<S> getSuccessor(Set<S> state, I input) {
        Set<S> result = ts.getSuccessors(state, input);

        return result.isEmpty() ? null : result;
    }

    @Override
    public @Nullable Set<T> getTransition(Set<S> state, I input) {
        Set<T> result = new HashSet<>();
        for (S s : state) {
            Collection<T> transitions = ts.getTransitions(s, input);
            result.addAll(transitions);
        }
        return result.isEmpty() ? null : result;
    }

    @Override
    public Collection<S> getOriginalStates(Set<S> state) {
        return state;
    }

    @Override
    public Collection<T> getOriginalTransitions(Set<T> transition) {
        return transition;
    }
}
