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

import de.learnlib.tooling.annotation.edsl.Action;
import de.learnlib.tooling.annotation.edsl.GenerateEDSL;
import net.automatalib.automaton.MutableAutomaton;

@GenerateEDSL(name = "AutomatonBuilder",
              syntax = "((from (on (withProperty? (to* loop? to*))+)+)|withStateProperty|withInitial)* create",
              classDoc = "A fluent builder for a {@link MutableAutomaton}.\n" +
                         "@param <S> state type\n" +
                         "@param <I> input symbol type\n" +
                         "@param <T> transition type\n" +
                         "@param <SP> state property type\n" +
                         "@param <TP> transition property type\n" +
                         "@param <A> automaton type\n")
@SuppressWarnings("nullness") // nullness correctness guaranteed by states of regular expression
class AutomatonBuilderImpl<S, I, T, SP, TP, A extends MutableAutomaton<S, ? super I, T, ? super SP, ? super TP>> {

    protected final A automaton;
    private final Map<Object, S> stateMap = new HashMap<>();

    protected List<S> currentStates;
    protected List<I> currentInputs;
    protected TP currentTransProp;

    @Action
    AutomatonBuilderImpl(A automaton) {
        this.automaton = automaton;
    }

    @Action
    public void from(Object stateId) {
        this.currentStates = getStates(stateId);
        this.currentInputs = null;
    }

    @Action
    public void from(Object firstStateId, Object... otherStateIds) {
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

    @Action
    public void on(I input) {
        this.currentInputs = Collections.singletonList(input);
        this.currentTransProp = null;
    }

    @Action
    @SafeVarargs
    public final void on(I firstInput, I... otherInputs) {
        this.currentInputs = new ArrayList<>(1 + otherInputs.length);
        this.currentInputs.add(firstInput);
        Collections.addAll(this.currentInputs, otherInputs);
        this.currentTransProp = null;
    }

    @Action
    public void withProperty(TP transProp) {
        this.currentTransProp = transProp;
    }

    @Action
    public void to(Object stateId) {
        S tgt = getState(stateId);
        for (S src : currentStates) {
            for (I input : currentInputs) {
                automaton.addTransition(src, input, tgt, currentTransProp);
            }
        }
    }

    @Action
    public void loop() {
        for (S src : currentStates) {
            for (I input : currentInputs) {
                automaton.addTransition(src, input, src, currentTransProp);
            }
        }
    }

    @Action(isTerminating = true)
    public A create() {
        return automaton;
    }

    @Action
    public void withInitial(Object stateId) {
        S state = getState(stateId);
        automaton.setInitial(state, true);
    }

    @Action
    public void withStateProperty(SP stateProperty, Object stateId) {
        S state = getState(stateId);
        automaton.setStateProperty(state, stateProperty);
    }
}
