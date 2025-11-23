/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.automaton.mmlt.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.MapAlphabet;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.automaton.mmlt.TimeoutPair;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.automaton.transducer.impl.MealyTransition;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Default implementation for a {@link MMLTSemantics} that wraps arbitrary {@link MMLT}s.
 *
 * @param <S>
 *         location type of the original MMLT
 * @param <I>
 *         input symbol of the original MMLT
 * @param <T>
 *         transition type of the original MMLT
 * @param <O>
 *         output symbol type of the original MMLT
 */
public class DefaultMMLTSemantics<S, I, T, O>
        implements MMLTSemantics<S, I, MealyTransition<State<S, O>, @Nullable TimedOutput<O>>, O> {

    private final MMLT<S, I, T, O> model;
    private final @Nullable State<S, O> initialConfiguration;
    private final Alphabet<TimedInput<I>> alphabet;
    private final TimedOutput<O> silentOutput;

    public DefaultMMLTSemantics(MMLT<S, I, T, O> model) {
        this.model = model;

        final S initialLocation = model.getInitialState();
        if (initialLocation == null) {
            this.initialConfiguration = null;
        } else {
            this.initialConfiguration = new State<>(initialLocation, model.getSortedTimers(initialLocation));
        }

        final Alphabet<I> inputs = model.getInputAlphabet();
        final List<TimedInput<I>> timedInputs = new ArrayList<>(inputs.size() + 2);
        for (I i : inputs) {
            timedInputs.add(TimedInput.input(i));
        }
        timedInputs.add(TimedInput.timeout());
        timedInputs.add(TimedInput.step());

        this.alphabet = new MapAlphabet<>(timedInputs);
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
    public @Nullable State<S, O> getInitialState() {
        return this.initialConfiguration;
    }

    @Override
    public Word<TimedOutput<O>> computeSuffixOutput(Iterable<? extends TimedInput<I>> prefix,
                                                    Iterable<? extends TimedInput<I>> suffix) {
        WordBuilder<TimedOutput<O>> wb = Output.getBuilderFor(suffix);
        State<S, O> currentConfiguration = getState(prefix);

        if (currentConfiguration == null) {
            return Word.epsilon();
        }

        for (TimedInput<I> sym : suffix) {
            MealyTransition<State<S, O>, @Nullable TimedOutput<O>> trans = getTransition(currentConfiguration, sym);

            if (trans == null) {
                break;
            }

            final TimedOutput<O> output = trans.getOutput();
            if (output == null) {
                throw new IllegalArgumentException(
                        "Cannot use time step sequences in suffix that have more than one symbol.");
            }

            wb.append(output);
            currentConfiguration = trans.getSuccessor();
        }

        return wb.toWord();
    }

    @Override
    public MealyTransition<State<S, O>, @Nullable TimedOutput<O>> getTransition(State<S, O> source, TimedInput<I> input) {
        return getTransition(source, input, Long.MAX_VALUE);
    }

    @Override
    @SuppressWarnings("PMD.UnnecessaryCast") // casts currently necessary for checkerframework
    public MealyTransition<State<S, O>, @Nullable TimedOutput<O>> getTransition(State<S, O> source,
                                                                      TimedInput<I> input,
                                                                      long maxWaitingTime) {
        if (input instanceof InputSymbol<I> ndi) {
            return (MealyTransition<State<S, O>, @Nullable TimedOutput<O>>) getTransition(source, ndi);
        } else if (input instanceof TimeoutSymbol<I>) {
            return (MealyTransition<State<S, O>, @Nullable TimedOutput<O>>) getTimeoutTransition(source, maxWaitingTime);
        } else if (input instanceof TimeStepSequence<I> ts) {
            // Per step, we can advance at most by the time to the next timeout:
            State<S, O> currentConfig = source;
            TimedOutput<O> lastOutput = null;
            long remainingTime = ts.timeSteps();
            while (remainingTime > 0) {
                MealyTransition<State<S, O>, TimedOutput<O>> nextTimeoutTrans =
                        getTimeoutTransition(currentConfig, remainingTime);
                currentConfig = nextTimeoutTrans.getSuccessor();
                lastOutput = nextTimeoutTrans.getOutput();
                if (Objects.equals(lastOutput, this.getSilentOutput())) {
                    // No timer will expire during remaining waiting time:
                    break;
                } else {
                    remainingTime -= nextTimeoutTrans.getOutput().delay();
                }
            }

            if (ts.timeSteps() > 1) {
                lastOutput = null; // ignore multiple outputs
            } else {
                // Output for single time step includes no delay by definition:
                assert lastOutput != null;
                lastOutput = new TimedOutput<>(lastOutput.symbol());
            }

            // Return final target + output:
            return new MealyTransition<>(currentConfig, lastOutput);
        } else {
            throw new IllegalArgumentException("Unknown input symbol type");
        }
    }

    private MealyTransition<State<S, O>, TimedOutput<O>> getTransition(State<S, O> source, InputSymbol<I> input) {
        State<S, O> target;
        TimedOutput<O> output;

        T trans = model.getTransition(source.getLocation(), input.symbol());
        if (trans == null) { // silent self-loop
            target = source;
            output = this.getSilentOutput();
        } else {
            // Identify successor configuration:
            S succ = model.getSuccessor(trans);
            if (!Objects.equals(succ, source.getLocation())) {
                // Change to a different location resets all timers in target:
                target = new State<>(succ, model.getSortedTimers(succ));
            } else if (model.isLocalReset(source.getLocation(), input.symbol())) {
                target = source.resetTimers();
            } else {
                target = source;
            }
            output = new TimedOutput<>(model.getTransitionProperty(trans));
        }

        // Return output:
        return new MealyTransition<>(target, output);
    }

    @Override
    public TimedOutput<O> getTransitionOutput(MealyTransition<State<S, O>, @Nullable TimedOutput<O>> transition) {
        TimedOutput<O> output = transition.getOutput();
        if (output == null) {
            throw new IllegalArgumentException("transition has no non-null output");
        }
        return output;
    }

    @Override
    public State<S, O> getSuccessor(MealyTransition<State<S, O>, @Nullable TimedOutput<O>> transition) {
        return transition.getSuccessor();
    }

    private MealyTransition<State<S, O>, TimedOutput<O>> getTimeoutTransition(State<S, O> source, long maxWaitingTime) {
        State<S, O> target;
        TimedOutput<O> output;

        TimeoutPair<S, O> nextTimeouts = source.getNextExpiringTimers();
        if (nextTimeouts == null) {
            // no timers:
            output = this.getSilentOutput();
            target = source;
        } else if (nextTimeouts.delay() > maxWaitingTime) {
            // timers, but too far away:
            target = source.decrement(maxWaitingTime);
            output = this.getSilentOutput();
        } else {
            if (nextTimeouts.allPeriodic()) {
                target = source.decrement(nextTimeouts.delay());
            } else {
                // query target + update configuration:
                assert nextTimeouts.timers().size() == 1;
                TimerInfo<S, O> timer = nextTimeouts.timers().get(0);
                S successor = timer.target();

                target = new State<>(successor, model.getSortedTimers(successor));
            }

            // Create combined output:
            if (nextTimeouts.timers().size() == 1) {
                output = new TimedOutput<>(nextTimeouts.timers().get(0).output(), nextTimeouts.delay());
            } else {
                List<O> outputs = nextTimeouts.timers().stream().map(TimerInfo::output).toList();
                O combinedOutput = model.getOutputCombiner().combineSymbols(outputs);
                output = new TimedOutput<>(combinedOutput, nextTimeouts.delay());
            }
        }

        return new MealyTransition<>(target, output);
    }
}
