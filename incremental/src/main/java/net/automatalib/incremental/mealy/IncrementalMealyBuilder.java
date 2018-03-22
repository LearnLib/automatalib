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
package net.automatalib.incremental.mealy;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.graphs.Graph;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.IncrementalConstruction;
import net.automatalib.ts.transout.MealyTransitionSystem;
import net.automatalib.words.Word;

public interface IncrementalMealyBuilder<I, O> extends IncrementalConstruction<MealyMachine<?, I, ?, O>, I> {

    Word<O> lookup(Word<? extends I> inputWord);

    /**
     * Retrieves the output word for the given input word. If no definitive information for the input word exists, the
     * output for the longest known prefix will be returned.
     *
     * @param inputWord
     *         the input word
     * @param output
     *         a consumer for constructing the output word
     *
     * @return <tt>true</tt> if the information contained was complete (in this case, <code>word.length() ==
     * output.size()</code> will hold), <tt>false</tt> otherwise.
     */
    boolean lookup(Word<? extends I> inputWord, List<? super O> output);

    /**
     * Incorporates a pair of input/output words into the stored information.
     *
     * @param inputWord
     *         the input word
     * @param outputWord
     *         the corresponding output word
     *
     * @throws ConflictException
     *         if this information conflicts with information already stored
     */
    void insert(Word<? extends I> inputWord, Word<? extends O> outputWord) throws ConflictException;

    @Override
    GraphView<I, O, ?, ?> asGraph();

    @Override
    MealyTransitionSystem<?, I, ?, O> asTransitionSystem();

    interface GraphView<I, O, N, E> extends Graph<N, E> {

        @Nullable
        I getInputSymbol(@Nonnull E edge);

        @Nullable
        O getOutputSymbol(@Nonnull E edge);

        @Nonnull
        N getInitialNode();
    }
}
