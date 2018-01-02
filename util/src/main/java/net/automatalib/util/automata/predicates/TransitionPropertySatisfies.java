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
package net.automatalib.util.automata.predicates;

import java.util.function.Predicate;

import net.automatalib.ts.TransitionPredicate;
import net.automatalib.ts.UniversalTransitionSystem;

final class TransitionPropertySatisfies<S, I, T, TP> implements TransitionPredicate<S, I, T> {

    private final UniversalTransitionSystem<?, ?, ? super T, ?, ? extends TP> uts;
    private final Predicate<? super TP> tpPred;

    TransitionPropertySatisfies(UniversalTransitionSystem<?, ?, ? super T, ?, ? extends TP> uts,
                                Predicate<? super TP> tpPred) {
        this.uts = uts;
        this.tpPred = tpPred;
    }

    @Override
    public boolean apply(S source, I input, T transition) {
        TP prop = uts.getTransitionProperty(transition);
        return tpPred.test(prop);
    }

}
