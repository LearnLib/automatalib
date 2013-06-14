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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automata.FiniteAlphabetAutomaton;
import net.automatalib.automata.abstractimpl.AbstractMutableDeterministic;
import net.automatalib.automata.base.StateIDGrowingMapping;
import net.automatalib.automata.base.StateIDStaticMapping;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.dot.DefaultDOTHelperAutomaton;
import net.automatalib.automata.graphs.AbstractAutomatonGraph;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;

public abstract class AbstractCompactSimpleDet<I, SP> extends
		AbstractMutableDeterministic<Integer, I, Integer, SP, Void>
		implements UniversalGraph<Integer,TransitionEdge<I,Integer>,SP,TransitionEdge.Property<I,Void>>,
		FiniteAlphabetAutomaton<Integer,I,Integer>, StateIDs<Integer>,
		NodeIDs<Integer>, DOTPlottableGraph<Integer, TransitionEdge<I,Integer>> {

	public static final float DEFAULT_RESIZE_FACTOR = 1.5f;
	public static final int DEFAULT_INIT_CAPACITY = 11;
	
	private final Alphabet<I> alphabet;
	private final int alphabetSize;
	private int[] transitions;
	private int stateCapacity;
	private int numStates;
	private int initial = -1;
	private final float resizeFactor;
	
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
	
	
	public int getIntTransition(int state, I input) {
		int transId = state * alphabetSize + alphabet.getSymbolIndex(input);
		return transitions[transId];
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
	public StateIDs<Integer> stateIDs() {
		return this;
	}

	@Override
	public SP getNodeProperty(Integer node) {
		return getStateProperty(node);
	}

	@Override
	public TransitionEdge.Property<I, Void> getEdgeProperty(TransitionEdge<I, Integer> edge) {
		return new TransitionEdge.Property<I,Void>(edge.getInput(), null);
	}

	@Override
	public Collection<TransitionEdge<I, Integer>> getOutgoingEdges(Integer node) {
		return AbstractAutomatonGraph.getOutgoingEdges(this, node);
	}

	@Override
	public Integer getTarget(TransitionEdge<I, Integer> edge) {
		return edge.getTransition();
	}

	@Override
	public <V> MutableMapping<Integer, V> createStaticNodeMapping() {
		return new StateIDStaticMapping<>(this, numStates);
	}

	@Override
	public <V> MutableMapping<Integer, V> createDynamicNodeMapping() {
		return new StateIDGrowingMapping<>(this, this);
	}

	@Override
	public Collection<Integer> getNodes() {
		return getStates();
	}

	@Override
	public NodeIDs<Integer> nodeIDs() {
		return this;
	}

	@Override
	public GraphDOTHelper<Integer, TransitionEdge<I, Integer>> getGraphDOTHelper() {
		return new DefaultDOTHelperAutomaton<>(this);
	}

	@Override
	public int getNodeId(Integer node) {
		return node.intValue();
	}

	@Override
	public Integer getNode(int id) {
		return Integer.valueOf(id);
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

	protected static Integer wrapState(int id) {
		if(id < 0) {
			return null;
		}
		return Integer.valueOf(id);
	}
	
}
