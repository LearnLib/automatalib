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
package net.automatalib.api.alphabet;

import java.util.Objects;

/**
 * A specialized {@link Alphabet} for procedural systems that combines a regular output alphabet with a designated error
 * symbol.
 *
 * @param <O>
 *         output symbol type
 */
public interface ProceduralOutputAlphabet<O> extends Alphabet<O> {

    /**
     * Returns the regular output symbols of this alphabet. Note that this alphabet must not contain the
     * {@link #getErrorSymbol() error symbol}.
     *
     * @return the regular output symbols of this alphabet
     */
    Alphabet<O> getRegularAlphabet();

    /**
     * Returns the error symbol of this alphabet.
     *
     * @return the error symbol
     */
    O getErrorSymbol();

    /**
     * Convenience method that compares the given {@code symbol} with {@code this} alphabet's
     * {@link #getErrorSymbol() error symbol}.
     *
     * @param symbol
     *         the symbol to check
     *
     * @return {@code true} if {@code symbol} equals this alphabet's {@link #getErrorSymbol() error symbol},
     * {@code false} otherwise.
     */
    default boolean isErrorSymbol(O symbol) {
        return Objects.equals(getErrorSymbol(), symbol);
    }

}
