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
package net.automatalib.symbol.time;

import java.util.function.Supplier;

import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * Markup-interface for concrete, time-sensitive inputs. Contains utility methods for conveniently constructing
 * instances of timed symbols.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 *
 * @see MMLTSemantics
 */
public sealed interface TimedInput<I> permits InputSymbol, TimeoutSymbol, TimeStepSequence {

    /**
     * Wraps a raw input symbol into a non-delaying timed input.
     *
     * @param symbol
     *         the symbol to wrap
     * @param <I>
     *         input symbol type (of non-delaying inputs)
     *
     * @return the non-delaying timed input
     */
    static <I> InputSymbol<I> input(I symbol) {
        return new InputSymbol<>(symbol);
    }

    /**
     * Wraps multiple raw input symbols into a word of non-delaying timed inputs.
     *
     * @param symbols
     *         the symbols to wrap
     * @param <I>
     *         input symbol type (of non-delaying inputs)
     *
     * @return the non-delaying timed input word
     */
    @SafeVarargs
    static <I> Word<InputSymbol<I>> inputs(I... symbols) {
        final WordBuilder<InputSymbol<I>> wb = new WordBuilder<>(symbols.length);
        for (I symbol : symbols) {
            wb.add(new InputSymbol<>(symbol));
        }
        return wb.toWord();
    }

    /**
     * Returns the timeout symbol.
     *
     * @param <I>
     *         input symbol type (of non-delaying inputs)
     *
     * @return the timeout symbol
     */
    static <I> TimeoutSymbol<I> timeout() {
        return new TimeoutSymbol<>();
    }

    /**
     * Returns a word of multiple timeout symbols.
     *
     * @param i
     *         the number of timeout symbols the result should contain
     * @param <I>
     *         input symbol type (of non-delaying inputs)
     *
     * @return the word of timeout symbols
     */
    static <I> Word<TimeoutSymbol<I>> timeouts(int i) {
        return generate(i, TimeoutSymbol::new);
    }

    /**
     * Returns a time step symbol with a length of 1.
     *
     * @param <I>
     *         input symbol type (of non-delaying inputs)
     *
     * @return the timestep symbol
     */
    static <I> TimeStepSequence<I> step() {
        return new TimeStepSequence<>();
    }

    /**
     * Returns a time step symbol with the given length.
     *
     * @param i
     *         the length of the timestep
     * @param <I>
     *         input symbol type (of non-delaying inputs)
     *
     * @return the timestep symbol
     */
    static <I> TimeStepSequence<I> step(long i) {
        return new TimeStepSequence<>(i);
    }

    /**
     * Returns a word of multiple steps symbols with length 1 each.
     *
     * @param i
     *         the number of timestep symbols the result should contain
     * @param <I>
     *         input symbol type (of non-delaying inputs)
     *
     * @return the word of timestep symbols
     */
    static <I> Word<TimeStepSequence<I>> steps(int i) {
        return generate(i, TimeStepSequence::new);
    }

    private static <T> Word<T> generate(int i, Supplier<T> supplier) {
        final WordBuilder<T> wb = new WordBuilder<>(i);
        for (int j = 0; j < i; j++) {
            wb.append(supplier.get());
        }

        return wb.toWord();
    }
}
