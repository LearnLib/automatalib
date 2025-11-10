package net.automatalib.symbol.time;

import java.util.function.Supplier;

import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * Markup-interface for timing-sensitive inputs currently used in {@link MMLTSemantics}s. Contains utility methods for
 * conveniently constructing instances of timed symbols.
 *
 * @param <I>
 *         input symbol type
 */
public sealed interface TimedInput<I> permits InputSymbol, TimeoutSymbol, TimeStepSequence {

    static <I> InputSymbol<I> input(I symbol) {
        return new InputSymbol<>(symbol);
    }

    @SafeVarargs
    static <I> Word<InputSymbol<I>> inputs(I... symbols) {
        var wb = new WordBuilder<InputSymbol<I>>(symbols.length);
        for (I symbol : symbols) {
            wb.add(new InputSymbol<>(symbol));
        }
        return wb.toWord();
    }

    static <I> TimeoutSymbol<I> timeout() {
        return new TimeoutSymbol<>();
    }

    static <I> Word<TimeoutSymbol<I>> timeouts(int i) {
        return generate(i, TimeoutSymbol::new);
    }

    static <I> TimeStepSequence<I> step() {
        return new TimeStepSequence<>(1);
    }

    static <I> TimeStepSequence<I> step(int i) {
        return new TimeStepSequence<>(i);
    }

    static <I> Word<TimeStepSequence<I>> steps(int i) {
        return generate(i, () -> new TimeStepSequence<>(1));
    }

    private static <T> Word<T> generate(int i, Supplier<T> supplier) {
        var wb = new WordBuilder<T>(i);
        for (int j = 0; j < i; j++) {
            wb.append(supplier.get());
        }

        return wb.toWord();
    }
}
