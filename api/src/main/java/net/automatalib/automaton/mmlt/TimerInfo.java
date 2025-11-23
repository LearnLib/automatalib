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

/**
 * Provides information about a timer that is stored in an MMLT.
 *
 * @param name
 *         name of the timer
 * @param initial
 *         initial value of the timer
 * @param output
 *         symbol that the timer produces at timeout (must not be silent)
 * @param target
 *         the target state of this timer
 * @param periodic
 *         {@code true} if the timer is periodic, {@code false} otherwise
 * @param <S>
 *         location type
 * @param <O>
 *         output symbol type
 */
public record TimerInfo<S, O>(String name, long initial, O output, S target, boolean periodic) {

    public TimerInfo {
        if (initial <= 0) {
            throw new IllegalArgumentException("Timer values must be greater than zero.");
        }
    }

    public TimerInfo(String name, long initial, O output, S target) {
        this(name, initial, output, target, true);
    }

    public TimerInfo<S, O> asOneShot() {
        return new TimerInfo<>(name, initial, output, target, false);
    }

    @Override
    public String toString() {
        return String.format("%s=%d/%s", name, initial, output);
    }
}
