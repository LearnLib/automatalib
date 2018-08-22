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

import net.automatalib.automata.concepts.DetOutputAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.words.Word;

/**
 * A model checker that delegates every call to every method by default to methods of the model checker returned by
 * {@link #getModelChecker()}.
 *
 * @param <I> the input type.
 * @param <A> the automaton type.
 * @param <P> the property type.
 * @param <D> the output type.
 */
@ParametersAreNonnullByDefault
public interface ModelCheckerDelegate<I, A, P, D> extends ModelChecker<I, A, P, D> {

    ModelChecker<I, A, P, D> getModelChecker();

    @Nullable
    @Override
    default DetOutputAutomaton<?, I, ?, D> findCounterExample(A automaton, Collection<? extends I> inputs, P property)
            throws ModelCheckingException {

        return getModelChecker().findCounterExample(automaton, inputs, property);
    }

    interface DFAModelCheckerDelegate<I, P> extends ModelCheckerDelegate<I, DFA<?, I>, P, Boolean>, DFAModelChecker<I, P> {

        @Override
        DFAModelChecker<I, P> getModelChecker();

        @Nullable
        @Override
        default DFA<?, I> findCounterExample(DFA<?, I> automaton, Collection<? extends I> inputs, P property)
                throws ModelCheckingException {

            return getModelChecker().findCounterExample(automaton, inputs, property);
        }
    }

    interface MealyModelCheckerDelegate<I, O, P> extends ModelCheckerDelegate<I, MealyMachine<?, I, ?, O>, P, Word<O>>, MealyModelChecker<I, O, P> {

        @Override
        MealyModelChecker<I, O, P> getModelChecker();

        @Nullable
        @Override
        default MealyMachine<?, I, ?, O> findCounterExample(MealyMachine<?, I, ?, O> automaton,
                                                            Collection<? extends I> inputs,
                                                            P property) throws ModelCheckingException {
            return getModelChecker().findCounterExample(automaton, inputs, property);
        }

        @Override
        default Collection<? super O> getSkipOutputs() {
            return getModelChecker().getSkipOutputs();
        }

        @Override
        default void setSkipOutputs(Collection<? super O> skipOutputs) {
            getModelChecker().setSkipOutputs(skipOutputs);
        }
    }
}
