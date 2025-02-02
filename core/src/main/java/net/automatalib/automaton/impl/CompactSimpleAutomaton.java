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
package net.automatalib.automaton.impl;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.base.AbstractCompactSimpleNondet;

/**
 * A simple automaton that only stores adjacency information.
 *
 * @param <I>
 *         input symbol type
 */
public class CompactSimpleAutomaton<I> extends AbstractCompactSimpleNondet<I, Void> {

    public CompactSimpleAutomaton(Alphabet<I> alphabet, int stateCapacity) {
        super(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
    }

    public CompactSimpleAutomaton(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY);
    }

    @Override
    public void setStateProperty(int state, Void property) {}

    @Override
    public Void getStateProperty(int state) {
        return null;
    }

    public static final class Creator<I> implements AutomatonCreator<CompactSimpleAutomaton<I>, I> {

        @Override
        public CompactSimpleAutomaton<I> createAutomaton(Alphabet<I> alphabet, int numStates) {
            return new CompactSimpleAutomaton<>(alphabet, numStates);
        }

        @Override
        public CompactSimpleAutomaton<I> createAutomaton(Alphabet<I> alphabet) {
            return new CompactSimpleAutomaton<>(alphabet);
        }
    }

}
