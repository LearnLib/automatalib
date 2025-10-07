package net.automatalib.alphabet.impl.time.mmlt;

import java.util.Objects;

/**
 * Symbolic input for the semantics automaton: causes a delay until the next timeout.
 */
public class TimeoutSymbol<U> implements ILocalTimerMealySemanticInputSymbol<U> {

    @Override
    public String toString() {
        return "timeout";
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.toString());
    }

}
