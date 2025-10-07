package net.automatalib.alphabet.impl.time.mmlt;

/**
 * Input for the semantics automaton: delay for a single time step.
 */
public class TimeStepSymbol<U> extends TimeStepSequence<U> {

    public TimeStepSymbol() {
        super(1);
    }

}
