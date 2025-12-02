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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A state configuration of an MMLT. A configuration is a tuple of an active location and the valuation of its timers.
 *
 * @param <S>
 *         location type
 * @param <O>
 *         output symbol type
 */
public final class State<S, O> {

    private final S location;

    private final List<TimerInfo<S, O>> sortedTimers;
    private final long[] timerValues;
    private final long[] initialValues;
    private final long minimumTimerValue;

    private final long entryDistance;

    /**
     * Initializes the entry configuration for the provided location, where all timers have their initial value.
     *
     * @param location
     *         the location
     * @param sortedTimers
     *         the timers of the location, sorted by initial value
     */
    public State(S location, List<TimerInfo<S, O>> sortedTimers) {
        this.location = location;

        this.sortedTimers = sortedTimers;

        this.initialValues = new long[sortedTimers.size()];
        this.timerValues = new long[sortedTimers.size()];
        this.minimumTimerValue = sortedTimers.isEmpty() ? 0 : sortedTimers.get(0).initial();

        for (int i = 0; i < sortedTimers.size(); i++) {
            final long initial = sortedTimers.get(i).initial();
            initialValues[i] = initial;
            timerValues[i] = initial; // reset
        }

        this.entryDistance = 0;
    }

    private State(S location,
                  List<TimerInfo<S, O>> sortedTimers,
                  long[] timerValues,
                  long[] initialValues,
                  long entryDistance,
                  long minimumTimerValue) {
        this.location = location;
        this.sortedTimers = sortedTimers;

        this.initialValues = initialValues;
        this.minimumTimerValue = minimumTimerValue;
        this.timerValues = timerValues;
        this.entryDistance = entryDistance;
    }

    /**
     * Returns the MMLT location of this state.
     *
     * @return the location
     */
    public S getLocation() {
        return location;
    }

    /**
     * Returns the entry distance. This is the minimal number of time steps required to reach this configuration from
     * the entry configuration.
     *
     * @return the entry distance
     */
    public long getEntryDistance() {
        return entryDistance;
    }

    /**
     * Indicates if this is the entry configuration of the location. A configuration is the entry configuration if all
     * timers have their initial value.
     *
     * @return {@code true} if this is the entry configuration, {@code false} otherwise
     */
    public boolean isEntryConfig() {
        return this.entryDistance == 0;
    }

    /**
     * Indicates if this configuration is stable. A configuration is stable if its entry distance is less than the
     * initial value of the timer with the lowest initial value of the location. If the location has no timers, its only
     * configuration is its entry configuration, which is always stable.
     *
     * @return {@code true} if stable, {@code false} otherwise
     */
    public boolean isStableConfig() {
        return this.entryDistance == 0 || this.entryDistance < minimumTimerValue;
    }

    /**
     * Returns a copy of {@code this} state with all timers reset to their initial values.
     *
     * @return the new state with all its timers reset
     */
    public State<S, O> resetTimers() {
        return new State<>(location, sortedTimers, initialValues.clone(), initialValues, 0, minimumTimerValue);
    }

    /**
     * Returns all timers that time out in the least number of time steps.
     *
     * @return the timed out timers
     */
    public @Nullable TimeoutPair<S, O> getNextExpiringTimers() {
        if (sortedTimers.isEmpty()) {
            return null;
        } else if (this.sortedTimers.size() == 1) {
            // No need to collect timeouts - there is only one timer that can expire.
            // The time to its timeout is its remaining value:
            return new TimeoutPair<>(this.timerValues[0], Collections.singletonList(this.sortedTimers.get(0)));

        } else {
            // Multiple timers may time out at the same time.
            // Get minimum distance to next timeout:
            long minValue = Long.MAX_VALUE;
            for (int i = 0; i < sortedTimers.size(); i++) {
                if (timerValues[i] < minValue) {
                    minValue = timerValues[i];
                }
            }

            assert minValue != Long.MAX_VALUE;

            // Collect info of all timers that time out then:
            List<TimerInfo<S, O>> expiringTimers = new ArrayList<>();
            for (int i = 0; i < sortedTimers.size(); i++) {
                if (timerValues[i] == minValue) {
                    expiringTimers.add(this.sortedTimers.get(i));
                }
            }

            // Return timed-out timers:
            return new TimeoutPair<>(minValue, expiringTimers);
        }
    }

    /**
     * Returns a new state in which all timer values have been decreased by the specified amount. This amount must be at
     * most the time to the next timeout. If this sets a timer to zero, this timer is immediately reset to its initial
     * value.
     *
     * @param delay
     *         the value by which to decrement the current timers
     *
     * @return the new state with updated timers
     */
    public State<S, O> decrement(long delay) {
        int timerResets = 0;
        int oneShotResets = 0;

        final long[] newTimerValues = this.timerValues.clone();

        for (int i = 0; i < this.sortedTimers.size(); i++) {
            long newValue = newTimerValues[i] - delay;

            if (newValue < 0) {
                throw new IllegalArgumentException("Can only advance to next timeout.");
            } else if (newValue == 0) {
                if (!sortedTimers.get(i).periodic()) {
                    oneShotResets += 1;
                }

                newValue = initialValues[i];
                timerResets += 1;
            }
            newTimerValues[i] = newValue;
        }

        assert oneShotResets <= 1;
        final long newEntryDistance;

        if (timerResets == this.sortedTimers.size() || oneShotResets == 1) {
            // reset all timers -> back at entry config:
            newEntryDistance = 0;
        } else {
            newEntryDistance = this.entryDistance + delay;
        }

        return new State<>(location, sortedTimers, newTimerValues, initialValues, newEntryDistance, minimumTimerValue);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return this == o || o instanceof State<?, ?> that && this.minimumTimerValue == that.minimumTimerValue &&
                            entryDistance == that.entryDistance && Objects.equals(location, that.location) &&
                            Objects.equals(sortedTimers, that.sortedTimers) &&
                            Arrays.equals(timerValues, that.timerValues) &&
                            Arrays.equals(initialValues, that.initialValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location,
                            sortedTimers,
                            Arrays.hashCode(timerValues),
                            Arrays.hashCode(initialValues),
                            minimumTimerValue,
                            entryDistance);
    }

    @Override
    public String toString() {
        return String.format("[%s,%d]", this.location, this.entryDistance);
    }
}
