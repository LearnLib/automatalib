/* Copyright (C) 2013-2021 TU Dortmund
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

import net.automatalib.automata.fsa.FiniteStateAcceptor;

final class AcceptanceStatePredicate<S extends Object> implements Predicate<S> {

    private final FiniteStateAcceptor<? super S, ?> fsa;
    private final boolean acceptance;

    AcceptanceStatePredicate(FiniteStateAcceptor<? super S, ?> fsa, boolean acceptance) {
        this.fsa = fsa;
        this.acceptance = acceptance;
    }

    @Override
    public boolean test(S state) {
        return fsa.isAccepting(state) == acceptance;
    }

}
