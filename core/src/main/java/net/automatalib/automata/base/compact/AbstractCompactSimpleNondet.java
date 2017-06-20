/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.automata.base.compact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.SimpleAlphabet;

public abstract class AbstractCompactSimpleNondet<I, SP>
		implements MutableAutomaton<Integer,I,Integer,SP,Void>,
		UniversalFiniteAlphabetAutomaton<Integer,I,Integer,SP,Void>,
		StateIDs<Integer>,
		GrowableAlphabetAutomaton<I> {

	public static final float DEFAULT_RESIZE_FACTOR = 1.5f;
	public static final int DEFAULT_INIT_CAPACITY = 11;
	
	protected final GrowingAlphabet<I> alphabet;
	protected int alphabetSize;
//	protected TIntSet[] transitions;
	protected Set<Integer>[] transitions; // TODO: replace by primitive specialization
	protected int stateCapacity;
	protected int numStates;
//	protected final TIntSet initial;
	protected final Set<Integer> initial; // TODO: replace by primitive specialization
	
	private final float resizeFactor;
	
	public AbstractCompactSimpleNondet(Alphabet<I> alphabet) {
		this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
	}
	
	public AbstractCompactSimpleNondet(Alphabet<I> alphabet, int stateCapacity) {
		this(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
	}
	
	public AbstractCompactSimpleNondet(Alphabet<I> alphabet, float resizeFactor) {
		this(alphabet, DEFAULT_INIT_CAPACITY, resizeFactor);
	}
	
	@SuppressWarnings("unchecked")
	public AbstractCompactSimpleNondet(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
		this.alphabet = new SimpleAlphabet<>(alphabet);
		this.alphabetSize = alphabet.size();
//		this.transitions = new TIntSet[stateCapacity * alphabetSize];
		this.transitions = new Set[stateCapacity * alphabetSize];  // TODO: replace by primitive specialization
		
		this.resizeFactor = resizeFactor;
		this.stateCapacity = stateCapacity;
		
//		this.initial = new TIntHashSet();
		this.initial = new HashSet<>(); // TODO: replace by primitive specialization
	}
	
	protected AbstractCompactSimpleNondet(Alphabet<I> alphabet, AbstractCompactSimpleNondet<?, ?> other) {
		this.alphabet = new SimpleAlphabet<>(alphabet);
		this.alphabetSize = alphabet.size();
		this.transitions = other.transitions.clone();
		for (int i = 0; i < transitions.length; i++) {
//			TIntSet tgts = transitions[i];
			Set<Integer> tgts = transitions[i]; // TODO: replace by primitive specialization
			if (tgts != null) {
//				transitions[i] = new TIntHashSet(tgts);
				transitions[i] = new HashSet<>(tgts); // TODO: replace by primitive specialization
			}
		}
		this.numStates = other.numStates;
		this.resizeFactor = other.resizeFactor;
		this.stateCapacity = other.stateCapacity;
		
//		this.initial = new TIntHashSet(other.initial);
		this.initial = new HashSet<>(); // TODO: replace by primitive specialization
	}
	
	@SuppressWarnings("unchecked")
	public void ensureCapacity(int newCapacity) {
		if(newCapacity <= stateCapacity)
			return;
		
		int newCap = (int)(stateCapacity * resizeFactor);
		if(newCap < newCapacity)
			newCap = newCapacity;
		
//		TIntSet[] newTrans = new TIntSet[newCap * alphabetSize];
		Set<Integer>[] newTrans = new Set[newCap * alphabetSize]; // TODO: replace by primitive specialization
		System.arraycopy(transitions, 0, newTrans, 0, stateCapacity * alphabetSize);
		this.transitions = newTrans;
		ensureCapacity(stateCapacity, newCap);
		this.stateCapacity = newCap;
	}
	
	
	protected void ensureCapacity(int oldCap, int newCap) {}
	
	@Override
	public Alphabet<I> getInputAlphabet() {
		return alphabet;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.abstractimpl.AbstractDeterministicAutomaton#size()
	 */
	@Override
	public int size() {
		return numStates;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.simple.SimpleAutomaton#getStates()
	 */
	@Override
	public Collection<Integer> getStates() {
		return CollectionsUtil.intRange(0, numStates);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.simple.SimpleAutomaton#getState(int)
	 */
	@Override
	public Integer getState(int id) {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.simple.SimpleAutomaton#getStateId(java.lang.Object)
	 */
	@Override
	public int getStateId(Integer state) {
		return state.intValue();
	}

	
//	public TIntCollection getIntInitialStates() {
	public Set<Integer> getIntInitialStates() { // TODO: replace by primitive specialization
		return initial;
	}
	
	
//	public TIntCollection getIntTransitions(int state, I input) {
	public Set<Integer> getIntTransitions(int state, I input) { // TODO: replace by primitive specialization
		int transId = state * alphabetSize + alphabet.getSymbolIndex(input);
		return successors(transId);
	}


	public void removeAllTransitions(int state) {
		int base = state * alphabetSize;
		
		Arrays.fill(transitions, base, base + alphabetSize, null);
	}
	
	@Override
	public void removeTransition(Integer state, I input, Integer transition) {
		removeTransition(state.intValue(), input, transition.intValue());
	}
	
	public void removeTransition(int stateId, I input, int successorId) {
		removeTransition(stateId, alphabet.getSymbolIndex(input), successorId);
	}
	
	public void removeTransition(int stateId, int inputIdx, int successorId) {
		int transIdx = stateId * alphabetSize + inputIdx;
//		TIntCollection successors = transitions[transIdx];
		Collection<Integer> successors = transitions[transIdx]; // TODO: replace by primitive specialization
		if(successors != null) {
			successors.remove(successorId);
		}
	}

	@Override
	public void removeAllTransitions(Integer state, I input) {
		removeAllTransitions(state.intValue(), input);
	}
	
	public void removeAllTransitions(int stateId, I input) {
		removeAllTransitions(stateId, alphabet.getSymbolIndex(input));
	}
	
	public void removeAllTransitions(int stateId, int inputIdx) {
		int transIdx = stateId * alphabetSize + inputIdx;
		transitions[transIdx] = null;
	}

	@Override
	public void addTransition(Integer state, I input, Integer transition) {
		addTransition(state.intValue(), input, transition.intValue());
	}
	
	public void addTransition(int stateId, I input, int succId) {
		addTransition(stateId, alphabet.getSymbolIndex(input), succId);
	}
	
	public void addTransition(int stateId, int inputIdx, int succId) {
		int transIdx = stateId * alphabetSize + inputIdx;
//		TIntSet successors = transitions[transIdx];
		Set<Integer> successors = transitions[transIdx]; // TODO: replace by primitive specialization
		if(successors == null) {
//			successors = new TIntHashSet();
			successors = new HashSet<>(); // TODO: replace by primitive specialization
			transitions[transIdx] = successors;
		}
		successors.add(succId);
	}

	@Override
	public void removeAllTransitions(Integer state) {
		removeAllTransitions(state.intValue());
	}

	@Override
	public Integer copyTransition(Integer trans, Integer succ) {
		return succ;
	}

	@Override
	public Integer getSuccessor(Integer transition) {
		return transition;
	}
		
	public abstract SP getStateProperty(int stateId);

	@Override
	public SP getStateProperty(Integer state) {
		return getStateProperty(state.intValue());
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.UniversalTransitionSystem#getTransitionProperty(java.lang.Object)
	 */
	@Override
	public Void getTransitionProperty(Integer transition) {
		return null;
	}
	
	protected abstract void initState(int stateId, SP property);

	public int addIntState(SP property) {
		int stateId = numStates++;
		ensureCapacity(numStates);
		initState(stateId, property);
		return stateId;
	}
	
	public int addIntState() {
		return addIntState(null);
	}
	
	public int addIntInitialState(SP property) {
		int state = addIntState(property);
		setInitial(state, true);
		return state;
	}
	
	public int addIntInitialState() {
		return addIntInitialState(null);
	}
	
	@Override
	public Integer addState(SP property) {
		return addIntState(property);
	}
	
	public abstract void setStateProperty(int stateId, SP property);

	@Override
	public void setStateProperty(Integer state, SP property) {
		setStateProperty(state.intValue(), property);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#setTransitionProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setTransitionProperty(Integer transition, Void property) {
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#createTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Integer createTransition(Integer successor, Void properties) {
		return successor;
	}
	
	
	@Override
	public StateIDs<Integer> stateIDs() {
		return this;
	}
	
//	protected TIntSet successors(int transId) {
	protected Set<Integer> successors(int transId) { // TODO: replace by primitive specialization
//		TIntSet successors = transitions[transId];
		Set<Integer> successors = transitions[transId]; // TODO: replace by primitive specialization
		if(successors == null) {
//			return EMPTY_SET;
			return Collections.emptySet(); // TODO: replace by primitive specialization
		}
		return successors;
	}

	protected static Integer wrapState(int id) {
		if(id < 0) {
			return null;
		}
		return Integer.valueOf(id);
	}

	@Override
	public void clear() {
		Arrays.fill(transitions, 0, numStates * alphabetSize, null);
		this.numStates = 0;
		
		this.initial.clear();
	}

	@Override
	public void setInitial(Integer state, boolean initial) {
		setInitial(state.intValue(), initial);
	}
	
	public void setInitial(int state, boolean initial) {
		if(initial) {
			this.initial.add(state);
		}
		else {
			this.initial.remove(state);
		}
	}

	@Override
	public void setTransitions(Integer state, I input,
			Collection<? extends Integer> transitions) {
//		TIntList successors = new TIntArrayList(transitions.size());
		List<Integer> successors = new ArrayList<>(transitions.size()); // TODO: replace by primitive specialization
		for(Integer succ : transitions) {
			successors.add(succ);
		}
		setTransitions(state.intValue(), input, successors);
	}
	
//	public void setTransitions(int state, I input, TIntCollection successors) {
	public void setTransitions(int state, I input, Collection<? extends Integer> successors) { // TODO: replace by primitive specialization
		setTransitions(state, alphabet.getSymbolIndex(input), successors);
	}
	
//	public void setTransitions(int state, int inputIdx, TIntCollection successors) {
	public void setTransitions(int state, int inputIdx, Collection<? extends Integer> successors) { // TODO: replace by primitive specialization
		int transIdx = state * alphabetSize + inputIdx;
//		TIntSet succs = transitions[transIdx];
		Set<Integer> succs = transitions[transIdx]; // TODO: replace by primitive specialization
		if(succs == null) {
//			succs = new TIntHashSet(successors);
			succs = new HashSet<>(); // TODO: replace by primitive specialization
			transitions[transIdx] = succs;
		}
		else {
			succs.clear();
		}
		succs.addAll(successors);
	}

	@Override
	public Collection<? extends Integer> getTransitions(Integer state, I input) {
//		return new TIntSetDecorator(getTransitions(state.intValue(), input));
		return getTransitions(state.intValue(), input); // TODO: replace by primitive specialization
	}
	
//	public TIntSet getTransitions(int state, I input) {
	public Set<Integer> getTransitions(int state, I input) { // TODO: replace by primitive specialization
		return getTransitions(state, alphabet.getSymbolIndex(input));
	}
	
//	public TIntSet getTransitions(int state, int inputIdx) {
	public Set<Integer> getTransitions(int state, int inputIdx) { // TODO: replace by primitive specialization
		return successors(state * alphabetSize + inputIdx);
	}

	@Override
	public Set<? extends Integer> getInitialStates() {
//		return new TIntSetDecorator(initial);
		return initial; // TODO: replace by primitive specialization
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addAlphabetSymbol(I symbol) {

		if (this.alphabet.containsSymbol(symbol)) {
			return;
		}

		final int oldAlphabetSize = this.alphabetSize;
		final int newAlphabetSize = oldAlphabetSize + 1;
		final int newArraySize = this.transitions.length + this.stateCapacity;
		final Set<Integer>[] newTransitions = new Set[newArraySize];

		for (int i = 0; i < this.numStates; i++) {
			System.arraycopy(transitions, i*oldAlphabetSize, newTransitions, i*newAlphabetSize, oldAlphabetSize);
		}

		this.transitions = newTransitions;
		this.alphabet.addSymbol(symbol);
		this.alphabetSize = newAlphabetSize;
	}
}
