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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.automata.base.compact;

import java.util.Collection;

import net.automatalib.automata.abstractimpl.AbstractMutableDeterministic;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;

public abstract class AbstractCompactDeterministic<I, T, SP, TP> extends
		AbstractMutableDeterministic<Integer, I, T, SP, TP> implements StateIDs<Integer> {

	public static final float DEFAULT_RESIZE_FACTOR = 1.5f;
	public static final int DEFAULT_INIT_CAPACITY = 11;
	
	protected final Alphabet<I> alphabet;
	protected final int alphabetSize;
	protected Object[] transitions;
	protected int stateCapacity; 
	protected int numStates;
	protected int initial = -1;
	protected final float resizeFactor;
	
	
	protected static final int getId(Integer id) {
		return (id != null) ? id.intValue() : -1;
	}
	
	
	
	protected static final Integer makeId(int id) {
		return (id != -1) ? Integer.valueOf(id) : null;
	}
	
	public AbstractCompactDeterministic(Alphabet<I> alphabet) {
		this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
	}
	
	public AbstractCompactDeterministic(Alphabet<I> alphabet, int stateCapacity) {
		this(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
	}
	
	public AbstractCompactDeterministic(Alphabet<I> alphabet, float resizeFactor) {
		this(alphabet, DEFAULT_INIT_CAPACITY, resizeFactor);
	}
	
	public AbstractCompactDeterministic(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
		this.alphabet = alphabet;
		this.alphabetSize = alphabet.size();
		this.transitions = new Object[stateCapacity * alphabetSize];
		this.resizeFactor = resizeFactor;
		this.stateCapacity = stateCapacity;
	}
	
	protected void ensureCapacity() {
	}
	
	public final void ensureCapacity(int newCapacity) {
		if(newCapacity <= stateCapacity)
			return;
		
		int newCap = (int)(stateCapacity * resizeFactor);
		if(newCap < newCapacity)
			newCap = newCapacity;
		
		Object[] newTrans = new Object[newCap * alphabetSize];
		System.arraycopy(transitions, 0, newTrans, 0, stateCapacity * alphabetSize);
		this.transitions = newTrans;
		this.stateCapacity = newCap;
		ensureCapacity();
	}
	
	public void setInitialState(int stateId) {
		initial = stateId;
	}
	
	@Override
	public void setInitialState(Integer state) {
		setInitialState(getId(state));
	}
	
	public void setTransition(int state, int inputIdx, T trans) {
		transitions[state * alphabetSize + inputIdx] = trans;
	}
	
	@Override
	public void setTransition(Integer state, I input, T transition) {
		setTransition(getId(state), alphabet.getSymbolIndex(input), transition);
	}
	
	
	public abstract T createTransition(int succId, TP property);
	
	@Override
	public T createTransition(Integer succ, TP property) {
		return createTransition(getId(succ), property);
	}
	
	@Override
	public T copyTransition(T trans, Integer succ) {
		return copyTransition(trans, getId(succ));
	}
	
	public abstract T copyTransition(T trans, int succId);
	
	public abstract int getIntSuccessor(T transition);

	@Override
	public final Integer getSuccessor(T transition) {
		return makeId(getIntSuccessor(transition));
	}

	@Override
	public Collection<Integer> getStates() {
		return CollectionsUtil.rangeList(0, numStates);
	}

	@Override
	public Integer getState(int id) {
		return Integer.valueOf(id);
	}

	@Override
	public int getStateId(Integer state) {
		return state.intValue();
	}

	public int getIntInitialState() {
		return initial;
	}
	
	@Override
	public Integer getInitialState() {
		return makeId(initial);
	}
	
	@SuppressWarnings("unchecked")
	public T getTransition(int stateId, int inputIdx) {
		return (T)transitions[stateId * alphabetSize + inputIdx];
	}
	
	public T getTransition(int stateId, I input) {
		return getTransition(stateId, alphabet.getSymbolIndex(input));
	}

	@Override
	public T getTransition(Integer state, I input) {
		return getTransition(getId(state), alphabet.getSymbolIndex(input));
	}

	public abstract SP getStateProperty(int stateId);
	
	@Override
	public SP getStateProperty(Integer state) {
		return getStateProperty(getId(state));
	}

	@Override
	public void clear() {
		int endIdx = numStates * alphabetSize;
		numStates = 0;
		for(int i = 0; i < endIdx; i++)
			transitions[i] = null;
	}
	
	protected final int createState() {
		int newState = numStates++;
		ensureCapacity(numStates);
		return newState;
	}
	
	public int addIntState(SP property) {
		int newState = createState();
		setStateProperty(newState, property);
		return newState;
	}

	@Override
	public Integer addState(SP property) {
		return addIntState(property);
	}

	public abstract void setStateProperty(int state, SP property);
	
	@Override
	public void setStateProperty(Integer state, SP property) {
		setStateProperty(state.intValue(), property);
	}

	@Override
	public void removeAllTransitions(Integer state) {
		int base = state.intValue() * alphabetSize;
		for(int i = 0; i < alphabetSize; i++)
			transitions[base++] = null;
	}
	
	
	@Override
	public StateIDs<Integer> stateIDs() {
		return this;
	}

}
