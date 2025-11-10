package net.automatalib.symbol.time;

import java.util.Objects;

/**
 * An input symbol that represents a direct action without any delay.
 *
 * @param symbol
 *         the symbolic action
 * @param <I>
 *         input symbol type
 */
public record InputSymbol<I>(I symbol) implements TimedInput<I>, SymbolicInput<I> {

    @Override
    public String toString() {
        return Objects.toString(symbol);
    }
}
