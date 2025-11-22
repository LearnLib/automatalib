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

/**
 * An input that represents multiple subsequent time steps.
 *
 * @param timeSteps
 *         the number of time steps this symbol should elapse
 * @param <I>
 *         input symbol type (of other timed symbols)
 */
public record TimeStepSequence<I>(long timeSteps) implements TimedInput<I> {

    public TimeStepSequence {
        if (timeSteps <= 0) {
            throw new IllegalArgumentException("Timeout must be larger than zero.");
        }
    }

    /**
     * Convenience constructor for a timestep of length 1.
     */
    public TimeStepSequence() {
        this(1);
    }

    @Override
    public String toString() {
        return String.format("wait[%d]", this.timeSteps);
    }

}
