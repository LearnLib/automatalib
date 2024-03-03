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
import net.automatalib.automaton.fsa.MutableDFA;

/**
 * A fluent builder for {@link net.automatalib.automaton.fsa.DFA}s.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <A>
 *         concrete automaton type
 */
@GenerateEDSL(name = "DFABuilder",
              syntax = "(<transOrAcc>)* withInitial (<transOrAcc>)* create",
              where = @Expr(name = "transOrAcc", syntax = "(from (on (loop|to))+)+|withAccepting"),
              constructorPublic = false,
              docGenType = DocGenType.COPY)
class DFABuilderImpl<S, I, A extends MutableDFA<S, ? super I>> extends FSABuilderImpl<S, I, A> {

    /**
     * Constructs a new builder with the given (mutable) automaton to write to.
     *
     * @param automaton
     *         the automaton to write to
     */
    @Action
    DFABuilderImpl(A automaton) {
        super(automaton);
    }

    // override to un-mark it as action
    @Override
    void withInitial(Object stateId, Object... stateIds) {
        throw new IllegalArgumentException("deterministic automata can only have a single initial state");
    }

    @Override
    void to(Object stateId, Object... stateIds) {
        throw new IllegalArgumentException("deterministic automata can only have a single target state");
    }
}
