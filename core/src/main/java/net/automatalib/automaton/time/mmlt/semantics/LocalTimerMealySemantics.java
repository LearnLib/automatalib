package net.automatalib.automaton.time.mmlt.semantics;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.alphabet.impl.time.mmlt.*;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.automaton.time.mmlt.MealyTimerInfo;
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
public class LocalTimerMealySemantics<S, I, O> {
    private final LocalTimerMealyConfiguration<S, I, O> initialConfiguration;
    private final LocalTimerMealy<S, I, O> model;

    private final Alphabet<ILocalTimerMealySemanticInputSymbol<I>> alphabet;
    private final LocalTimerMealyOutputSymbol<O> silentOutput;

    /**
     * Represents a transition in the semantics automaton ("expanded form") of an MMLT.
     *
     * @param output Transition output
     * @param target Transition target
     * @param <S>    Location type
     * @param <I>    Input type
     * @param <O>    Output type
     */
    public record LocalTimerMealySemanticTransition<S, I, O>(LocalTimerMealyOutputSymbol<O> output,
                                                             LocalTimerMealyConfiguration<S, I, O> target) {

    }

    public LocalTimerMealySemantics(LocalTimerMealy<S, I, O> model) {
        this.model = model;

        var initialLocation = model.getInitialState();
        this.initialConfiguration = new LocalTimerMealyConfiguration<>(initialLocation, model.getSortedTimers(initialLocation));

        this.alphabet = new GrowingMapAlphabet<>(model.getUntimedAlphabet());
        this.alphabet.add(new TimeoutSymbol<>());
        this.alphabet.add(new TimeStepSymbol<>());

        this.silentOutput = new LocalTimerMealyOutputSymbol<>(model.getSilentOutput());
    }

    /**
     * Returns the input alphabet of the semantics automaton. This consists of all non-delaying inputs
     * of the associated MMLT, as well as the time step symbol and the symbolic timeout symbol.
     *
     * @return Input alphabet
     */
    public Alphabet<ILocalTimerMealySemanticInputSymbol<I>> getInputAlphabet() {
        return alphabet;
    }

    /**
     * Returns the symbol used for silent outputs.
     *
     * @return Silent output symbol
     */
    public LocalTimerMealyOutputSymbol<O> getSilentOutput() {
        return this.silentOutput;
    }

    /**
     * Returns the initial configuration of this MMLT. This is a tuple of the
     * initial location and its initial timer values.
     *
     * @return Initial configuration
     */
    public LocalTimerMealyConfiguration<S, I, O> getInitialConfiguration() {
        return this.initialConfiguration;
    }

    /**
     * Enters the suffix into the provided configuration and returns corresponding outputs.
     * <p>
     * Cannot provide TimeSequences with more than 1 symbol for the suffix, as this might trigger multiple timeouts
     * and thus lead to output sequences that are longer than the suffix.
     *
     * @param configuration Configuration
     * @param suffix        Suffix inputs
     * @return Outputs for the suffix
     */
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

    /**
     * Enters the prefix and suffix sequences into the automaton and returns the outputs that occur for the suffixes.
     *
     * @param prefix Configuration prefix
     * @param suffix Suffix inputs
     * @return Outputs for the suffix
     */
    public Word<LocalTimerMealyOutputSymbol<O>> computeSuffixOutput(Word<ILocalTimerMealySemanticInputSymbol<I>> prefix, Word<ILocalTimerMealySemanticInputSymbol<I>> suffix) {
        var prefixConfig = this.traceInputs(prefix);
        return computeSuffixOutput(prefixConfig, suffix);
    }

    /**
     * Traces the provided prefix and returns the reached configuration.
     *
     * @param prefix Configuration prefix
     * @return Reached configuration
     */
    public LocalTimerMealyConfiguration<S, I, O> traceInputs(Word<ILocalTimerMealySemanticInputSymbol<I>> prefix) {
        var currentConfiguration = getInitialConfiguration().copy();
        for (var sym : prefix) {
            currentConfiguration = getTransition(currentConfiguration, sym).target();
        }
        return currentConfiguration;
    }


    @NonNull
    public LocalTimerMealySemanticTransition<S, I, O> getTransition(LocalTimerMealyConfiguration<S, I, O> source, ILocalTimerMealySemanticInputSymbol<I> input) {
        return getTransition(source, input, Long.MAX_VALUE);
    }

    /**
     * Retrieves the transition in the semantics automaton that has the provided input and source configuration.
     * <p>
     * If the input is a sequence of time steps, the target of the transition is the configuration reached after
     * executing all time steps. If the sequence counts more than one step, the sequence might trigger multiple
     * timeouts. To avoid ambiguity, the transition output is set to null in this case.
     * If the sequence comprises a single time step only, the output is either that of a timeout or silence.
     *
     * @param source         Source configuration
     * @param input          Input symbol
     * @param maxWaitingTime Maximum time steps to wait for a timeout
     * @return Transition in semantics automaton
     */
    @NonNull
    public LocalTimerMealySemanticTransition<S, I, O> getTransition(LocalTimerMealyConfiguration<S, I, O> source,
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
