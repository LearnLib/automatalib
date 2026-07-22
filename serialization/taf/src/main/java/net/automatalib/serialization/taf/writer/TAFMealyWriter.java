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
package net.automatalib.serialization.taf.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.serialization.InputModelSerializer;

class TAFMealyWriter<I, O, A extends MealyMachine<?, I, ?, O>> implements InputModelSerializer<I, A> {

    @Override
    public void writeModel(OutputStream os, A automaton, Alphabet<I> inputs) throws IOException {
        writeModelInternal(os, (MealyMachine<?, I, ?, O>) automaton, inputs);
    }

    static <I, O> void writeModelInternal(OutputStream os, MealyMachine<?, I, ?, O> automaton, Alphabet<I> inputs)
            throws IOException {
        TAFWriterUtil.writeModel(os, automaton, inputs, "mealy", s -> Collections.emptyList());
    }
}

