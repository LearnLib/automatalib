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
package net.automatalib.automaton.mmlt;

import java.util.List;

/**
 * A symbol combiner deterministically maps between multiple output symbols and a single representative. This
 * functionality is currently used by {@link MMLT}s when multiple timeouts occur simultaneously.
 *
 * @param <S>
 *         symbol type
 */
public interface SymbolCombiner<S> {

    /**
     * Indicates if the provided symbol is a combined symbol.
     *
     * @param symbol
     *         the symbol for testing
     *
     * @return {@code true} if combined suffix, {@code false} otherwise.
     */
    boolean isCombinedSymbol(S symbol);

    /**
     * Combines the provided symbols to a single symbol of same data type. Must be deterministic.
     *
     * @param symbols
     *         the provided symbols
     *
     * @return the combined suffix
     */
    S combineSymbols(List<S> symbols);

    /**
     * Attempts to separate the provided combined symbol into individual symbols.
     *
     * @param symbol
     *         the combined symbol
     *
     * @return the individual symbols
     */
    List<S> separateSymbols(S symbol);
}
