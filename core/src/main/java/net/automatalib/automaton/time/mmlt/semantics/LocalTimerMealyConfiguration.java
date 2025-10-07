package net.automatalib.automaton.time.mmlt.semantics;

import net.automatalib.alphabet.impl.time.mmlt.ILocalTimerMealySemanticInputSymbol;
import net.automatalib.automaton.time.mmlt.MealyTimerInfo;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

/**
 * A configuration, a.k.a., state of an MMLT.
 * A configuration is a tuple of an active location and the values of its timers.
 *
 * @param <S> Location type
 * @param <O> Output symbol type
 */
public final class LocalTimerMealyConfiguration<S, I, O> {
    private final S location;

    private final List<MealyTimerInfo<O>> sortedTimers;
    private final long[] timerValues;
    private final long[] initialValues;
    private final long minimumTimerValue;

    private long entryDistance;

    /**
     * Initializes the entry configuration for the provided location, where all timers have their initial value.
     *
     * @param location     Location
     * @param sortedTimers Timers of the location, sorted by initial value.
     */
    public LocalTimerMealyConfiguration(S location, List<MealyTimerInfo<O>> sortedTimers) {
        this(location, sortedTimers, null);
    }

    public LocalTimerMealyConfiguration(S location, List<MealyTimerInfo<O>> sortedTimers, Word<ILocalTimerMealySemanticInputSymbol<I>> locationPrefix) {
        this.location = location;

        this.sortedTimers = sortedTimers;

        this.initialValues = new long[sortedTimers.size()];
        this.timerValues = new long[sortedTimers.size()];
        this.minimumTimerValue = (sortedTimers.isEmpty()) ? 0 : sortedTimers.get(0).initial();

        for (int i = 0; i < sortedTimers.size(); i++) {
            initialValues[i] = sortedTimers.get(i).initial();
            timerValues[i] = initialValues[i]; // reset
        }

        this.entryDistance = 0;
    }


    private LocalTimerMealyConfiguration(S location, List<MealyTimerInfo<O>> sortedTimers,
                                         long[] timerValues, long[] initialValues, long entryDistance, long minimumTimerValue) {
        this.location = location;
        this.sortedTimers = sortedTimers;

        this.initialValues = initialValues;
        this.minimumTimerValue = minimumTimerValue;
        this.timerValues = Arrays.copyOf(timerValues, timerValues.length);
        this.entryDistance = entryDistance;
    }

    /**
     * Creates a copy of this configuration.
     * The location, timers, prefix, and initialValue still point to the original instances.
     * The current timer values are copied.
     * Modifying these in the resulting object does not affect the original configuration.
     */
    public LocalTimerMealyConfiguration<S, I, O> copy() {
        return new LocalTimerMealyConfiguration<>(location, sortedTimers, timerValues, initialValues, entryDistance, minimumTimerValue);
    }

    public S getLocation() {
        return location;
    }

    /**
     * Returns the entry distance. This is the minimal number of time steps
     * required to reach this configuration from the entry configuration.
     *
     * @return Entry distance
     */
    public long getEntryDistance() {
        return entryDistance;
    }

    /**
     * Indicates if this is the entry configuration of the location.
     * A configuration is the entry configuration if all timers have their initial value.
     *
     * @return True if entry configuration.
     */
    public boolean isEntryConfig() {
        return this.entryDistance == 0;
    }

    /**
     * Indicates if this configuration is stable.
     * A configuration is stable if its entry distance is less than the initial value
     * of the timer with the lowest initial value of the location.
     * If the location has no timers, its only configuration is its entry configuration, which is always stable.
     *
     * @return True if stable
     */
    public boolean isStableConfig() {
        return this.entryDistance == 0 || this.entryDistance < minimumTimerValue;
    }

    /**
     * Resets all timers to their initial values.
     */
    public void resetTimers() {
        System.arraycopy(this.initialValues, 0, this.timerValues, 0, sortedTimers.size());
        this.entryDistance = 0;
    }


    /**
     * Returns all timers that time out in the least number of time steps.
     */
    @Nullable
    public TimeoutPair<O> getNextExpiringTimers() {
        if (sortedTimers.isEmpty()) {
            return null;
        } else if (this.sortedTimers.size() == 1) {
            // No need to collect timeouts - there is only one timer that can expire.
            // The time to its timeout is its remaining value:
            return new TimeoutPair<>(this.timerValues[0],
                    Collections.singletonList(this.sortedTimers.get(0)));

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
            List<MealyTimerInfo<O>> expiringTimers = new ArrayList<>();
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
     * Decreases all timer values by the specified amount. This amount must be at most the time to the next timeout.
     * If this sets a timer to zero, this timer is immediately reset.
     * to its initial value.
     *
     * @param delay Decrement
     */
    public void decrement(long delay) {
        int timerResets = 0;
        int oneShotResets = 0;
        for (int i = 0; i < this.sortedTimers.size(); i++) {
            long newValue = this.timerValues[i] - delay;

            if (newValue < 0) {
                throw new IllegalArgumentException("Can only advance to next timeout.");
            } else if (newValue == 0) {
                if (!sortedTimers.get(i).periodic()) {
                    oneShotResets += 1;
                }

                newValue = this.initialValues[i];
                timerResets += 1;
            }
            this.timerValues[i] = newValue;
        }

        if (oneShotResets > 1) throw new AssertionError();
        if (timerResets == this.sortedTimers.size() || oneShotResets == 1) {
            // reset all timers -> back at entry config:
            this.entryDistance = 0;
        } else {
            this.entryDistance += delay;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LocalTimerMealyConfiguration<?, ?, ?> that = (LocalTimerMealyConfiguration<?, ?, ?>) o;
        return minimumTimerValue == that.minimumTimerValue && entryDistance == that.entryDistance && Objects.equals(location, that.location) && Objects.equals(sortedTimers, that.sortedTimers) && Objects.deepEquals(timerValues, that.timerValues) && Objects.deepEquals(initialValues, that.initialValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, sortedTimers, Arrays.hashCode(timerValues), Arrays.hashCode(initialValues), minimumTimerValue, entryDistance);
    }

    @Override
    public String toString() {
        return String.format("[%s,%d]", this.location, this.entryDistance);
    }
}
