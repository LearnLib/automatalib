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
package net.automatalib.modelchecker.m3c.solver;

import net.automatalib.exception.FormatException;

/**
 * An interface for a generic M3C solver which may need to parse the given formula and thus may throw an exception when
 * doing so.
 *
 * @param <F>
 *         formula type
 */
public interface M3CSolver<F> {

    /**
     * Checks whether the given formula is satisfied.
     *
     * @param formula
     *         the formula whose satisfiability should be checked
     *
     * @return {@code true} if the formula is satisfied, {@code false} otherwise.
     *
     * @throws FormatException
     *         when the given formula object cannot be parsed
     */
    boolean solve(F formula) throws FormatException;

    /**
     * A specialized {@link M3CSolver} which no longer throws a {@link FormatException} when solving a formula, but
     * requires a type-safe formula object.
     *
     * @param <F>
     *         formula type
     */
    interface TypedM3CSolver<F> extends M3CSolver<F> {

        /**
         * Checks whether the given formula is satisfied.
         *
         * @param formula
         *         the formula whose satisfiability should be checked
         *
         * @return {@code true} if the formula is satisfied, {@code false} otherwise.
         */
        @Override
        boolean solve(F formula);
    }
}
