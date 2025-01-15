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
package net.automatalib.automaton.fsa;

import net.automatalib.automaton.MutableDeterministic;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MutableDFA<S, I> extends DFA<S, I>, MutableDeterministic<S, I, S, Boolean, Void>, MutableFSA<S, I> {

    @Override
    // Overridden for performance reasons (to prevent creating redundant transition objects)
    default void setTransition(S state, I input, @Nullable S transition, Void property) {
        setTransition(state, input, transition);
    }

}
