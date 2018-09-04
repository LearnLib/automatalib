/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package net.automatalib.modelcheckers.ltsmin.monitor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.modelcheckers.ltsmin.AbstractLTSmin;
import net.automatalib.modelcheckers.ltsmin.LTSminMealy;
import net.automatalib.modelcheckers.ltsmin.ltl.AbstractLTSminLTL;
import net.automatalib.serialization.fsm.parser.FSMParseException;
import net.automatalib.words.Word;

/**
 * An monitor model checker using LTSmin for Mealy machines.
 *
 * @param <I>
 *         the input type.
 * @param <O>
 *         the output type.
 *
 * @author Jeroen Meijer
 */
public abstract class AbstractLTSminMonitorMealy<I, O>
        extends AbstractLTSminMonitor<I, MealyMachine<?, I, ?, O>, MealyMachine<?, I, ?, O>>
        implements LTSminMealy<I, O, MealyMachine<?, I, ?, O>> {

    /**
     * @see #getString2Output()
     */
    private final Function<String, O> string2Output;

    /**
     * @see #getSkipOutputs()
     * @see #setSkipOutputs(Collection)
     */
    private Collection<? super O> skipOutputs;

    /**
     * Constructs a new AbstractLTSminLTLMealy.
     *
     * @param string2Output
     *         the function that transforms edges in the FSM file to actual output.
     * @param skipOutputs
     *         the set of outputs that need to be skipped while writing the Mealy machine to ETF.
     *
     * @see AbstractLTSminLTL
     */
    protected AbstractLTSminMonitorMealy(boolean keepFiles,
                                         Function<String, I> string2Input,
                                         Function<String, O> string2Output,
                                         Collection<? super O> skipOutputs) {
        super(keepFiles, string2Input);
        this.string2Output = string2Output;
        this.skipOutputs = skipOutputs == null ? Collections.emptyList() : skipOutputs;
    }

    /**
     * Gets a function that transforms edges in the FSM file to actual output.
     *
     * @return the Function.
     */
    @Override
    public Function<String, O> getString2Output() {
        return string2Output;
    }

    /**
     * Gets a set of outputs that need to be skipped while writing the Mealy machine to ETF.
     *
     * @return the Colleciton.
     */
    @Override
    public Collection<? super O> getSkipOutputs() {
        return skipOutputs;
    }

    /**
     * Sets a set of outputs that need to be skipped while writing the Mealy machine to ETF.
     */
    @Override
    public void setSkipOutputs(Collection<? super O> skipOutputs) {
        this.skipOutputs = skipOutputs;
    }

    /**
     * Converts the FSM file to a {@link MealyMachine}.
     *
     * @see AbstractLTSmin#findCounterExample(Object, Collection, Object)
     */
    @Nullable
    @Override
    public MealyMachine<?, I, ?, O> findCounterExample(MealyMachine<?, I, ?, O> automaton,
                                                       Collection<? extends I> inputs, String property)
            throws ModelCheckingException {
        final File fsm = findCounterExampleFSM(automaton, inputs, property);

        try {
            final CompactMealy<I, O> result = fsm != null ? fsm2Mealy(fsm, automaton) : null;

            // check if we must keep the FSM
            if (!isKeepFiles() && fsm != null && !fsm.delete()) {
                throw new ModelCheckingException("Could not delete file: " + fsm.getAbsolutePath());
            }

            return result == null ? null : new MealyMachine<Integer, I, CompactMealyTransition<O>, O>() {

                private final Integer deadlock = result.getStates().stream().filter(
                        s -> result.getInputAlphabet().stream().allMatch(
                                i -> result.getSuccessor(s, i) == null)).findFirst().orElseThrow(
                        () -> new ModelCheckingException("No deadlock found"));

                @Override
                public Word<O> computeOutput(Iterable<? extends I> input) {
                    final Integer state = getState(input);

                    return state != null && state.equals(deadlock) ? MealyMachine.super.computeOutput(input) : null;
                }

                @Nullable
                @Override
                public Integer getInitialState() {
                    return result.getInitialState();
                }

                @Nonnull
                @Override
                public Integer getSuccessor(CompactMealyTransition<O> transition) {
                    return result.getSuccessor(transition);
                }

                @Nullable
                @Override
                public CompactMealyTransition<O> getTransition(Integer state, @Nullable I input) {
                    return result.getTransition(state, input);
                }

                @Nullable
                @Override
                public O getTransitionOutput(CompactMealyTransition<O> transition) {
                    return result.getTransitionOutput(transition);
                }

                @Nonnull
                @Override
                public Collection<Integer> getStates() {
                    return result.getStates();
                }
            };
        } catch (IOException | FSMParseException e) {
            throw new ModelCheckingException(e);
        }
    }
}
