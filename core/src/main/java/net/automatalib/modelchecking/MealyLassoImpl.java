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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.words.Word;

/**
 * A MealyLasso is a lasso for {@link MealyMachine}s.
 *
 * @param <S>
 *         the state type of the Mealy machine that contains the lasso.
 * @param <I>
 *         the input type
 * @param <O>
 *         the output type
 *
 * @author Jeroen Meijer
 */
public class MealyLassoImpl<S, I, O> extends AbstractLasso<S, MealyMachine<S, I, ?, O>, I, Word<O>>
        implements MealyMachine<Integer, I, CompactMealyTransition<O>, O>,
                   MealyLasso<S, I, CompactMealyTransition<O>, O> {

    public MealyLassoImpl(MealyMachine<S, I, ?, O> automaton, Collection<? extends I> inputs, int unfoldTimes) {
        super(automaton, inputs, unfoldTimes);
    }

    @Nullable
    @Override
    public O getTransitionOutput(CompactMealyTransition<O> transition) {
        return transition.getOutput();
    }

    /**
     * Returns the transition from a given {@code state}, and {@code input}, or {@code null} if such a transition
     * does not exist.
     *
     * @see net.automatalib.ts.DeterministicTransitionSystem#getTransition(Object, Object)
     */
    @Nullable
    @Override
    public CompactMealyTransition<O> getTransition(Integer state, @Nullable I input) {
        final CompactMealyTransition<O> result;
        if (getWord().getSymbol(state).equals(input)) {
            result = new CompactMealyTransition<>(state + 1, getOutput().getSymbol(state));
        } else {
            result = null;
        }
        return result;
    }

    @Nonnull
    @Override
    public Integer getSuccessor(CompactMealyTransition<O> transition) {
        return transition.getSuccId();
    }

    /**
     * Computes the output of the given input sequence.
     * <p>
     * Only returns a word ({@code null} otherwise) when the input sequence is precisely the finite representation
     * of the input word of the lasso.
     *
     * @see Output#computeOutput(Iterable)
     */
    @Override
    public Word<O> computeOutput(Iterable<? extends I> input) {
        final Word<O> output = getAutomaton().computeOutput(input);
        final Word<O> result;
        if (output.equals(getOutput())) {
            result = output;
        } else {
            result = Word.epsilon();
        }

        return result;
    }
}
