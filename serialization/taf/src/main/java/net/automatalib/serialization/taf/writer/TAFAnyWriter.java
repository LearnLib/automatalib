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
package net.automatalib.serialization.taf.writer;

import java.io.IOException;
import java.io.OutputStream;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.FiniteAlphabetAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.serialization.InputModelSerializer;

class TAFAnyWriter<S, I, T, O, A extends FiniteAlphabetAutomaton<S, I, T>> implements InputModelSerializer<I, A> {

    @Override
    public void writeModel(OutputStream os, A automaton, Alphabet<I> inputs) throws IOException {
        if (automaton instanceof DFA) {
            @SuppressWarnings("unchecked")
            final DFA<S, I> dfa = (DFA<S, I>) automaton;
            final TAFConcreteWriter<S, I, ?, ?, DFA<S, I>> writer =
                    new TAFConcreteWriter<>("dfa", TAFWriters::extractSPDFA);
            writer.writeModel(os, dfa, inputs);
        } else if (automaton instanceof MealyMachine) {
            @SuppressWarnings("unchecked")
            final MealyMachine<S, I, T, O> mealy = (MealyMachine<S, I, T, O>) automaton;
            final TAFConcreteWriter<S, I, T, O, MealyMachine<S, I, T, O>> writer =
                    new TAFConcreteWriter<>("mealy", TAFWriters::extractSPMealy);
            writer.writeModel(os, mealy, inputs);
        } else {
            throw new IllegalArgumentException("Unknown type " + automaton.getClass().getSimpleName());
        }
    }
}
