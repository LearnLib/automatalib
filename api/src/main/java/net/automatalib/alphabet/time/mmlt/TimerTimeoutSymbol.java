package net.automatalib.alphabet.time.mmlt;

import java.util.Objects;

/**
 * The timeout symbol of a timer, as used by the structural automaton.
 * <p>
 * This input is used for the transition and output function of some MMLT.
 * These symbols are not inputs for the expanded form of an MMLT.
 *
 * @param <U>
 */
public class TimerTimeoutSymbol<U> implements ILocalTimerMealyInputSymbol<U> {
    private final String timer;

    public TimerTimeoutSymbol(String timer) {
        this.timer = timer;
    }

    public String getTimer() {
        return timer;
    }

    @Override
    public String toString() {
        return String.format("to[%s]", this.timer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimerTimeoutSymbol<?> that = (TimerTimeoutSymbol<?>) o;
        return Objects.equals(timer, that.timer);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(timer);
    }
}
