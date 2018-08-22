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
package net.automatalib.modelchecking;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.words.Word;

/**
 * @see ModelCheckerDelegate
 */
@ParametersAreNonnullByDefault
public interface ModelCheckerLassoDelegate<I, A, P, D> extends ModelCheckerDelegate<I, A, P, D>, ModelCheckerLasso<I, A, P, D> {

    @Override
    ModelCheckerLasso<I, A, P, D> getModelChecker();

    @Override
    default double getMultiplier() {
        return getModelChecker().getMultiplier();
    }

    @Override
    default void setMultiplier(double multiplier) throws IllegalArgumentException {
        getModelChecker().setMultiplier(multiplier);
    }

    @Override
    default int getMinimumUnfolds() {
        return getModelChecker().getMinimumUnfolds();
    }

    @Override
    default void setMinimumUnfolds(int minimumUnfolds) throws IllegalArgumentException {
        getModelChecker().setMinimumUnfolds(minimumUnfolds);
    }

    @Nullable
    @Override
    default Lasso<I, D> findCounterExample(A automaton, Collection<? extends I> inputs, P property)
            throws ModelCheckingException {
        return getModelChecker().findCounterExample(automaton, inputs, property);
    }

    @Override
    default int computeUnfolds(int size) {
        return getModelChecker().computeUnfolds(size);
    }

    interface DFAModelCheckerLassoDelegate<I, P> extends ModelCheckerLassoDelegate<I, DFA<?, I>, P, Boolean>,
            DFAModelCheckerLasso<I, P>, DFAModelCheckerDelegate<I, P> {

        @Override
        DFAModelCheckerLasso<I, P> getModelChecker();

        @Nullable
        @Override
        default Lasso.DFALasso<I> findCounterExample(DFA<?, I> automaton, Collection<? extends I> inputs, P property)
                throws ModelCheckingException {
            return getModelChecker().findCounterExample(automaton, inputs, property);
        }
    }

    interface MealyModelCheckerLassoDelegate<I, O, P>
            extends ModelCheckerLassoDelegate<I, MealyMachine<?, I, ?, O>, P, Word<O>>, MealyModelCheckerLasso<I, O, P>,
            MealyModelCheckerDelegate<I, O, P> {

        @Override
        MealyModelCheckerLasso<I, O, P> getModelChecker();

        @Nullable
        @Override
        default Lasso.MealyLasso<I, O> findCounterExample(MealyMachine<?, I, ?, O> automaton,
                                                          Collection<? extends I> inputs,
                                                          P property) throws ModelCheckingException {
            return getModelChecker().findCounterExample(automaton, inputs, property);
        }
    }
}
