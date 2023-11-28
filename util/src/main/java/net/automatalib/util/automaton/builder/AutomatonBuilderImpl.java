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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.learnlib.tooling.annotation.DocGenType;
import de.learnlib.tooling.annotation.edsl.Action;
import de.learnlib.tooling.annotation.edsl.GenerateEDSL;
import net.automatalib.api.automaton.MutableAutomaton;

/**
 * A fluent builder for {@link net.automatalib.automaton.Automaton automata}.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 * @param <A>
 *         concrete automaton type
 */
@GenerateEDSL(name = "AutomatonBuilder",
              syntax = "((from (on (withProperty? (to* loop? to*))+)+)|withStateProperty|withInitial)* create",
              docGenType = DocGenType.COPY)
@SuppressWarnings("nullness") // nullness correctness guaranteed by states of regular expression
class AutomatonBuilderImpl<S, I, T, SP, TP, A extends MutableAutomaton<S, ? super I, T, ? super SP, ? super TP>> {

    protected final A automaton;
    private final Map<Object, S> stateMap = new HashMap<>();

    protected List<S> currentStates;
    protected List<I> currentInputs;
    protected TP currentTransProp;

    /**
     * Constructs a new builder with the given (mutable) automaton to write to.
     *
     * @param automaton
     *         the automaton to write to
     */
    @Action
    AutomatonBuilderImpl(A automaton) {
        this.automaton = automaton;
    }

    /**
     * Starts a definition of transition(s) from a given source state.
     *
     * @param stateId
     *         the object to identify the state
     */
    @Action
    void from(Object stateId) {
        this.currentStates = getStates(stateId);
        this.currentInputs = null;
    }

    /**
     * Starts a definition of transition(s) from multiple given source states.
     *
     * @param firstStateId
     *         the mandatory object to identify the first state
     * @param otherStateIds
     *         the optional objects to identify additional states
     */
    @Action
    void from(Object firstStateId, Object... otherStateIds) {
        this.currentStates = getStates(firstStateId, otherStateIds);
    }

    protected List<S> getStates(Object firstStateId, Object... otherStateIds) {
        if (otherStateIds.length == 0) {
            return Collections.singletonList(getState(firstStateId));
        }
        List<S> result = new ArrayList<>(1 + otherStateIds.length);
        result.add(getState(firstStateId));
        for (Object otherId : otherStateIds) {
            result.add(getState(otherId));
        }
        return result;
    }

    protected S getState(Object stateId) {
        if (stateMap.containsKey(stateId)) {
            return stateMap.get(stateId);
        }
        S state = automaton.addState();
        stateMap.put(stateId, state);
        return state;
    }

    /**
     * Specifies the input symbol of the current transition definition(s).
     *
     * @param input
     *         the input symbol
     */
    @Action
    void on(I input) {
        this.currentInputs = Collections.singletonList(input);
        this.currentTransProp = null;
    }

    /**
     * Specifies multiple input symbols of the current transition definition(s).
     *
     * @param firstInput
     *         the mandatory first input symbol
     * @param otherInputs
     *         the optional additional input symbols
     */
    @Action
    @SafeVarargs
    final void on(I firstInput, I... otherInputs) {
        this.currentInputs = new ArrayList<>(1 + otherInputs.length);
        this.currentInputs.add(firstInput);
        Collections.addAll(this.currentInputs, otherInputs);
        this.currentTransProp = null;
    }

    /**
     * Associates a transition property with the currently scoped transition(s).
     *
     * @param transProp
     *         the property
     */
    @Action
    void withProperty(TP transProp) {
        this.currentTransProp = transProp;
    }

    /**
     * Selects the target state of the current transition definition(s) and adds all resulting transitions to the
     * automaton.
     *
     * @param stateId
     *         the object to identify the state
     */
    @Action
    public void to(Object stateId) {
        S tgt = getState(stateId);
        for (S src : currentStates) {
            for (I input : currentInputs) {
                automaton.addTransition(src, input, tgt, currentTransProp);
            }
        }
    }

    /**
     * Selects the target state(s) of the current transition definition(s) by looping them to their source state.
     */
    @Action
    public void loop() {
        for (S src : currentStates) {
            for (I input : currentInputs) {
                automaton.addTransition(src, input, src, currentTransProp);
            }
        }
    }

    /**
     * Returns the constructed automaton.
     *
     * @return the automaton
     */
    @Action(terminating = true)
    public A create() {
        return automaton;
    }

    /**
     * Marks the given state as initial.
     *
     * @param stateId
     *         the object to identify the state
     */
    @Action
    public void withInitial(Object stateId) {
        S state = getState(stateId);
        automaton.setInitial(state, true);
    }

    /**
     * Associates with the given state the given state property.
     *
     * @param stateProperty
     *         the property to associate with the state
     * @param stateId
     *         the object to identify the state
     */
    @Action
    public void withStateProperty(SP stateProperty, Object stateId) {
        S state = getState(stateId);
        automaton.setStateProperty(state, stateProperty);
    }
}
