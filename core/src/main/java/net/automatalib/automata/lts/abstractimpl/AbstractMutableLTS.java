/* Copyright (C) 2014 AutomataLib
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
package net.automatalib.automata.lts.abstractimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.automata.abstractimpl.AbstractMutableAutomaton;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.lts.MutableLTS;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;

/**
 * Abstract base class for automata nondeterministic mutable labeled transition system.
 * 
 * @author Michele Volpato
 *
 * @param <I> input symbol class
 */
public abstract class AbstractMutableLTS<I> extends AbstractMutableAutomaton<Integer, I, Integer, Void, Void> 
	implements MutableLTS<I>, DOTPlottableAutomaton<Integer, I, Integer> {

	protected final Alphabet<I> alphabet;
	protected final int alphabetSize;
	protected List<StateLTS> states;
	protected List<Integer> statesIds;
	protected int nextState = 0;
	
	protected List<Integer> initialStatesIds;
	protected List<Integer> transitionsIds;
	protected Map<Integer, I> transitionLabels;
	protected Map<Integer, Integer> transitions;
	
	protected class StateLTS {
		private boolean initial;
		private Integer stateId;
		private List<TransitionLTS> transitions;
		
		public StateLTS(Integer id){
			this(id, false, new ArrayList<TransitionLTS>());	
		}
		
		public StateLTS(Integer id, boolean initial){
			this(id, initial, new ArrayList<TransitionLTS>());	
		}

		public StateLTS(Integer id, boolean initial, List<TransitionLTS> edges){
			stateId = id;
			transitions = edges;
			this.initial = initial;
		}
		
		public Integer getStateId(){
			return stateId;
		}
		
		public Integer getSuccessor(I label){
			return transitions.get(label);
		}
	}
	
	protected class TransitionLTS {
		private Integer transitionId;
		private I label;
		private Integer successorState;
	
	}
	
	public AbstractMutableLTS(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
		this.alphabetSize = alphabet.size();
		this.states = new ArrayList<StateLTS>();
	}

	@Override
	public Collection<? extends Integer> getTransitions(Integer state, I input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSuccessor(Integer transition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Integer> getInitialStates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Integer> getStates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void getStateProperty(Integer state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void getTransitionProperty(Integer transition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer addState(Void property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInitial(Integer state, boolean initial) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStateProperty(Integer state, Void property) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTransitionProperty(Integer transition, Void property) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer createTransition(Integer successor, Void properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTransitions(Integer state, I input,
			Collection<? extends Integer> transitions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllTransitions(Integer state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer copyTransition(Integer trans, Integer succ) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Alphabet<I> getInputAlphabet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphDOTHelper<Integer, TransitionEdge<I, Integer>> getDOTHelper() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
