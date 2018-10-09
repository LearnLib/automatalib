/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.modelcheckers.ltsmin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.serialization.etf.writer.Mealy2ETFWriterIO;
import net.automatalib.serialization.fsm.parser.FSM2MealyParserIO;
import net.automatalib.serialization.fsm.parser.FSMParseException;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * An model checker using LTSmin for Mealy machines using synchronous edge semantics.
 *
 * The implementation uses {@link FSM2MealyParserIO}, and {@link Mealy2ETFWriterIO}, to read the {@link
 * MealyMachine}, and write the {@link MealyMachine} respectively.
 *
 * @param <I> the input type.
 * @param <O> the output type.
 * @param <R> the type of a counterexample
 *
 * @author Jeroen Meijer
 */
public interface LTSminIO<I, O, R> extends LTSminMealy<I, O, R> {

    @Override
    default CompactMealy<I, O> fsm2Mealy(File fsm, MealyMachine<?, I, ?, O> originalAutomaton)
            throws IOException, FSMParseException {
        return FSM2MealyParserIO.parse(fsm, getString2Input(), getString2Output());
    }

    @Override
    default void mealy2ETF(MealyMachine<?, I, ?, O> automaton, Collection<? extends I> inputs, File etf)
            throws IOException {
        final Alphabet<I> alphabet = Alphabets.fromCollection(inputs);
        Mealy2ETFWriterIO.write(etf, automaton, alphabet);
    }
}
