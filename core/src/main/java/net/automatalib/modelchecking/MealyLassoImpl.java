/* Copyright (C) 2013-2024 TU Dortmund University
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

import net.automatalib.api.automaton.concept.DetOutputAutomaton;
import net.automatalib.api.automaton.transducer.MealyMachine;
import net.automatalib.api.modelchecking.Lasso.MealyLasso;
import net.automatalib.api.word.Word;

/**
 * A MealyLasso is a lasso for {@link MealyMachine}s.
 *
 * @param <I>
 *         the input type
 * @param <O>
 *         the output type
 */
public class MealyLassoImpl<I, O> extends AbstractLasso<I, Word<O>> implements MealyLasso<I, O> {

    public MealyLassoImpl(DetOutputAutomaton<?, I, ?, Word<O>> automaton,
                          Collection<? extends I> inputs,
                          int unfoldTimes) {
        super(automaton, inputs, unfoldTimes);
    }

    @Override
    public O getTransitionOutput(Integer transition) {
        return getOutput().getSymbol(transition);
    }

    @Override
    public Integer getSuccessor(Integer transition) {
        return transition;
    }

    @Override
    @SuppressWarnings("nullness") // TODO XXX FIXME: Returning non-null values would currently break PropertyOracles in LearnLib. We should rethink a clean API here.
    public Word<O> computeOutput(Iterable<? extends I> input) {
        final Integer state = getState(input);
        return state != null && state.equals(getWord().length()) ? getOutput() : null;
    }
}
