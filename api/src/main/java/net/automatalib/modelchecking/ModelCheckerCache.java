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

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * A model checker that caches calls to {@link #findCounterExample(Object, Collection, Object)}.
 *
 * @param <I> the input type
 * @param <A> the automaton type
 * @param <P> the property type
 * @param <D> the output type
 */
@ParametersAreNonnullByDefault
public interface ModelCheckerCache<I, A, P, D> extends ModelCheckerDelegate<I, A, P, D> {

    /**
     * Clears the cache.
     */
    void clear();

    interface DFAModelCheckerCache<I, P> extends ModelCheckerCache<I, DFA<?, I>, P, Boolean>,
            DFAModelCheckerDelegate<I, P> {}

    interface MealyModelCheckerCache<I, O, P> extends ModelCheckerCache<I, MealyMachine<?, I, ?, O>, P, Word<O>>,
            MealyModelCheckerDelegate<I, O, P> {}
}
