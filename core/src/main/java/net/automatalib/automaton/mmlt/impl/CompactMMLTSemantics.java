package net.automatalib.automaton.mmlt.impl;

import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.mmlt.MealyTimerInfo;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.automaton.transducer.impl.MealyTransition;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines the semantics of an MMLT.
 * <p>
 * The semantics of an MMLT are defined with an associated Mealy machine. The states of this machine are
 * LocalTimerMealyConfiguration objects. These represent tuples of an active location and the current timer values of
 * this location. The inputs of the machine are non-delaying inputs, discrete time steps, and the symbolic input
 * timeout, which causes a delay until the next timeout.
 * <p>
 * The outputs of this machine are the outputs of the MMLT, extended with a delay. This delay is zero for all
 * transitions, except for those with the input timeout.
 *
 * @param <S>
 *         Location type
 * @param <I>
 *         Input type for non-delaying inputs
 * @param <O>
 *         Output type of the MMLT
 */
public class CompactMMLTSemantics<S, I, T, O>
        implements MMLTSemantics<S, I, MealyTransition<State<S, O>, TimedOutput<O>>, O> {

    private final State<S, O> initialConfiguration;
    private final MMLT<S, I, T, O> model;

    private final Alphabet<TimedInput<I>> alphabet;
    private final TimedOutput<O> silentOutput;

    public CompactMMLTSemantics(MMLT<S, I, T, O> model) {
        this.model = model;

        var initialLocation = model.getInitialState();
        this.initialConfiguration = new State<>(initialLocation, model.getSortedTimers(initialLocation));

        this.alphabet = new GrowingMapAlphabet<>(model.getInputAlphabet().stream().map(TimedInput::input).toList());
        this.alphabet.add(TimedInput.timeout());
        this.alphabet.add(TimedInput.step());

        this.silentOutput = new TimedOutput<>(model.getSilentOutput());
    }

    @Override
    public Alphabet<TimedInput<I>> getInputAlphabet() {
        return alphabet;
    }

    @Override
    public TimedOutput<O> getSilentOutput() {
        return this.silentOutput;
    }

    @Override
    public State<S, O> getInitialState() {
        return this.initialConfiguration;
    }

    @Override
    public Word<TimedOutput<O>> computeSuffixOutput(Iterable<? extends TimedInput<I>> prefix,
                                                    Iterable<? extends TimedInput<I>> suffix) {
        WordBuilder<TimedOutput<O>> wbOutput = Output.getBuilderFor(suffix);
        var currentConfiguration = getState(prefix);
        for (var sym : suffix) {
            var trans = getTransition(currentConfiguration, sym);
            currentConfiguration = trans.getSuccessor();

            if (trans.getOutput() == null) {
                throw new IllegalArgumentException(
                        "Cannot use time step sequences in suffix that have more than one symbol.");
            }
            wbOutput.append(trans.getOutput());
        }

        return wbOutput.toWord();
    }

    @Override
    public @NonNull MealyTransition<State<S, O>, TimedOutput<O>> getTransition(State<S, O> source, TimedInput<I> input) {
        return getTransition(source, input, Long.MAX_VALUE);
    }

    @Override
    public @NonNull MealyTransition<State<S, O>, TimedOutput<O>> getTransition(State<S, O> source,
                                                                             TimedInput<I> input,
                                                                             long maxWaitingTime) {
        var sourceCopy = source.copy(); // we do not want to modify values of the source configuration

        if (input instanceof InputSymbol<I> ndi) {
            return getTransition(sourceCopy, ndi);
        } else if (input instanceof TimeoutSymbol<I>) {
            return getTimeoutTransition(sourceCopy, maxWaitingTime);
        } else if (input instanceof TimeStepSequence<I> ts) {
            // Per step, we can advance at most by the time to the next timeout:
            var currentConfig = sourceCopy;
            TimedOutput<O> lastOutput = null;
            long remainingTime = ts.timeSteps();
            while (remainingTime > 0) {
                var nextTimeoutTrans = getTimeoutTransition(currentConfig, remainingTime);
                lastOutput = nextTimeoutTrans.getOutput();
                if (nextTimeoutTrans.getOutput().equals(this.getSilentOutput())) {
                    // No timer will expire during remaining waiting time:
                    break;
                } else {
                    remainingTime -= nextTimeoutTrans.getOutput().delay();
                    currentConfig = nextTimeoutTrans.getSuccessor();
                }
            }

            if (ts.timeSteps() > 1) {
                lastOutput = null; // ignore multiple outputs
            } else {
                // Output for single time step includes no delay by definition:
                lastOutput = new TimedOutput<>(lastOutput.symbol());
            }

            // Return final target + output:
            return new MealyTransition<>(currentConfig, lastOutput);
        } else {
            throw new IllegalArgumentException("Unknown input symbol type");
        }
    }

    @Override
    public TimedOutput<O> getTransitionOutput(MealyTransition<State<S, O>, TimedOutput<O>> transition) {
        return transition.getOutput();
    }

    @Override
    public State<S, O> getSuccessor(MealyTransition<State<S, O>, TimedOutput<O>> transition) {
        return transition.getSuccessor();
    }

    private MealyTransition<State<S, O>, TimedOutput<O>> getTimeoutTransition(State<S, O> source, long maxWaitingTime) {
        State<S, O> target;
        TimedOutput<O> output;

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
                var timer = nextTimeouts.timers().get(0);
                var successor = timer.target();

                target = new State<>(successor, model.getSortedTimers(successor));
                target.resetTimers();
            }

            // Create combined output:
            if (nextTimeouts.timers().size() == 1) {
                output = new TimedOutput<>(nextTimeouts.timers().get(0).output(), nextTimeouts.delay());
            } else {
                List<O> outputs = nextTimeouts.timers().stream().map(MealyTimerInfo::output).toList();
                O combinedOutput = model.getOutputCombiner().combineSymbols(outputs);
                output = new TimedOutput<>(combinedOutput, nextTimeouts.delay());
            }
        }

        return new MealyTransition<>(target, output);
    }

    private MealyTransition<State<S, O>, TimedOutput<O>> getTransition(State<S, O> source, InputSymbol<I> input) {
        State<S, O> target;
        TimedOutput<O> output;

        var trans = model.getTransition(source.getLocation(), input.symbol());
        if (trans == null) { // silent self-loop
            target = source;
            output = this.getSilentOutput();
        } else {
            // Identify successor configuration:
            S succ = model.getSuccessor(trans);
            if (!succ.equals(source.getLocation())) {
                // Change to a different location resets all timers in target:
                target = new State<>(succ, model.getSortedTimers(succ));
                target.resetTimers();
            } else if (model.isLocalReset(source.getLocation(), input.symbol())) {
                target = source;
                target.resetTimers();
            } else {
                target = source;
            }
            output = new TimedOutput<>(model.getTransitionProperty(trans));
        }

        // Return output:
        return new MealyTransition<>(target, output);
    }
}
