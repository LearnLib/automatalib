package net.automatalib.symbol.time;

/**
 * An input that causes a delay until the next timeout.
 *
 * @param <I>
 *         input symbol type (of other timed symbols)
 */
public record TimeoutSymbol<I>() implements TimedInput<I> {

    @Override
    public String toString() {
        return "timeout";
    }

}
