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

import net.automatalib.automaton.Automaton;
import net.automatalib.automaton.concept.Probabilistic;
import net.automatalib.automaton.concept.TransitionOutput;
import net.automatalib.ts.UniversalTransitionSystem;

public interface ProbabilisticMealyMachine<S, I, T, O> extends Automaton<S, I, T>,
                                                               TransitionOutput<T, O>,
                                                               UniversalTransitionSystem<S, I, T, Void, ProbabilisticOutput<O>>,
                                                               Probabilistic<T> {

    @Override
    default Void getStateProperty(S state) {
        return null;
    }

    @Override
    default float getTransitionProbability(T transition) {
        return getTransitionProperty(transition).getProbability();
    }

    @Override
    default O getTransitionOutput(T transition) {
        return getTransitionProperty(transition).getOutput();
    }
}
