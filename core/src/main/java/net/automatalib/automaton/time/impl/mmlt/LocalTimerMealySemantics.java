package net.automatalib.automaton.time.impl.mmlt;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.alphabet.time.mmlt.*;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.automaton.time.mmlt.MealyTimerInfo;
import net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealyConfiguration;
import net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealySemanticTransition;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

/**
 * Defines the semantics of an MMLT.
 * <p>
 * The semantics of an MMLT are defined with an associated Mealy machine. The states of this machine are
 * LocalTimerMealyConfiguration objects. These represent tuples of an active location and the current timer values
 * of this location. The inputs of the machine are non-delaying inputs, discrete time steps, and the
 * symbolic input timeout, which causes a delay until the next timeout.
 * <p>
 * The outputs of this machine are the outputs of the MMLT, extended with a delay. This delay is zero for all
 * transitions, except for those with the input timeout.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output type of the MMLT
 */
public class LocalTimerMealySemantics<S, I, O> implements net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealySemantics<S, I, O> {
    private final LocalTimerMealyConfiguration<S, I, O> initialConfiguration;
    private final LocalTimerMealy<S, I, O> model;

    private final Alphabet<ILocalTimerMealySemanticInputSymbol<I>> alphabet;
    private final LocalTimerMealyOutputSymbol<O> silentOutput;

    public LocalTimerMealySemantics(LocalTimerMealy<S, I, O> model) {
        this.model = model;

        var initialLocation = model.getInitialState();
        this.initialConfiguration = new LocalTimerMealyConfiguration<>(initialLocation, model.getSortedTimers(initialLocation));

        this.alphabet = new GrowingMapAlphabet<>(model.getUntimedAlphabet());
        this.alphabet.add(new TimeoutSymbol<>());
        this.alphabet.add(new TimeStepSymbol<>());

        this.silentOutput = new LocalTimerMealyOutputSymbol<>(model.getSilentOutput());
    }


    @Override
    public Alphabet<ILocalTimerMealySemanticInputSymbol<I>> getInputAlphabet() {
        return alphabet;
    }


    @Override
    public LocalTimerMealyOutputSymbol<O> getSilentOutput() {
        return this.silentOutput;
    }


    @Override
    public LocalTimerMealyConfiguration<S, I, O> getInitialConfiguration() {
        return this.initialConfiguration;
    }


    @Override
    public Word<LocalTimerMealyOutputSymbol<O>> computeSuffixOutput(LocalTimerMealyConfiguration<S, I, O> configuration, Word<ILocalTimerMealySemanticInputSymbol<I>> suffix) {
        WordBuilder<LocalTimerMealyOutputSymbol<O>> wbOutput = new WordBuilder<>();

        var currentConfiguration = configuration;
        for (var sym : suffix) {
            var trans = getTransition(currentConfiguration, sym);
            currentConfiguration = trans.target();

            if (trans.output() == null) {
                throw new IllegalArgumentException("Cannot use time step sequences in suffix that have more than one symbol.");
            }
            wbOutput.append(trans.output());
        }

        return wbOutput.toWord();
    }


    @Override
    public Word<LocalTimerMealyOutputSymbol<O>> computeSuffixOutput(Word<ILocalTimerMealySemanticInputSymbol<I>> prefix, Word<ILocalTimerMealySemanticInputSymbol<I>> suffix) {
        var prefixConfig = this.traceInputs(prefix);
        return computeSuffixOutput(prefixConfig, suffix);
    }


    @Override
    public LocalTimerMealyConfiguration<S, I, O> traceInputs(Word<ILocalTimerMealySemanticInputSymbol<I>> prefix) {
        var currentConfiguration = getInitialConfiguration().copy();
        for (var sym : prefix) {
            currentConfiguration = getTransition(currentConfiguration, sym).target();
        }
        return currentConfiguration;
    }


    @Override
    public @NonNull LocalTimerMealySemanticTransition<S, I, O> getTransition(LocalTimerMealyConfiguration<S, I, O> source, ILocalTimerMealySemanticInputSymbol<I> input) {
        return getTransition(source, input, Long.MAX_VALUE);
    }


