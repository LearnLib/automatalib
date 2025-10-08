package net.automatalib.alphabet.time.mmlt;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * An non-delaying input for the structural and semantics automaton.
 *
 * @param <U>
 */
public class NonDelayingInput<U> implements ILocalTimerMealySemanticInputSymbol<U>, ILocalTimerMealyInputSymbol<U> {
    private final U symbol;

    public NonDelayingInput(@NonNull U input) {
        this.symbol = input;
    }

    public U getSymbol() {
        return this.symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NonDelayingInput<?> that = (NonDelayingInput<?>) o;
        return Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(symbol);
    }

    @Override
    public String toString() {
        return symbol.toString();
    }
}
