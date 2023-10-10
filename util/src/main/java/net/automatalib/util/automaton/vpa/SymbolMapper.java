/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.util.automaton.vpa;

import java.util.HashMap;
import java.util.Map;

import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.vpa.SEVPA;

/**
 * An interface for mapping (abstract) {@link SEVPA} input symbols to concrete {@link SPA} input symbols.
 *
 * @param <AI>
 *         abstract input symbol type
 * @param <CI>
 *         concrete input symbol type
 */
public interface SymbolMapper<AI, CI> {

    /**
     * Returns for an abstract call symbol of a {@link VPAlphabet} its mapped call symbol of a
     * {@link ProceduralInputAlphabet}.
     * <p>
     * Note that this is a stateful operation, i.e., multiple calls to this method with the same argument should return
     * distinct results.
     *
     * @param ai
     *         abstract (call) input symbol
     *
     * @return the concretized (call) input symbol
     */
    CI mapCallSymbol(AI ai);

    /**
     * Returns for an abstract internal symbol of a {@link VPAlphabet} its mapped internal symbol of a
     * {@link ProceduralInputAlphabet}.
     * <p>
     * Note that this is a stateless operation, i.e., multiple calls to this method with the same argument should return
     * the same result.
     *
     * @param ai
     *         abstract (internal) input symbol
     *
     * @return the concretized (internal) input symbol
     */
    CI mapInternalSymbol(AI ai);

    /**
     * Returns for an abstract return symbol of a {@link VPAlphabet} its mapped return symbol of a
     * {@link ProceduralInputAlphabet}.
     * <p>
     * Note that this is a stateless operation, i.e., multiple calls to this method with the same argument should return
     * the same results.
     *
     * @param ai
     *         abstract (return) input symbol
     *
     * @return the concretized (return) input symbol
     */
    CI mapReturnSymbol(AI ai);

    /**
     * A default implementation that maps abstract input symbol to their {@link String} representations while adding an
     * incrementing number to mapped {@link #mapCallSymbol(Object) call symbols}.
     *
     * @param <AI>
     *         abstract input symbol type
     */
    class StringSymbolMapper<AI> implements SymbolMapper<AI, String> {

        private final Map<AI, Integer> map = new HashMap<>();

        @Override
        public String mapCallSymbol(AI s) {
            map.putIfAbsent(s, -1);
            return String.valueOf(s) + '_' + map.computeIfPresent(s, (k, v) -> v + 1);
        }

        @Override
        public String mapInternalSymbol(AI s) {
            return String.valueOf(s);
        }

        @Override
        public String mapReturnSymbol(AI s) {
            return String.valueOf(s);
        }
    }
}
