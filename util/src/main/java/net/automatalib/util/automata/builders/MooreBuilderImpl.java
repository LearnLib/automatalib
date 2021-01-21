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
package net.automatalib.util.automata.builders;

import com.github.misberner.duzzt.annotations.DSLAction;
import com.github.misberner.duzzt.annotations.GenerateEmbeddedDSL;
import com.github.misberner.duzzt.annotations.SubExpr;
import net.automatalib.automata.transducers.MutableMooreMachine;

@GenerateEmbeddedDSL(name = "MooreBuilder",
                     enableAllMethods = false,
                     syntax = "(withOutput|<transition>)* withInitial (withOutput|<transition>)* create",
                     where = {@SubExpr(name = "transition", definedAs = "from (on (to|loop))+")})
public class MooreBuilderImpl<S, I, T, O, A extends MutableMooreMachine<S, ? super I, T, ? super O>>
        extends AutomatonBuilderImpl<S, I, T, O, Void, A> {

    MooreBuilderImpl(A automaton) {
        super(automaton);
    }

    @Override
    @DSLAction(autoVarArgs = false)
    public void withInitial(Object stateId) {
        super.withInitial(stateId);
    }

    @DSLAction(autoVarArgs = false)
    public void withInitial(Object stateId, O output) {
        super.withInitial(stateId);
        withOutput(stateId, output);
    }

    @DSLAction(autoVarArgs = false)
    public void withOutput(Object stateId, O output) {
        super.withStateProperty(output, stateId);
    }
}
