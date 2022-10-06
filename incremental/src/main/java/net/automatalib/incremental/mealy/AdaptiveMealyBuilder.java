/* Copyright (C) 2013-2022 TU Dortmund
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

import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.incremental.IncrementalConstruction;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.words.Word;

public interface AdaptiveMealyBuilder<I, O> extends MealyBuilder<I, O> {

    /**
     * Incorporates a pair of input/output words into the stored information.
     *
     * @param inputWord
     *         the input word
     * @param outputWord
     *         the corresponding output word
     *
     * @return {@code true} if the inserted output word has overridden existing information, {@code false} otherwise.
     */
    boolean insert(Word<? extends I> inputWord, Word<? extends O> outputWord);

}
