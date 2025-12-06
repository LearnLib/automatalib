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
package net.automatalib.automaton.concept;

import java.util.Collection;

import net.automatalib.exception.UndefinedPropertyAccessException;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * Feature for transition systems that compute an output. Here, output refers to the <i>complete</i> output that is made
 * when an input word is read, not a single symbol.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
@FunctionalInterface
public interface Output<I, D> {

    /**
     * Computes the output for the given sequence of input symbols.
     *
     * @param input
     *         the sequence of input symbols
     *
     * @return the computed output
     *
     * @throws UndefinedPropertyAccessException
     *         if the computation encountered undefined transitions that would have been required for computing the
     *         output
     */
    D computeOutput(Iterable<? extends I> input);

    /**
     * Convenience method for {@link #getBuilderFor(Iterable, int)} which uses {@code 0} for
     * {@code additionalElements}.
     *
     * @param iterable
     *         the sequence of input symbols
     * @param <T>
     *         symbol type
     *
     * @return a pre-sized builder (may use the default size if no information could be extracted from the iterable)
     */
    static <T> WordBuilder<T> getBuilderFor(Iterable<?> iterable) {
        return getBuilderFor(iterable, 0);
    }

    /**
     * Utility method for constructing a pre-sized builder (if possible) for storing responses generated when applying
     * the provided sequence of input symbols.
     *
     * @param iterable
     *         the sequence of input symbols
     * @param additionalElements
     *         number of elements that is added to the size of the iterable
     * @param <T>
     *         symbol type
     *
     * @return a pre-sized builder (may use the default size if no information could be extracted from the iterable)
     */
    static <T> WordBuilder<T> getBuilderFor(Iterable<?> iterable, int additionalElements) {
        if (iterable instanceof Word<?> w) {
            return new WordBuilder<>(w.length() + additionalElements);
        } else if (iterable instanceof Collection<?> c) {
            return new WordBuilder<>(c.size() + additionalElements);
        } else {
            return new WordBuilder<>();
        }
    }
}
