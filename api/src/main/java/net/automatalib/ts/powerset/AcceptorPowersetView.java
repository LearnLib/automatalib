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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.automatalib.ts.AcceptorPowersetViewTS;
import net.automatalib.ts.acceptor.AcceptorTS;

/**
 * A (default) {@link AcceptorPowersetViewTS} implementation that represents states of the original acceptor transition
 * system via {@link Set}s.
 *
 * @param <S>
 *         (original) state type
 * @param <I>
 *         input symbol type
 */
public class AcceptorPowersetView<S, I> implements AcceptorPowersetViewTS<Set<S>, I, S> {

    private final AcceptorTS<S, I> ts;

    public AcceptorPowersetView(AcceptorTS<S, I> ts) {
        this.ts = ts;
    }

    @Override
    public Set<S> getInitialState() {
        return this.ts.getInitialStates();
    }

    @Override
    public Set<S> getTransition(Set<S> state, I input) {
        // store in a list first to prevent re-sizes, better performance according to benchmarks
        final List<S> result = new LinkedList<>();
        for (S s : state) {
            for (S t : this.ts.getTransitions(s, input)) {
                // LinkedList's #add is faster than #addAll
                result.add(t);
            }
        }
        return new HashSet<>(result);
    }

    @Override
    public boolean isAccepting(Set<S> state) {
        return this.ts.isAccepting(state);
    }

    @Override
    public Collection<S> getOriginalStates(Set<S> state) {
        return state;
    }

    @Override
    public Collection<S> getOriginalTransitions(Set<S> transition) {
        return transition;
    }
}
