package net.automatalib.automaton.mmlt;

import java.util.Objects;

/**
 * Provides information about a timer that is stored in an MMLT.
 *
 * @param <O> Output symbol type
 */
public class MealyTimerInfo<O> {
    /**
     * Name of the timer
     */
    private final String name;

    /**
     * Initial value of the timer
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

    public MealyTimerInfo(String name, long initial, O output, boolean periodic) {
        if (initial <= 0) {
            throw new IllegalArgumentException("Timer values must be greater than zero.");
        }

        this.name = name;
        this.initial = initial;
        this.output = output;
        this.periodic = periodic;
    }

    public MealyTimerInfo(String name, long initial, O output) {
        this(name, initial, output, true);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MealyTimerInfo) obj;
        return Objects.equals(this.name, that.name) &&
                this.initial == that.initial &&
                Objects.equals(this.output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, initial, output);
    }

    @Override
    public String toString() {
        return String.format("%s=%d/%s", name, initial, output);
    }

}
