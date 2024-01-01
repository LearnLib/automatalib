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

import de.learnlib.tooling.annotation.edsl.Action;
import de.learnlib.tooling.annotation.edsl.Expr;
import de.learnlib.tooling.annotation.edsl.GenerateEDSL;
import net.automatalib.automaton.fsa.MutableDFA;

@GenerateEDSL(name = "DFABuilder",
              syntax = "(<transOrAcc>)* withInitial (<transOrAcc>)* create",
              where = {@Expr(name = "transOrAcc", syntax = "(from (on (loop|to))+)+|withAccepting")},
              classDoc = "A fluent builder for a {@link MutableDFA}.\n" +
                         "@param <S> state type\n" +
                         "@param <I> input symbol type\n" +
                         "@param <A> automaton type\n")
class DFABuilderImpl<S, I, A extends MutableDFA<S, ? super I>> extends FSABuilderImpl<S, I, A> {

    @Action
    DFABuilderImpl(A automaton) {
        super(automaton);
    }

}
