/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.util.automaton.builder;

import de.learnlib.tooling.annotation.DocGenType;
import de.learnlib.tooling.annotation.edsl.Action;
import de.learnlib.tooling.annotation.edsl.Expr;
import de.learnlib.tooling.annotation.edsl.GenerateEDSL;
import net.automatalib.api.automaton.transducer.MutableMealyMachine;

/**
 * A fluent builder for {@link net.automatalib.automaton.transducer.MealyMachine}s.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <O>
 *         output symbol type
 * @param <A>
 *         concrete automaton type
 */
@GenerateEDSL(name = "MealyBuilder",
              syntax = "(<transition>)* withInitial (<transition>)* create",
              where = {@Expr(name = "transition", syntax = "from (on withOutput? (to|loop))+")},
              docGenType = DocGenType.COPY)
class MealyBuilderImpl<S, I, T, O, A extends MutableMealyMachine<S, ? super I, T, ? super O>>
        extends AutomatonBuilderImpl<S, I, T, Void, O, A> {

    /**
     * Constructs a new builder with the given (mutable) automaton to write to.
     *
     * @param automaton
     *         the automaton to write to
     */
    @Action
    MealyBuilderImpl(A automaton) {
        super(automaton);
    }

    /**
     * Associates an output symbol with the currently scoped transition(s).
     *
     * @param output
     *         the output
     */
    @Action
    public void withOutput(O output) {
        super.withProperty(output);
    }
}
