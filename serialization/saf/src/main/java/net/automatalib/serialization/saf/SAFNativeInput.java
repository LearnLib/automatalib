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
package net.automatalib.serialization.saf;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.MutableAutomaton;

class SAFNativeInput<S, T, SP, TP, A extends MutableAutomaton<S, Integer, T, SP, TP>>
        extends AbstractSAFInput<S, Integer, T, SP, TP, A> {

    SAFNativeInput(AutomatonType expectedType,
                   AutomatonCreator<? extends A, Integer> creator,
                   BlockPropertyDecoder<? extends SP> spDecoder,
                   SinglePropertyDecoder<? extends TP> tpDecoder) {
        super(expectedType, creator, spDecoder, tpDecoder);
    }

    @Override
    protected Alphabet<Integer> getAlphabet(int size) {
        return Alphabets.integers(0, size - 1);
    }
}
