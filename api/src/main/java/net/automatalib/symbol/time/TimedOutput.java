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

import java.util.Objects;

/**
 * Output that may occur with some or no delay.
 *
 * @param symbol
 *         the output symbol
 * @param delay
 *         the delay
 * @param <O>
 *         output symbol type
 */
public record TimedOutput<O>(O symbol, long delay) {

    public TimedOutput {
        if (delay < 0) {
            throw new IllegalArgumentException("Delay must not be negative.");
        }
    }

    /**
     * Convenience constructor for creating a timed output with no delay.
     *
     * @param symbol
     *         the output symbol
     */
    public TimedOutput(O symbol) {
        this(symbol, 0);
    }

    /**
     * Checks whether this timed output is delayed.
     *
     * @return {@code true}, if {@code delay > 0}, {@code false} otherwise
     */
    public boolean isDelayed() {
        return this.delay > 0;
    }

    @Override
    public String toString() {
        if (this.isDelayed()) {
            return String.format("[%d]%s", this.delay, this.symbol);
        }
        return Objects.toString(this.symbol);
    }

}
