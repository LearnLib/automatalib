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
package net.automatalib.automaton.simple;

import java.util.function.IntFunction;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.abstraction.SimpleDeterministicAbstractions.FullIntAbstraction;
import net.automatalib.automaton.abstraction.SimpleDeterministicAbstractions.FullIntAbstractionImpl;
import net.automatalib.automaton.abstraction.SimpleDeterministicAbstractions.StateIntAbstraction;
import net.automatalib.automaton.abstraction.SimpleDeterministicAbstractions.StateIntAbstractionImpl;
import net.automatalib.ts.simple.SimpleDTS;

/**
 * A simple deterministic automaton.
 *
 * @param <S>
 *         state class.
 * @param <I>
 *         input symbol class.
 */
public interface SimpleDeterministicAutomaton<S, I> extends SimpleAutomaton<S, I>, SimpleDTS<S, I> {

    /**
     * Retrieves a {@link FullIntAbstraction} of this automaton, using the mapping induced by the given alphabet as the
     * abstraction for the input symbols.
     * <p>
     * This method is provided for convenience. It is equivalent to calling {@code fullIntAbstraction(alphabet.size(),
     * alphabet)}.
     *
     * @param alphabet
     *         the alphabet inducing the abstraction
     *
     * @return a {@link FullIntAbstraction}
     */
    default FullIntAbstraction fullIntAbstraction(Alphabet<I> alphabet) {
        return fullIntAbstraction(alphabet.size(), alphabet);
    }

    /**
     * Retrieves a {@link FullIntAbstraction} of this automaton, using the given number of (abstract) inputs and the
     * inputs mapping.
     *
     * @param numInputs
     *         the number of inputs represented in the full abstraction
     * @param symMapping
     *         the mapping from integers in the range {@code [0, numInputs - 1]} to input symbols.
     *
     * @return a {@link FullIntAbstraction}
     */
    default FullIntAbstraction fullIntAbstraction(int numInputs, IntFunction<? extends I> symMapping) {
        return new FullIntAbstractionImpl<>(stateIntAbstraction(), numInputs, symMapping);
    }

    /**
     * Retrieves a {@link StateIntAbstraction} of this automaton.
     *
     * @return a {@link StateIntAbstraction}
     */
    default StateIntAbstraction<I> stateIntAbstraction() {
        return new StateIntAbstractionImpl<>(this);
    }

}
