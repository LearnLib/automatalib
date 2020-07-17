package net.automatalib.util.ts.modal;

public final class IllegalConjunctionException extends IllegalArgumentException {

    public IllegalConjunctionException() {}

    public IllegalConjunctionException(String s) {
        super(s);
    }

    public IllegalConjunctionException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalConjunctionException(Throwable cause) {
        super(cause);
    }
}
