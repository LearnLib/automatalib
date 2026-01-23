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
package net.automatalib.automaton.mmlt;

import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.word.Word;

/**
 * Defines the semantics of an MMLT.
 * <p>
 * The semantics of an MMLT are defined with an associated Mealy machine. The {@link State states} of this machine
 * represent tuples of an active location and the current valuation of timers of this location. The inputs of the
 * machine are {@link InputSymbol non-delaying inputs}, {@link TimeStepSequence discrete time steps}, and the
 * {@link TimeoutSymbol symbolic input timeout}, which causes a delay until the next timeout.
 * <p>
 * The input alphabet of this machine consists of all non-delaying inputs of the associated MMLT, as well as a
 * {@link TimeStepSequence time step symbol} and the {@link TimeoutSymbol symbolic input timeout}.
 * <p>
 * The outputs of this machine are the outputs of the MMLT, extended with a delay. This delay is zero for all
 * transitions, except for those with the {@link TimeoutSymbol} input.
 *
 * @param <S>
 *         location type
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <T>
 *         transition type
 * @param <O>
 *         output symbol type
 */
public interface MMLTSemantics<S, I, T, O> extends MealyTransitionSystem<State<S, O>, TimedInput<I>, T, TimedOutput<O>>,
                                                   SuffixOutput<TimedInput<I>, Word<TimedOutput<O>>>,
                                                   InputAlphabetHolder<TimedInput<I>> {

    /**
     * Returns the symbol used for silent outputs.
     *
     * @return the silent output symbol
     */
    TimedOutput<O> getSilentOutput();

    /**
     * Retrieves the transition in the semantics automaton that has the provided input and source configuration.
     * <p>
     * If the input is a sequence of time steps, the target of the transition is the configuration reached after
     * executing all time steps. The output of the transition is the output of the time step that was executed last.
     * This output might belong to a timeout or be silence. The delay of this output is set to zero.
     * <p>
     * Please note that a sequence with more than one time step may trigger multiple timeouts. Regardless of that, only
     * the output at the last time step is returned.
     *
     * @param source
     *         the source configuration
     * @param input
     *         the input symbol
     * @param maxWaitingTime
     *         the maximum time steps to wait for a timeout
     *
     * @return the transition in semantics automaton
     */
    T getTransition(State<S, O> source, TimedInput<I> input, long maxWaitingTime);
}
