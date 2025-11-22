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

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Provides information about a timer that is stored in an MMLT.
 *
 * @param <S>
 *         location type
 * @param <O>
 *         output symbol type
 */
public final class MealyTimerInfo<S, O> {

    /**
     * Name of the timer.
     */
    private final String name;

    /**
     * Initial value of the timer.
     */
    private final long initial;

    /**
     * Symbol that the timer produces at timeout. Must not be silent.
     */
    private final O output;

    /**
     * True if the timer is periodic.
     */
    private boolean periodic;

    private final S target;

    public MealyTimerInfo(String name, long initial, O output, boolean periodic, S target) {
        this.target = target;
        if (initial <= 0) {
            throw new IllegalArgumentException("Timer values must be greater than zero.");
        }

        this.name = name;
        this.initial = initial;
        this.output = output;
        this.periodic = periodic;
    }

    public MealyTimerInfo(String name, long initial, O output, S target) {
        this(name, initial, output, true, target);
    }

    public void setOneShot() {
        this.periodic = false;
    }

    public String name() {
        return name;
    }

    public long initial() {
        return initial;
    }

    public boolean periodic() {
        return this.periodic;
    }

    public O output() {
        return output;
    }

    public S target() {
        return target;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return this == o || o instanceof MealyTimerInfo<?, ?> that && Objects.equals(this.name, that.name) &&
                            this.initial == that.initial && Objects.equals(this.output, that.output) &&
                            Objects.equals(this.target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, initial, output, target);
    }

    @Override
    public String toString() {
        return String.format("%s=%d/%s", name, initial, output);
    }

}
