package net.automatalib.symbol.time;

/**
 * An input that represents multiple subsequent time steps.
 *
 * @param timeSteps
 *         the number of time steps this symbol should elapse
 * @param <I>
 *         input symbol type (of other timed symbols)
 */
public record TimeStepSequence<I>(long timeSteps) implements TimedInput<I> {

    public TimeStepSequence {
        if (timeSteps <= 0) {
            throw new IllegalArgumentException("Timeout must be larger than zero.");
        }
    }

    @Override
    public String toString() {
        return String.format("wait[%d]", this.timeSteps);
    }

}
