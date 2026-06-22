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
package net.automatalib.automaton.concept;

import java.util.Collections;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The concept of providing a single (potentially undefined) initial state.
 *
 * @param <S>
 *         state type
 */
@FunctionalInterface
public interface InitialState<S> extends InitialStates<S> {

    /**
     * Retrieves the initial state of a transition system.
     *
     * @return the initial state.
     *
     * @see InitialStates#getInitialStates()
     */
    @Nullable S getInitialState();

    @Override
    default Set<S> getInitialStates() {
        return stateToSet(getInitialState());
    }

    static <S> Set<S> stateToSet(@Nullable S state) {
        if (state == null) {
            return Collections.emptySet();
        }
        return Collections.singleton(state);
    }
}
