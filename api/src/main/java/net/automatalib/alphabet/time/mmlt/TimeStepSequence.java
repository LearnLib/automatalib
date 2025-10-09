package net.automatalib.alphabet.time.mmlt;

import java.util.Objects;

/**
 * Convenience type for aggregating multiple subsequent time steps.
 */
public class TimeStepSequence<U> implements LocalTimerMealySemanticInputSymbol<U> {

    private long timeSteps;

    public TimeStepSequence(long timeSteps) {
        this.timeSteps = timeSteps;

        if (timeSteps <= 0) {
            throw new IllegalArgumentException("Timeout must be larger than zero.");
        }
    }

    public void setTimeSteps(long timeSteps) {
        this.timeSteps = timeSteps;
        if (timeSteps <= 0) {
            throw new IllegalArgumentException("Timeout must be larger than zero.");
        }
    }

    public long getTimeSteps() {
        return timeSteps;
    }

    @Override
    public String toString() {
        return String.format("wait[%d]", this.timeSteps);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeStepSequence<?> that = (TimeStepSequence<?>) o;
        return timeSteps == that.timeSteps;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeSteps);
    }
}
