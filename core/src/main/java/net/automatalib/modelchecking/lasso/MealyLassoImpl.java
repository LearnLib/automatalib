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
package net.automatalib.modelchecking.lasso;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.automatalib.automata.concepts.DetOutputAutomaton;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.words.Word;

/**
 * A MealyLasso is a lasso for {@link MealyMachine}s.
 *
 * @param <I>
 *         the input type
 * @param <O>
 *         the output type
 *
 * @author Jeroen Meijer
 */
public class MealyLassoImpl<I, O> extends AbstractLasso<I, Word<O>> implements MealyLasso<I, O> {

    public MealyLassoImpl(DetOutputAutomaton<?, I, ?, Word<O>> automaton,
                              Collection<? extends I> inputs,
                              int unfoldTimes) {
        super(automaton, inputs, unfoldTimes);
    }

    @Nullable
    @Override
    public O getTransitionOutput(Integer transition) {
        return getOutput().getSymbol(transition);
    }

    @Nonnull
    @Override
    public Integer getSuccessor(Integer transition) {
        return transition;
    }

    @Override
    public Word<O> computeOutput(Iterable<? extends I> input) {
        final Integer state = getState(input);
        return state != null && state.equals(getWord().length()) ? getOutput() : null;
    }
}
