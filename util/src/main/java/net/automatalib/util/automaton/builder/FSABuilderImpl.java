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
import de.learnlib.tooling.annotation.edsl.GenerateEDSL;
import net.automatalib.automaton.fsa.MutableFSA;

/**
 * A fluent builder for {@link net.automatalib.automaton.fsa.FiniteStateAcceptor}s.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <A>
 *         concrete automaton type
 */
@GenerateEDSL(name = "FSABuilder",
              syntax = "(((from (on (loop|to)+)+)+)|withAccepting|withInitial)* create",
              constructorPublic = false,
              docGenType = DocGenType.COPY)
class FSABuilderImpl<S, I, A extends MutableFSA<S, ? super I>> extends AutomatonBuilderImpl<S, I, S, Boolean, Void, A> {

    /**
     * Constructs a new builder with the given (mutable) automaton to write to.
     *
     * @param automaton
     *         the automaton to write to
     */
    @Action
    FSABuilderImpl(A automaton) {
        super(automaton);
    }

    /**
     * Sets the target states of the current transition definition(s).
     *
     * @param firstStateId
     *         the mandatory object to identify the first state
     * @param otherStateIds
     *         the optional objects to identify additional states
     */
    @Action
    void to(Object firstStateId, Object... otherStateIds) {
        for (S src : currentStates) {
            for (I input : currentInputs) {
                for (S tgt : getStates(firstStateId, otherStateIds)) {
                    automaton.addTransition(src, input, tgt, currentTransProp);
                }
            }
        }
    }

    /**
     * Marks the given states as initial.
     *
     * @param stateId
     *         the object to identify the mandatory state
     * @param stateIds
     *         the objects to identify the additional states
     */
    @Action
    void withInitial(Object stateId, Object... stateIds) {
        for (S s : getStates(stateId, stateIds)) {
            automaton.setInitial(s, true);
        }
    }

    /**
     * Marks the given state as accepting.
     *
     * @param stateId
     *         the object to identify the state
     */
    @Action
    void withAccepting(Object stateId) {
        S state = getState(stateId);
        automaton.setAccepting(state, true);
    }

    /**
     * Marks the given states as accepting.
     *
     * @param stateId
     *         the object to identify the mandatory state
     * @param stateIds
     *         the objects to identify the additional states
     */
    @Action
    void withAccepting(Object stateId, Object... stateIds) {
        for (S s : getStates(stateId, stateIds)) {
            automaton.setAccepting(s, true);
        }
    }
}
