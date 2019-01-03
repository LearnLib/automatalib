/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.modelcheckers.ltsmin.ltl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.modelcheckers.ltsmin.LTSminMealy;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.modelchecking.ModelCheckerLasso.MealyModelCheckerLasso;
import net.automatalib.modelchecking.lasso.MealyLassoImpl;
import net.automatalib.serialization.fsm.parser.FSMParseException;

/**
 * An LTL model checker using LTSmin for Mealy machines.
 *
 * @param <I>
 *         the input type.
 * @param <O>
 *         the output type.
 *
 * @author Jeroen Meijer
 */
public abstract class AbstractLTSminLTLMealy<I, O>
        extends AbstractLTSminLTL<I, MealyMachine<?, I, ?, O>, MealyLasso<I, O>>
        implements MealyModelCheckerLasso<I, O, String>, LTSminMealy<I, O, MealyLasso<I, O>> {

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
    protected AbstractLTSminLTLMealy(boolean keepFiles,
                                     Function<String, I> string2Input,
                                     Function<String, O> string2Output,
                                     int minimumUnfolds,
                                     double multiplier,
                                     Collection<? super O> skipOutputs) {
        super(keepFiles, string2Input, minimumUnfolds, multiplier);
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

    @Nullable
    @Override
    public MealyLasso<I, O> findCounterExample(MealyMachine<?, I, ?, O> automaton, Collection<? extends I> inputs, String property)
            throws ModelCheckingException {
        final File fsm = findCounterExampleFSM(automaton, inputs, property);

        final MealyLasso<I, O> result;

        if (fsm != null) {
            final CompactMealy<I, O> mealy;

            try {
                mealy = fsm2Mealy(fsm, automaton, inputs);

                // check if we must keep the FSM
                if (!isKeepFiles() && !fsm.delete()) {
                    throw new ModelCheckingException("Could not delete file: " + fsm.getAbsolutePath());
                }
            } catch (IOException | FSMParseException e) {
                throw new ModelCheckingException(e);
            }

            result = new MealyLassoImpl<>(mealy, mealy.getInputAlphabet(), computeUnfolds(automaton.size()));
        } else {
            result = null;
        }

        return result;
    }
}
