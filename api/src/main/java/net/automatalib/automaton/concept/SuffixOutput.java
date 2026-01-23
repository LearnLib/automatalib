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
package net.automatalib.automaton.concept;

import net.automatalib.exception.UndefinedPropertyAccessException;
import net.automatalib.word.Word;

/**
 * Feature for transition systems that compute a <i>suffix-observable</i> output function, i.e., they compute an output
 * containing a part that can be attributed to a suffix of the input.
 * <p>
 * Note that this is a special case of the {@link Output} feature, as
 * {@code computeOutput(input) = computeSuffixOutput(ε, input)}.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
@FunctionalInterface
public interface SuffixOutput<I, D> extends Output<I, D> {

    @Override
    default D computeOutput(Iterable<? extends I> input) {
        return computeSuffixOutput(Word.epsilon(), input);
    }

    /**
     * Computes the output for the given suffix from the state reached by the given prefix.
     *
     * @param prefix
     *         the sequence of input symbols for reaching the state from which the output computation should start
     * @param suffix
     *         the sequence of input symbols that should be considered for computing the output
     *
     * @return the computed output
     *
     * @throws UndefinedPropertyAccessException
     *         if the computation encountered undefined transitions that would have been required for computing the
     *         output
     */
    D computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix);
}
