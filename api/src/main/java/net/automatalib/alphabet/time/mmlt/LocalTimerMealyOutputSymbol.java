package net.automatalib.alphabet.time.mmlt;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * Output type for the semantics automaton: an output that occurs with some or no delay.
 */
public class LocalTimerMealyOutputSymbol<U> {

    private final U userObject;
    private final long delay;

    public LocalTimerMealyOutputSymbol(long delay, @NonNull U userObject) {
        if (delay < 0) {
            throw new IllegalArgumentException("Delay must not be negative.");
        }
        this.userObject = userObject;
        this.delay = delay;
    }

    public LocalTimerMealyOutputSymbol(@NonNull U userObject) {
        this(0, userObject);
    }

    public long getDelay() {
        return delay;
    }

    public boolean isDelayed() {
        return this.delay > 0;
    }

    public U getSymbol() {
        return userObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalTimerMealyOutputSymbol<?> that = (LocalTimerMealyOutputSymbol<?>) o;
        return delay == that.delay && Objects.equals(userObject, that.userObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userObject, delay);
    }

    @Override
    public String toString() {
        if (this.isDelayed()) {
            return String.format("[%d]%s", this.getDelay(), this.userObject);
        }
        return this.userObject.toString();
    }

}
