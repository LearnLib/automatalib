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
package net.automatalib.automata.base.compact;

import java.util.Collection;

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
		return (initial != -1) ? Integer.valueOf(initial) : null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.DeterministicTransitionSystem#getTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Integer getTransition(Integer state, I input) {
		int transId = state.intValue() * alphabetSize + alphabet.getSymbolIndex(input);
		int succId = transitions[transId];
		if(succId == 0)
			return null;
		return succId - 1;
	}

	public void setInitialState(int state) {
		this.initial = state;
	}

	@Override
	public void setInitialState(Integer state) {
		setInitialState((state != null) ? state.intValue() : -1);
	}
	
	public void setTransition(int state, int inputIdx, int succ) {
		transitions[state * alphabetSize + inputIdx] = succ + 1;
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
		for(int i = 0; i < endIdx; i++)
			transitions[i] = 0;
		initial = -1;
	}


	public void removeAllTransitions(int state) {
		int base = state * alphabetSize;
		
		for(int i = 0; i < alphabetSize; i++)
			transitions[base++] = 0;
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
	
	public abstract void initState(int stateId, SP property);

	public int addIntState(SP property) {
		int stateId = numStates++;
		ensureCapacity(numStates);
		initState(stateId, property);
		return stateId;
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


	
}
