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
package net.automatalib.automaton.impl;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.MutableDeterministic.RegularAutomaton;

/**
 * A refinement of {@link UniversalCompactDet} that additionally implements {@link RegularAutomaton} so that structural
 * determinism and finiteness coincides with semantic determinism and finiteness.
 *
 * @param <I>
 *         input symbol type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 */
public class UniversalCompactDetAutomaton<I, SP, TP> extends UniversalCompactDet<I, SP, TP>
        implements RegularAutomaton<Integer, I, CompactTransition<TP>, SP, TP> {

    public UniversalCompactDetAutomaton(Alphabet<I> alphabet) {
        super(alphabet);
    }

    public UniversalCompactDetAutomaton(Alphabet<I> alphabet, int stateCapacity) {
        super(alphabet, stateCapacity);
    }

    public static final class Creator<I, SP, TP>
            implements AutomatonCreator<UniversalCompactDetAutomaton<I, SP, TP>, I> {

        @Override
        public UniversalCompactDetAutomaton<I, SP, TP> createAutomaton(Alphabet<I> alphabet, int numStates) {
            return new UniversalCompactDetAutomaton<>(alphabet, numStates);
        }

        @Override
        public UniversalCompactDetAutomaton<I, SP, TP> createAutomaton(Alphabet<I> alphabet) {
            return new UniversalCompactDetAutomaton<>(alphabet);
        }
    }
}
