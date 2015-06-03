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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;

public abstract class AbstractCompactSimpleDet<I, SP>
		implements MutableDeterministic<Integer,I,Integer,SP,Void>,
		UniversalFiniteAlphabetAutomaton<Integer,I,Integer,SP,Void>,
		StateIDs<Integer>,
		MutableDeterministic.StateIntAbstraction<I, Integer, SP, Void>,
		MutableDeterministic.FullIntAbstraction<Integer, SP, Void> {

	public static final float DEFAULT_RESIZE_FACTOR = 1.5f;
	public static final int DEFAULT_INIT_CAPACITY = 11;
	
	protected final Alphabet<I> alphabet;
	protected final int alphabetSize;
	protected int[] transitions;
	protected int stateCapacity;
	protected int numStates;
	protected int initial = -1;
	protected final float resizeFactor;
	
	public AbstractCompactSimpleDet(Alphabet<I> alphabet) {
		this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
	}
	
	public AbstractCompactSimpleDet(Alphabet<I> alphabet, int stateCapacity) {
		this(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
	}
	
	public AbstractCompactSimpleDet(Alphabet<I> alphabet, float resizeFactor) {
		this(alphabet, DEFAULT_INIT_CAPACITY, resizeFactor);
	}
	
	public AbstractCompactSimpleDet(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
		this.alphabet = alphabet;
		this.alphabetSize = alphabet.size();
		this.transitions = new int[stateCapacity * alphabetSize];
		Arrays.fill(this.transitions, 0, this.transitions.length, -1);
		this.resizeFactor = resizeFactor;
		this.stateCapacity = stateCapacity;
	}
	
	protected AbstractCompactSimpleDet(Alphabet<I> alphabet, int numStates, int initial, int[] transitions,
			float resizeFactor) {
		this.alphabet = alphabet;
		this.alphabetSize = alphabet.size();
		this.numStates = numStates;
		if (initial < 0 || initial >= numStates) {
			throw new IllegalArgumentException("Invalid initial state " + initial + " for automaton with "
					+ numStates + " states");
		}
		this.initial = initial;
		if (transitions.length < numStates * alphabetSize) {
			throw new IllegalArgumentException("Transition array is not large enough for automaton with "
					+ numStates + " states");
		}
		this.transitions = transitions;
		this.stateCapacity = transitions.length / alphabetSize;
		this.resizeFactor = resizeFactor;
	}
	
	
	protected AbstractCompactSimpleDet(Alphabet<I> alphabet, AbstractCompactSimpleDet<?,?> other) {
		this(alphabet, other.numStates, other.initial, other.transitions.clone(), other.resizeFactor);
	}
	
	public void ensureCapacity(int newCapacity) {
		if(newCapacity <= stateCapacity)
			return;
		
		int newCap = (int)(stateCapacity * resizeFactor);
		if(newCap < newCapacity)
			newCap = newCapacity;
		
		int[] newTrans = new int[newCap * alphabetSize];
		System.arraycopy(transitions, 0, newTrans, 0, stateCapacity * alphabetSize);
		Arrays.fill(newTrans, this.transitions.length, newTrans.length, -1);
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

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleDTS#getInitialState()
	 */
	@Override
	public Integer getInitialState() {
		return wrapState(initial);
	}
	
	public int getIntInitialState() {
		return initial;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.DeterministicTransitionSystem#getTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Integer getTransition(Integer state, I input) {
		int trans = getIntTransition(state.intValue(), input);
		return wrapState(trans);
	}
	
	public int getIntTransition(int state, int input) {
		int transId = state * alphabetSize + input;
		return transitions[transId];
	}
	
	public int getIntTransition(int state, I input) {
		return getIntTransition(state, alphabet.getSymbolIndex(input));
	}

	public void setInitialState(int state) {
		this.initial = state;
	}

	@Override
	public void setInitialState(Integer state) {
		setInitialState((state != null) ? state.intValue() : -1);
	}
	
	public void setTransition(int state, int inputIdx, int succ) {
		transitions[state * alphabetSize + inputIdx] = succ;
	}
	
	public void setTransition(int state, I input, int succ) {
		setTransition(state, alphabet.getSymbolIndex(input), succ);
	}

	@Override
	public void setTransition(Integer state, I input, Integer transition) {
		int succId = (transition != null) ? transition.intValue() : -1;
		setTransition(state.intValue(), input, succId);
	}
	
	@Override
	public void setTransition(Integer state, I input, Integer transition, Void property) {
		setTransition(state, input, transition);
	}

	@Override
	public void clear() {
		int endIdx = numStates * alphabetSize;
		numStates = 0;
		Arrays.fill(transitions, 0, endIdx, -1);
		initial = -1;
	}


	public void removeAllTransitions(int state) {
		int base = state * alphabetSize;
		
		Arrays.fill(transitions, base, base + alphabetSize, -1);
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
	
	public int getIntSuccessor(int state, I input) {
		return getIntTransition(state, input);
	}
	
	@Override
	public int getSuccessor(int state, I input) {
		return getIntTransition(state, input);
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
		setInitialState(state);
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
	public Integer createTransition(int successor, Void property) {
		return wrapState(successor);
	}
	
	
	@Override
	public StateIDs<Integer> stateIDs() {
		return this;
	}

	public int getIntSuccessor(int state, Iterable<? extends I> input) {
		int current = state;
		
		Iterator<? extends I> inputIt = input.iterator();
		
		while(current >= 0 && inputIt.hasNext()) {
			current = getIntSuccessor(current, inputIt.next());
		}
		
		return current;
	}
	
	@Override
	public Integer getSuccessor(Integer state, Iterable<? extends I> input) {
		return wrapState(getIntSuccessor(state.intValue(), input));
	}

	
	public int getIntState(Iterable<? extends I> input) {
		return getIntSuccessor(initial, input);
	}
	@Override
	public Integer getState(Iterable<? extends I> input) {
		return wrapState(getIntState(input));
	}
	
	@Override
	public StateIntAbstraction<I,Integer,SP,Void> stateIntAbstraction() {
		return this;
	}
	
	@Override
	public FullIntAbstraction<Integer,SP,Void> fullIntAbstraction(Alphabet<I> alphabet) {
		if (alphabet == this.alphabet) {
			return this;
		}
		return MutableDeterministic.super.fullIntAbstraction(alphabet);
	}
	
	public FullIntAbstraction<Integer,SP,Void> fullIntAbstraction() {
		return this;
	}
	
	
	@Override
	public int getIntSuccessor(Integer transition) {
		return unwrapState(transition);
	}

	@Override
	public Integer getTransition(int state, I input) {
		return wrapState(getSuccessor(state, input));
	}

	@Override
	public Integer getTransition(int state, int input) {
		return wrapState(getSuccessor(state, input));
	}
	
	@Override
	public int getSuccessor(int state, int input) {
		return getIntTransition(state, input);
	}
	
	@Override
	public void setTransition(int state, int input, Integer transition) {
		setTransition(state, input, unwrapState(transition));
	}
	
	@Override
	public void setTransition(int state, int input, int successor, Void property) {
		setTransition(state, input, successor);
	}
	
	@Override
	public void setTransition(int state, I input, Integer transition) {
		setTransition(state, input, unwrapState(transition));
	}
	
	@Override
	public void setTransition(int state, I input, int successor, Void property) {
		setTransition(state, input, successor);
	}

	@Override
	public int numInputs() {
		return alphabet.size();
	}

	protected static Integer wrapState(int id) {
		if(id < 0) {
			return null;
		}
		return Integer.valueOf(id);
	}
	
	protected static int unwrapState(Integer state) {
		if (state == null) {
			return INVALID_STATE;
		}
		return state.intValue();
	}
	
}
