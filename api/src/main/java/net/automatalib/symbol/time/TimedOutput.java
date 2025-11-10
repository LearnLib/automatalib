package net.automatalib.symbol.time;

import java.util.Objects;

/**
 * Output that may occur with some or no delay.
 *
 * @param symbol
 *         the output symbol
 * @param delay
 *         the delay
 * @param <O>
 *         output symbol type
 */
public record TimedOutput<O>(O symbol, long delay) {

    public TimedOutput {
        if (delay < 0) {
            throw new IllegalArgumentException("Delay must not be negative.");
        }
    }

    public TimedOutput(O symbol) {
        this(symbol, 0);
    }

    public boolean isDelayed() {
        return this.delay > 0;
    }

    @Override
    public String toString() {
        if (this.isDelayed()) {
            return String.format("[%d]%s", this.delay, this.symbol);
        }
        return Objects.toString(this.symbol);
    }

}
