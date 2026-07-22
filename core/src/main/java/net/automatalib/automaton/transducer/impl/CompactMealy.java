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
package net.automatalib.automaton.transducer.impl;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.impl.CompactTransitionOutput;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.automaton.transducer.StateLocalInputMealyMachine;

public class CompactMealy<I, O> extends CompactTransitionOutput<I, O>
        implements MutableMealyMachine<Integer, I, CompactTransition<O>, O>,
                   StateLocalInputMealyMachine<Integer, I, CompactTransition<O>, O> {

    public CompactMealy(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
    }

    public CompactMealy(Alphabet<I> alphabet, int stateCapacity) {
        super(alphabet, stateCapacity);
    }

    public CompactMealy(Alphabet<I> alphabet) {
        super(alphabet);
    }

    public CompactMealy(CompactMealy<I, O> other) {
        super(other);
    }

    protected CompactMealy(Alphabet<I> alphabet, CompactMealy<?, O> other) {
        super(alphabet, other);
    }

    public <I2> CompactMealy<I2, O> translate(Alphabet<I2> newAlphabet) {
        if (newAlphabet.size() != numInputs()) {
            throw new IllegalArgumentException(
                    "Alphabet sizes must match, but they do not (old/new): " + numInputs() + " vs. " +
                    newAlphabet.size());
        }
        return new CompactMealy<>(newAlphabet, this);
    }

    public static final class Creator<I, O> implements AutomatonCreator<CompactMealy<I, O>, I> {

        @Override
        public CompactMealy<I, O> createAutomaton(Alphabet<I> alphabet, int sizeHint) {
            return new CompactMealy<>(alphabet, sizeHint);
        }

        @Override
        public CompactMealy<I, O> createAutomaton(Alphabet<I> alphabet) {
            return new CompactMealy<>(alphabet);
        }
    }

}
