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
package net.automatalib.automaton;

import java.util.function.IntFunction;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.abstraction.DeterministicAbstractions.FullIntAbstraction;
import net.automatalib.automaton.abstraction.DeterministicAbstractions.FullIntAbstractionImpl;
import net.automatalib.automaton.abstraction.DeterministicAbstractions.StateIntAbstraction;
import net.automatalib.automaton.abstraction.DeterministicAbstractions.StateIntAbstractionImpl;
import net.automatalib.automaton.simple.SimpleDeterministicAutomaton;
import net.automatalib.semantic.DeterministicSemantics;
import net.automatalib.ts.DeterministicTransitionSystem;

/**
 * Basic interface for a deterministic automaton. A deterministic automaton is a {@link DeterministicTransitionSystem}
 * with a finite number of states.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 */
public interface DeterministicAutomaton<S, I, T>
        extends Automaton<S, I, T>, SimpleDeterministicAutomaton<S, I>, DeterministicTransitionSystem<S, I, T> {

    @Override
    default FullIntAbstraction<T> fullIntAbstraction(Alphabet<I> alphabet) {
        return fullIntAbstraction(alphabet.size(), alphabet);
    }

    @Override
    default FullIntAbstraction<T> fullIntAbstraction(int numInputs, IntFunction<? extends I> symMapping) {
        return new FullIntAbstractionImpl<>(stateIntAbstraction(), numInputs, symMapping);
    }

    @Override
    default StateIntAbstraction<I, T> stateIntAbstraction() {
        return new StateIntAbstractionImpl<>(this);
    }

    interface FiniteSemantics<S, I, T>
            extends DeterministicAutomaton<S, I, T>, Automaton.FiniteSemantics<S, I, T>, DeterministicSemantics {

        @Override
        default DeterministicAutomaton<S, I, T> getSemantics() {
            return this;
        }
    }

}
