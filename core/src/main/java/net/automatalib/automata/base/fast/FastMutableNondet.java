/* Copyright (C) 2013 TU Dortmund
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
package net.automatalib.automata.base.fast;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.automata.ShrinkableAutomaton;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.base.StateIDDynamicMapping;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.helpers.StateIDStaticMapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.commons.util.nid.DynamicList;
import net.automatalib.commons.util.nid.IDChangeNotifier;
import net.automatalib.words.Alphabet;


public abstract class FastMutableNondet<S extends FastNondetState<S, T>, I, T, SP, TP>
		implements ShrinkableAutomaton<S, I, T, SP, TP>,
		UniversalFiniteAlphabetAutomaton<S, I, T, SP, TP>, StateIDs<S> {
	
	
	private final DynamicList<S> states
		= new DynamicList<>();
	private final IDChangeNotifier<S> tracker
		= new IDChangeNotifier<>();
	
	private final Set<S> initialStates
		= new HashSet<>();
	
	protected Alphabet<I> inputAlphabet;
	
	public FastMutableNondet(Alphabet<I> inputAlphabet) {
		this.inputAlphabet = inputAlphabet;
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
	
	protected abstract S createState(SP property);

}