    @Override
    public @NonNull LocalTimerMealySemanticTransition<S, I, O> getTransition(LocalTimerMealyConfiguration<S, I, O> source,
                                                                             ILocalTimerMealySemanticInputSymbol<I> input,
                                                                             long maxWaitingTime) {
        var sourceCopy = source.copy(); // we do not want to modify values of the source configuration

        if (input instanceof NonDelayingInput<I> ndi) {
            return getTransition(sourceCopy, ndi);
        } else if (input instanceof TimeoutSymbol<I>) {
            return getTimeoutTransition(sourceCopy, maxWaitingTime);
        } else if (input instanceof TimeStepSequence<I> ts) {
            // Per step, we can advance at most by the time to the next timeout:
            var currentConfig = sourceCopy;
            LocalTimerMealyOutputSymbol<O> lastOutput = null;
            long remainingTime = ts.getTimeSteps();
            while (remainingTime > 0) {
                var nextTimeoutTrans = getTimeoutTransition(currentConfig, remainingTime);
                lastOutput = nextTimeoutTrans.output();
                if (nextTimeoutTrans.output().equals(this.getSilentOutput())) {
                    // No timer will expire during remaining waiting time:
                    break;
                } else {
                    remainingTime -= nextTimeoutTrans.output().getDelay();
                    currentConfig = nextTimeoutTrans.target();
                }
            }

            if (ts.getTimeSteps() > 1) {
                lastOutput = null; // ignore multiple outputs
            } else {
                // Output for single time step includes no delay by definition:
                lastOutput = new LocalTimerMealyOutputSymbol<>(lastOutput.getSymbol());
            }

            // Return final target + output:
            return new LocalTimerMealySemanticTransition<>(lastOutput, currentConfig);
        } else {
            throw new IllegalArgumentException("Unknown input symbol type");
        }
    }

    private LocalTimerMealySemanticTransition<S, I, O> getTimeoutTransition(LocalTimerMealyConfiguration<S, I, O> source, long maxWaitingTime) {
        LocalTimerMealyConfiguration<S, I, O> target;
        LocalTimerMealyOutputSymbol<O> output;

        var nextTimeouts = source.getNextExpiringTimers();
        if (nextTimeouts == null) {
            // no timers:
            output = this.getSilentOutput();
            target = source;
        } else if (nextTimeouts.delay() > maxWaitingTime) {
            // timers, but too far away:
            target = source;
            target.decrement(maxWaitingTime);
            output = this.getSilentOutput();
        } else {
            if (nextTimeouts.allPeriodic()) {
                target = source;
                target.decrement(nextTimeouts.delay());
            } else {
                // query target + update configuration:
                assert nextTimeouts.timers().size() == 1;
                TimerTimeoutSymbol<I> expiringTimerSym = new TimerTimeoutSymbol<>(nextTimeouts.timers().get(0).name());

                var successor = model.getSuccessor(source.getLocation(), expiringTimerSym);
                target = new LocalTimerMealyConfiguration<>(successor, model.getSortedTimers(successor));
                target.resetTimers();
            }

            // Create combined output:
            if (nextTimeouts.timers().size() == 1) {
                output = new LocalTimerMealyOutputSymbol<>(nextTimeouts.delay(), nextTimeouts.timers().get(0).output());
            } else {
                List<O> outputs = nextTimeouts.timers().stream().map(MealyTimerInfo::output).toList();
                O combinedOutput = model.getOutputCombiner().combineSymbols(outputs);
                output = new LocalTimerMealyOutputSymbol<>(nextTimeouts.delay(), combinedOutput);
            }
        }

        return new LocalTimerMealySemanticTransition<>(output, target);
    }

    private LocalTimerMealySemanticTransition<S, I, O> getTransition(LocalTimerMealyConfiguration<S, I, O> source, NonDelayingInput<I> input) {
        LocalTimerMealyConfiguration<S, I, O> target;
        LocalTimerMealyOutputSymbol<O> output;

        var trans = model.getTransition(source.getLocation(), input);
        if (trans == null) { // silent self-loop
            target = source;
            output = this.getSilentOutput();
        } else {
            // Identify successor configuration:
            if (!trans.successor().equals(source.getLocation())) {
                // Change to a different location resets all timers in target:
                target = new LocalTimerMealyConfiguration<>(trans.successor(), model.getSortedTimers(trans.successor()));
                target.resetTimers();
            } else if (model.isLocalReset(source.getLocation(), input)) {
                target = source;
                target.resetTimers();
            } else {
                target = source;
            }
            output = new LocalTimerMealyOutputSymbol<>(trans.output());
        }

        // Return output:
        return new LocalTimerMealySemanticTransition<>(output, target);
    }
}
