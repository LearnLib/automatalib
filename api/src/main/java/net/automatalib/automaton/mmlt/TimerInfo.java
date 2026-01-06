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

import java.util.List;

/**
 * Provides information about a timer that is stored in an MMLT.
 *
 * @param name
 *         the name of the timer
 * @param initial
 *         the initial value of the timer
 * @param outputs
 *         the symbols that the timer produces at timeout (must not be empty, must not include silence)
 *         in the MMLT semantics, these are combined deterministically to a single output using a {@link SymbolCombiner}
 * @param target
 *         the target state of this timer
 * @param periodic
 *         {@code true} if the timer is periodic, {@code false} otherwise
 * @param <S>
 *         location type
 * @param <O>
 *         output symbol type
 */
public record TimerInfo<S, O>(String name, long initial, List<O> outputs, S target, boolean periodic) {

    public TimerInfo {
        if (initial <= 0) {
            throw new IllegalArgumentException("Timer values must be greater than zero.");
        }
    }

    /**
     * Convenience constructor for creating a periodic timer. This constructor calls {@link TimerInfo} with
     * {@code periodic} set to {@code true}.
     *
     * @param name
     *         the name of the timer
     * @param initial
     *         the initial value of the timer
     * @param output
     *         the symbol that the timer produces at timeout (must not be silent)
     * @param target
     *         the target state of this timer
     */
    public TimerInfo(String name, long initial, O output, S target) {
        this(name, initial, List.of(output), target, true);
    }

    /**
     * Returns a copy of {@code this} timer info with {@link #periodic} set to {@code false}.
     *
     * @return this timer as a one-shot timer
     */
    public TimerInfo<S, O> asOneShot() {
        return new TimerInfo<>(name, initial, outputs, target, false);
    }
}
