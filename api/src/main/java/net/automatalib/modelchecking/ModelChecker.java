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

/**
 * A ModelChecker checks whether a given hypothesis satisfies a given property. If the property can not be satisfied it
 * provides a counterexample.
 *
 * @param <I>
 *         the input type
 * @param <A>
 *         the automaton type
 * @param <P>
 *         the property type
 * @param <R>
 *         the type of a counterexample
 *
 * @author Jeroen Meijer
 */
@ParametersAreNonnullByDefault
public interface ModelChecker<I, A, P, R> {

    /**
     * Try to find a counterexample for the given {@code property} and {@code hypothesis}.
     *
     * @param hypothesis
     *         the automaton to check the property on.
     * @param inputs
     *         the alphabet.
     * @param property
     *         the property.
     *
     * @return the counterexample, or {@code null} if a counterexample does not exist.
     *
     * @throws ModelCheckingException
     *         when a model checker can not check the property.
     */
    @Nullable
    R findCounterExample(A hypothesis, Collection<? extends I> inputs, P property) throws ModelCheckingException;

    interface DFAModelChecker<I, P, R extends DFA<?, I>> extends ModelChecker<I, DFA<?, I>, P, R> {}

    interface MealyModelChecker<I, O, P, R extends MealyMachine<?, I, ?, O>>
            extends ModelChecker<I, MealyMachine<?, I, ?, O>, P, R> {}
}
