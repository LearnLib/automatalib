/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
package net.automatalib.automaton.transducer.probabilistic;

import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.automaton.concept.MutableProbabilistic;
import net.automatalib.automaton.concept.MutableTransitionOutput;

public interface MutableProbabilisticMealy<S, I, T, O> extends ProbabilisticMealyMachine<S, I, T, O>,
                                                               MutableTransitionOutput<T, O>,
                                                               MutableProbabilistic<T>,
                                                               MutableAutomaton<S, I, T, Void, ProbabilisticOutput<O>> {

    @Override
    default void setStateProperty(S state, Void property) {}

}
