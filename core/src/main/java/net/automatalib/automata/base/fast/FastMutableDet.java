/* Copyright (C) 2013 TU Dortmund
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
package net.automatalib.automata.base.fast;

import java.util.Collection;

import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.ShrinkableAutomaton;
import net.automatalib.automata.ShrinkableDeterministic;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.base.StateIDDynamicMapping;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.helpers.StateIDStaticMapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.commons.util.nid.DynamicList;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.SimpleAlphabet;

public abstract class FastMutableDet<S extends FastDetState<S, T>, I, T, SP, TP> implements
		ShrinkableDeterministic<S, I, T, SP, TP>,
		UniversalFiniteAlphabetAutomaton<S, I, T, SP, TP>,
		StateIDs<S>,
		GrowableAlphabetAutomaton<I> {

	private final DynamicList<S> states = new DynamicList<>();

	private S initialState;
	
	protected final GrowingAlphabet<I> inputAlphabet;
	
	public FastMutableDet(Alphabet<I> inputAlphabet) {
		this.inputAlphabet = new SimpleAlphabet<>(inputAlphabet);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.simple.SimpleAutomaton#getStates()
	 */
	@Override
	public Collection<S> getStates() {
		return states;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.StateIDs#getState(int)
	 */
	@Override
	public S getState(int id) {
		return states.get(id);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.StateIDs#getStateId(java.lang.Object)
	 */
	@Override
	public int getStateId(S state) {
		return state.getId();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableDeterministic#setInitialState(java.lang.Object)
	 */
	@Override
	public void setInitialState(S state) {
		this.initialState = state;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableDeterministic#setTransition(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setTransition(S state, I input, T transition) {
		int inputIdx = inputAlphabet.getSymbolIndex(input);
		state.setTransition(inputIdx, transition);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.simple.SimpleDTS#getInitialState()
	 */
	@Override
	public S getInitialState() {
		return initialState;
	}

	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.DeterministicTransitionSystem#getTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public T getTransition(S state, I input) {
		int inputIdx = inputAlphabet.getSymbolIndex(input);
		return state.getTransition(inputIdx);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#clear()
	 */
	@Override
	public void clear() {
		states.clear();
		this.initialState = null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#addState(java.lang.Object)
	 */
	@Override
	public S addState(SP property) {
		S newState = createState(property);
		states.add(newState);
		return newState;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.ShrinkableAutomaton#removeState(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void removeState(S state, S replacement) {
		ShrinkableAutomaton.unlinkState(this, state, replacement, inputAlphabet);
		states.remove(state);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.InputAlphabetHolder#getInputAlphabet()
	 */
	@Override
	public Alphabet<I> getInputAlphabet() {
		return inputAlphabet;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#removeAllTransitions(java.lang.Object)
	 */
	@Override
	public void removeAllTransitions(S state) {
		state.clearTransitions();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.abstractimpl.AbstractDeterministicAutomaton#stateIDs()
	 */
	@Override
	public StateIDs<S> stateIDs() {
		return this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.abstractimpl.AbstractTS#createStaticStateMapping()
	 */
	@Override
	public <V> MutableMapping<S,V> createStaticStateMapping() {
		return new StateIDStaticMapping<>(this, size());
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.abstractimpl.AbstractTS#createDynamicStateMapping()
	 */
	@Override
	public <V> MutableMapping<S,V> createDynamicStateMapping() {
		return new StateIDDynamicMapping<>(this);
	}

	@Override
	public void addAlphabetSymbol(I symbol) {
		this.inputAlphabet.addSymbol(symbol);
		final int newAlphabetSize = this.inputAlphabet.size();

		for (final S s : this.getStates()) {
			s.ensureInputCapacity(newAlphabetSize);
		}
	}

	protected abstract S createState(SP property);
}
