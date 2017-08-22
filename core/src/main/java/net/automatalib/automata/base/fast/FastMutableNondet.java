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
import java.util.HashSet;
import java.util.Set;

import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.ShrinkableAutomaton;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.base.StateIDDynamicMapping;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.helpers.StateIDStaticMapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.commons.util.nid.DynamicList;
import net.automatalib.commons.util.nid.IDChangeNotifier;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.SimpleAlphabet;

public abstract class FastMutableNondet<S extends FastNondetState<S, T>, I, T, SP, TP>
		implements ShrinkableAutomaton<S, I, T, SP, TP>,
		UniversalFiniteAlphabetAutomaton<S, I, T, SP, TP>,
		StateIDs<S>,
		GrowableAlphabetAutomaton<I> {
	
	
	private final DynamicList<S> states
		= new DynamicList<>();
	private final IDChangeNotifier<S> tracker
		= new IDChangeNotifier<>();
	
	private final Set<S> initialStates
		= new HashSet<>();
	
	protected GrowingAlphabet<I> inputAlphabet;
	
	public FastMutableNondet(Alphabet<I> inputAlphabet) {
		this.inputAlphabet = new SimpleAlphabet<>(inputAlphabet);
	}

	@Override
	public Collection<S> getStates() {
		return states;
	}

	@Override
	public S getState(int id) {
		return states.get(id);
	}

	@Override
	public int getStateId(S state) {
		return state.getId();
	}

	@Override
	public Set<S> getInitialStates() {
		return initialStates;
	}

	@Override
	public Collection<T> getTransitions(S state, I input) {
		int inputIdx = inputAlphabet.getSymbolIndex(input);
		return state.getTransitions(inputIdx);
	}

	

	@Override
	public void clear() {
		states.clear();
		initialStates.clear();
	}

	@Override
	public S addState(SP property) {
		S newState = createState(property);
		states.add(newState);
		return newState;
	}

	@Override
	public void setInitial(S state, boolean initial) {
		if(initial)
			initialStates.add(state);
		else
			initialStates.remove(state);
	}

	@Override
	public void removeState(S state, S replacement) {
		ShrinkableAutomaton.unlinkState(this, state, replacement, inputAlphabet);
		states.remove(state);
		if(initialStates.remove(state))
			initialStates.add(state);
	}
	
	@Override
	public void removeAllTransitions(S state) {
		state.clearTransitions();
	}


	@Override
	public void setTransitions(S state, I input, Collection<? extends T> transitions) {
		int inputIdx = inputAlphabet.getSymbolIndex(input);
		state.setTransitions(inputIdx, transitions);
	}

	@Override
	public Alphabet<I> getInputAlphabet() {
		return inputAlphabet;
	}
	
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
		StateIDDynamicMapping<S, V> mapping = new StateIDDynamicMapping<>(this);
		tracker.addListener(mapping, true);
		return mapping;
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
