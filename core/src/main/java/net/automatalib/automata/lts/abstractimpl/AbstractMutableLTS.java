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
import java.util.HashSet;
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
 * @param <I> label class
 */
public abstract class AbstractMutableLTS<I> extends AbstractMutableAutomaton<Integer, I, Integer, Void, Void> 
	implements MutableLTS<I>, DOTPlottableAutomaton<Integer, I, Integer> {

	protected final Alphabet<I> alphabet;
	protected final int alphabetSize;
	protected Map<Integer, List<TransitionLTS>> states = new HashMap<Integer, List<TransitionLTS>>();

	protected int nextState = 0;
	protected int nextTransition = 0;
	protected Map<Integer, TransitionLTS> transitions = new HashMap<Integer, TransitionLTS>();
	protected Set<Integer> initialStatesIds = new HashSet<Integer>();;
	
	protected class TransitionLTS{
		private Integer transitionId;
		private I label;
		private Integer nextState;
		
		public TransitionLTS(Integer transitionId) {
			this.transitionId = transitionId;
		}
		
		public Integer getTransitionId() {
			return transitionId;
		}
		public void setTransitionId(Integer transitionId) {
			this.transitionId = transitionId;
		}
		public I getLabel() {
			return label;
		}
		public void setLabel(I label) {
			this.label = label;
		}
		public Integer getNextState() {
			return nextState;
		}
		public void setNextState(Integer nextState) {
			this.nextState = nextState;
		}
	
	}
	
	public AbstractMutableLTS(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
		this.alphabetSize = alphabet.size();	
	}

	@Override
	public Collection<? extends Integer> getTransitions(Integer state, I input) {
		List<Integer> edgeIds = new ArrayList<Integer>();
		List<TransitionLTS> edges = states.get(state);
		for(TransitionLTS edge : edges){
			edgeIds.add(edge.getTransitionId());
		}
		return edgeIds;
	}

	@Override
	public Integer getSuccessor(Integer transition) {
		return (transitions.get(transition)).getNextState();
	}

	@Override
	public Set<? extends Integer> getInitialStates() {
		return initialStatesIds;
	}

	@Override
	public Collection<Integer> getStates() {
		return states.keySet();
	}

	@Override
	public void clear() {
		states = new HashMap<Integer, List<TransitionLTS>>();
		nextState = 0;
		nextTransition = 0;
		transitions = new HashMap<Integer, TransitionLTS>();
		initialStatesIds = new HashSet<Integer>();
	}

	@Override
	public Integer addState(Void property) {

		Integer stateId = nextState;
		states.put(stateId, new ArrayList<TransitionLTS>());
		nextState++;
		
		return stateId;
	}
	
	public Integer addState() {
		return this.addState(null);
	}

	@Override
	public void setInitial(Integer state, boolean initial) {
		if(states.containsKey(state)){
			if(initial){
				initialStatesIds.add(state);
			}
			else {
				initialStatesIds.remove(state);
			}
		}
	}

	@Override
	public Integer createTransition(Integer successor, Void properties) {
		
		Integer transitionId = nextTransition;
		transitions.put(transitionId, new TransitionLTS(transitionId));
		transitionId++;
	
		return transitionId;
	}
	
	public Integer createTransition(Integer successor) {
		return this.createTransition(successor, null);
	}

	@Override
	public void setTransitions(Integer state, I input,
			Collection<? extends Integer> transitions) {
		if(states.containsKey(state) && alphabet.contains(input)){
			for(Integer transitionId : transitions){
				TransitionLTS transition = this.transitions.get(transitionId);
				if(transition != null){
					transition.setLabel(input);
					states.get(state).add(transition);
				}
			}
		}
	}

	@Override
	public void removeAllTransitions(Integer state) {
		for(TransitionLTS transition : states.get(state)){
			transitions.remove(transition.getTransitionId());
		}
		states.remove(state);
		states.put(state, new ArrayList<TransitionLTS>());
	}

	@Override
	public Integer copyTransition(Integer trans, Integer succ) {
		Integer newTransitionId = createTransition(succ);
		TransitionLTS newTrans = transitions.get(newTransitionId);
		TransitionLTS oldTrans = transitions.get(trans);
		newTrans.setLabel(oldTrans.getLabel());
		return newTransitionId;
	}

	@Override
	public Alphabet<I> getInputAlphabet() {
		return alphabet;
	}

}
