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

import java.util.SortedSet;

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Word;

/**
 * A lasso is an single infinite word.
 * <p>
 * The implementation is an automaton such that its singleton language is the infinite word. Also, the implementation is
 * actually the finite representation (by unrolling the loop) of the infinite word, including information how many times
 * the loop of the lasso is unrolled.
 *
 * @param <S>
 *         the state type of the automaton that contains the infinite word.
 * @param <A>
 *         the automaton type which contains the lasso.
 * @param <I>
 *         the input type
 * @param <D>
 *         the output type
 *
 * @author Jeroen Meijer
 */
@ParametersAreNonnullByDefault
public interface Lasso<S, A extends SimpleDTS<S, I> & Output<I, D>, I, D>
        extends SimpleDTS<Integer, I>, Output<I, D>, SimpleAutomaton<Integer, I>, InputAlphabetHolder<I> {

    /**
     * Gets the finite representation of the lasso.
     *
     * @return the Word.
     */
    Word<I> getWord();

    /**
     * Gets the loop of the lasso.
     *
     * @return the Word.
     */
    Word<I> getLoop();

    /**
     * Gets the prefix of the lasso.
     *
     * @return the Word.
     */
    Word<I> getPrefix();

    /**
     * Gets the finite representation of the output of the lasso.
     *
     * @return the output type D.
     */
    D getOutput();

    /**
     * Gets the automaton containing the lasso.
     *
     * @return the automaton type a.
     */
    A getAutomaton();

    /**
     * The sorted set containing some symbol indices after which the begin state of the loop is visited.
     */
    SortedSet<Integer> getLoopBeginIndices();

    /**
     * A DFALasso is a lasso for {@link DFA}s.
     *
     * @param <S>
     *         the state type of the DFA that contains the lasso.
     * @param <I>
     *         the input type
     */
    interface DFALasso<S, I> extends Lasso<S, DFA<S, I>, I, Boolean>, DFA<Integer, I> {}

    /**
     * A MealyLasso is a lasso for {@link MealyMachine}s.
     *
     * @param <S>
     *         the state type of the Mealy machine that contains the lasso.
     * @param <I>
     *         the input type
     * @param <O>
     *         the output type
     */
    interface MealyLasso<S, I, T, O>
            extends Lasso<S, MealyMachine<S, I, ?, O>, I, Word<O>>, MealyMachine<Integer, I, T, O> {}
}
