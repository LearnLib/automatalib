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
import net.automatalib.automaton.transducer.MutableMooreMachine;

/**
 * A fluent builder for {@link net.automatalib.automaton.transducer.MooreMachine}s.
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
@GenerateEDSL(name = "MooreBuilder",
              syntax = "(withOutput|<transition>)* withInitial (withOutput|<transition>)* create",
              where = {@Expr(name = "transition", syntax = "(from (on (to|loop))+)")},
              docGenType = DocGenType.COPY)
class MooreBuilderImpl<S, I, T, O, A extends MutableMooreMachine<S, ? super I, T, ? super O>>
        extends AutomatonBuilderImpl<S, I, T, O, Void, A> {

    /**
     * Constructs a new builder with the given (mutable) automaton to write to.
     *
     * @param automaton
     *         the automaton to write to
     */
    @Action
    MooreBuilderImpl(A automaton) {
        super(automaton);
    }

    /**
     * Marks the given state as initial and allows to set its output.
     *
     * @param stateId
     *         the object to identify the state
     * @param output
     *         the output of the state
     */
    @Action
    void withInitial(Object stateId, O output) {
        super.withInitial(stateId);
        withOutput(stateId, output);
    }

    /**
     * Associates with the given state the given output symbol.
     *
     * @param stateId
     *         the object to identify the state
     * @param output
     *         the output symbol
     */
    @Action
    void withOutput(Object stateId, O output) {
        super.withStateProperty(output, stateId);
    }
}
