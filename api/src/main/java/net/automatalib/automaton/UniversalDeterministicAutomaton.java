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
import net.automatalib.automaton.abstraction.UniversalDeterministicAbstractions.FullIntAbstraction;
import net.automatalib.automaton.abstraction.UniversalDeterministicAbstractions.FullIntAbstractionImpl;
import net.automatalib.automaton.abstraction.UniversalDeterministicAbstractions.StateIntAbstraction;
import net.automatalib.automaton.abstraction.UniversalDeterministicAbstractions.StateIntAbstractionImpl;
import net.automatalib.semantic.DeterministicFiniteSemantics;
import net.automatalib.ts.UniversalDTS;

/**
 * A {@link DeterministicAutomaton} with state and transition properties.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 *
 * @see UniversalAutomaton
 */
public interface UniversalDeterministicAutomaton<S, I, T, SP, TP>
        extends DeterministicAutomaton<S, I, T>, UniversalDTS<S, I, T, SP, TP>, UniversalAutomaton<S, I, T, SP, TP> {

    @Override
    default FullIntAbstraction<T, SP, TP> fullIntAbstraction(Alphabet<I> alphabet) {
        return fullIntAbstraction(alphabet.size(), alphabet);
    }

    @Override
    default FullIntAbstraction<T, SP, TP> fullIntAbstraction(int numInputs, IntFunction<? extends I> symMapping) {
        return new FullIntAbstractionImpl<>(stateIntAbstraction(), numInputs, symMapping);
    }

    @Override
    default StateIntAbstraction<I, T, SP, TP> stateIntAbstraction() {
        return new StateIntAbstractionImpl<>(this);
    }

    /**
     * Convenience interface that describes an automaton with finite syntactic and finite semantic state space. This
     * type links a {@link UniversalDeterministicAutomaton} with {@link DeterministicFiniteSemantics}.
     *
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     */
    interface RegularAutomaton<S, I, T, SP, TP> extends UniversalDeterministicAutomaton<S, I, T, SP, TP>,
                                                        DeterministicAutomaton.RegularAutomaton<S, I, T>,
                                                        UniversalAutomaton.RegularAutomaton<S, I, T, SP, TP> {

        @Override
        default UniversalDeterministicAutomaton<S, I, T, SP, TP> getSemantics() {
            return this;
        }
    }

}

