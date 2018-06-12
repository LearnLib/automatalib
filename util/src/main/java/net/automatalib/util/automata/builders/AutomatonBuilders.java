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
package net.automatalib.util.automata.builders;

import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;

/**
 * Fluent interface automaton builders.
 *
 * @author Malte Isberner
 */
public final class AutomatonBuilders {

    private AutomatonBuilders() {
    }

    public static <I> DFABuilder<Integer, I, CompactDFA<I>> newDFA(Alphabet<I> alphabet) {
        return forDFA(new CompactDFA<>(alphabet));
    }

    public static <S, I, A extends MutableDFA<S, ? super I>> DFABuilder<S, I, A> forDFA(A dfa) {
        return new DFABuilder<>(dfa);
    }

    public static <I, O> MealyBuilder<Integer, I, CompactMealyTransition<O>, O, CompactMealy<I, O>> newMealy(Alphabet<I> alphabet) {
        return forMealy(new CompactMealy<>(alphabet));
    }

    public static <S, I, T, O, A extends MutableMealyMachine<S, ? super I, T, ? super O>> MealyBuilder<S, I, T, O, A> forMealy(
            A mealy) {
        return new MealyBuilder<>(mealy);
    }
}
