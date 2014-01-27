/* Copyright (C) 2014 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.util.automata.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.MutableAutomaton;

import com.github.misberner.duzzt.annotations.DSLAction;
import com.github.misberner.duzzt.annotations.GenerateEmbeddedDSL;

@GenerateEmbeddedDSL(name = "AutomatonBuilder",
		enableAllMethods = false,
		syntax = "((from (on (withProperty? <<to* loop? to*>>)+)+)|withStateProperty|withInitial)* create")
class AutomatonBuilderImpl<S,I,T,SP,TP,A extends MutableAutomaton<S,? super I,T,? super SP,? super TP>> {
	
	protected final A automaton;
	private final Map<Object,S> stateMap = new HashMap<>();
	
	protected List<S> currentStates;
	protected List<I> currentInputs;
	protected TP currentTransProp;
	
	public AutomatonBuilderImpl(A automaton) {
		this.automaton = automaton;
	}
	
	protected S getState(Object stateId) {
		if(stateMap.containsKey(stateId)) {
			return stateMap.get(stateId);
		}
		S state = automaton.addState();
		stateMap.put(stateId, state);
		return state;
	}
	
	protected List<S> getStates(Object firstStateId, Object... otherStateIds) {
		if(otherStateIds.length == 0) {
			return Collections.singletonList(getState(firstStateId));
		}
		List<S> result = new ArrayList<>(1 + otherStateIds.length);
		result.add(getState(firstStateId));
		for(Object otherId : otherStateIds) {
			result.add(getState(otherId));
		}
		return result;
	}
	
	@DSLAction
	public void from(Object stateId) {
		this.currentStates = getStates(stateId);
		this.currentInputs = null;
	}
	
	@DSLAction
	public void from(Object firstStateId, Object... otherStateIds) {
		this.currentStates = getStates(firstStateId, otherStateIds);
	}
	
	@DSLAction
	public void on(I input) {
		this.currentInputs = Collections.singletonList(input);
		this.currentTransProp = null;
	}
	
	@DSLAction
	@SafeVarargs
	public final void on(I firstInput, I... otherInputs) {
		if(otherInputs.length == 0) {
			this.currentInputs = Collections.singletonList(firstInput);
		}
		this.currentInputs = new ArrayList<>(1 + otherInputs.length);
		this.currentInputs.add(firstInput);
		Collections.addAll(this.currentInputs, otherInputs);
	}
	
	@DSLAction
	public void withProperty(TP transProp) {
		this.currentTransProp = transProp;
	}
	
	@DSLAction
	public void to(Object stateId) {
		S tgt = getState(stateId);
		for(S src : currentStates) {
			for(I input : currentInputs) {
				automaton.addTransition(src, input, tgt, currentTransProp);
			}
		}
	}
	
	@DSLAction
	public void loop() {
		for(S src : currentStates) {
			for(I input : currentInputs) {
				automaton.addTransition(src, input, src, currentTransProp);
			}
		}
	}
	
	@DSLAction(terminator = true)
	public A create() {
		return automaton;
	}
	
	@DSLAction
	public void withInitial(Object stateId) {
		S state = getState(stateId);
		automaton.setInitial(state, true);
	}
	
	@DSLAction
	public void withStateProperty(SP stateProperty, Object stateId) {
		S state = getState(stateId);
		automaton.setStateProperty(state, stateProperty);
	}
}
