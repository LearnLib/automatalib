package net.automatalib.symbol.time;

import net.automatalib.automaton.mmlt.MMLT;

/**
 * The timeout symbol of a timer currently used by {@link MMLT}s.
 *
 * @param timer
 *         the name of the timer
 * @param <I>
 *         input symbol type (of other timed symbols)
 */
public record TimerTimeoutSymbol<I>(String timer) implements SymbolicInput<I> {

    @Override
    public String toString() {
        return String.format("to[%s]", this.timer);
    }

}
