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
package net.automatalib.automata.base.compact;

import gnu.trove.TCollections;
import gnu.trove.TIntCollection;
import gnu.trove.decorator.TIntSetDecorator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;

public abstract class AbstractCompactSimpleNondet<I, SP>
		implements MutableAutomaton<Integer,I,Integer,SP,Void>,
		UniversalFiniteAlphabetAutomaton<Integer,I,Integer,SP,Void>, StateIDs<Integer> {
	
	// FIXME: This should come with trove!
	public static final TIntSet EMPTY_SET
		= TCollections.unmodifiableSet(new TIntHashSet());

	public static final float DEFAULT_RESIZE_FACTOR = 1.5f;
	public static final int DEFAULT_INIT_CAPACITY = 11;
	
	private final Alphabet<I> alphabet;
	private final int alphabetSize;
	private TIntSet[] transitions;
	private int stateCapacity;
	private int numStates;
	private final TIntSet initial = new TIntHashSet();
	
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
	
	public AbstractCompactSimpleNondet(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
		this.alphabet = alphabet;
		this.alphabetSize = alphabet.size();
		this.transitions = new TIntSet[stateCapacity * alphabetSize];
		
		this.resizeFactor = resizeFactor;
		this.stateCapacity = stateCapacity;
	}
	
	public void ensureCapacity(int newCapacity) {
		if(newCapacity <= stateCapacity)
			return;
		
		int newCap = (int)(stateCapacity * resizeFactor);
		if(newCap < newCapacity)
			newCap = newCapacity;
		
		TIntSet[] newTrans = new TIntSet[newCap * alphabetSize];
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

	
	public TIntCollection getIntInitialStates() {
		return initial;
	}
	
	
	public TIntCollection getIntTransitions(int state, I input) {
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
		TIntCollection successors = transitions[transIdx];
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
		TIntSet successors = transitions[transIdx];
		if(successors == null) {
			successors = new TIntHashSet();
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
	
	protected TIntSet successors(int transId) {
		TIntSet successors = transitions[transId];
		if(successors == null) {
			return EMPTY_SET;
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
		TIntList successors = new TIntArrayList(transitions.size());
		for(Integer succ : transitions) {
			successors.add(succ.intValue());
		}
		setTransitions(state, input, successors);
	}
	
	public void setTransitions(int state, I input, TIntCollection successors) {
		setTransitions(state, alphabet.getSymbolIndex(input), successors);
	}
	
	public void setTransitions(int state, int inputIdx, TIntCollection successors) {
		int transIdx = state * alphabetSize + inputIdx;
		TIntSet succs = transitions[transIdx];
		if(succs == null) {
			succs = new TIntHashSet(successors);
			transitions[transIdx] = succs;
		}
		else {
			succs.clear();
			succs.addAll(successors);
		}
	}

	@Override
	public Collection<? extends Integer> getTransitions(Integer state, I input) {
		return new TIntSetDecorator(getTransitions(state.intValue(), input));
	}
	
	public TIntSet getTransitions(int state, I input) {
		return getTransitions(state, alphabet.getSymbolIndex(input));
	}
	
	public TIntSet getTransitions(int state, int inputIdx) {
		return successors(state * alphabetSize + inputIdx);
	}

	@Override
	public Set<? extends Integer> getInitialStates() {
		return new TIntSetDecorator(initial);
	}

	
}
