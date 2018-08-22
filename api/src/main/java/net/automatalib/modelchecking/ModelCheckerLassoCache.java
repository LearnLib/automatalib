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

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * @see ModelCheckerCache
 */
@ParametersAreNonnullByDefault
public interface ModelCheckerLassoCache<I, A, P, D> extends ModelCheckerCache<I, A, P, D>, ModelCheckerLassoDelegate<I, A, P, D> {

    interface DFAModelCheckerLassoCache<I, P> extends ModelCheckerLassoCache<I, DFA<?, I>, P, Boolean>,
            DFAModelCheckerLassoDelegate<I, P> {}

    interface MealyModelCheckerLassoCache<I, O, P> extends
            ModelCheckerLassoCache<I, MealyMachine<?, I, ?, O>, P, Word<O>>, MealyModelCheckerLassoDelegate<I, O, P> {}
}
