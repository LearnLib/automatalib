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
package net.automatalib.modelchecker.ltsmin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.etf.writer.Mealy2ETFWriterIO;
import net.automatalib.serialization.fsm.parser.FSM2MealyParserIO;

/**
 * A model checker using LTSmin for Mealy machines using synchronous edge semantics.
 * <p>
 * The implementation uses {@link FSM2MealyParserIO}, and {@link Mealy2ETFWriterIO}, to read the {@link MealyMachine},
 * and write the {@link MealyMachine} respectively.
 *
 * @param <I>
 *         the input type.
 * @param <O>
 *         the output type.
 * @param <R>
 *         the type of counterexample
 */
public interface LTSminIO<I, O, R> extends LTSminMealy<I, O, R> {

    @Override
    default CompactMealy<I, O> fsm2Mealy(File fsm,
                                         MealyMachine<?, I, ?, O> originalAutomaton,
                                         Collection<? extends I> inputs) throws IOException, FormatException {
        return FSM2MealyParserIO.getParser(inputs, getString2Input(), getString2Output()).readModel(fsm);
    }

    @Override
    default void mealy2ETF(MealyMachine<?, I, ?, O> automaton, Collection<? extends I> inputs, File etf)
            throws IOException {
        Mealy2ETFWriterIO.<I, O>getInstance().writeModel(etf, automaton, Alphabets.fromCollection(inputs));
    }
}
