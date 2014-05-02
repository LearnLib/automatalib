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
import net.automatalib.automata.lts.MutableLTS;
import net.automatalib.words.Alphabet;

/**
 * Abstract base class for automata nondeterministic mutable labeled transition system.
 * 
 * @author Michele Volpato
 *
 * @param <L> label class
 */
public abstract class AbstractMutableLTS<L> extends AbstractMutableAutomaton<Integer, L, Integer, Void, Void> 
	implements MutableLTS<L>, DOTPlottableAutomaton<Integer, L, Integer> {

	protected final Alphabet<L> alphabet;
	protected final int alphabetSize;
	protected Map<Integer, List<TransitionLTS>> states = new HashMap<Integer, List<TransitionLTS>>();

	protected int nextState = 0;
	protected int nextTransition = 0;
	protected Map<Integer, TransitionLTS> transitions = new HashMap<Integer, TransitionLTS>();
	protected Set<Integer> initialStatesIds = new HashSet<Integer>();;
	
	protected class TransitionLTS{
		private Integer transitionId;
		private L label;
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
		public L getLabel() {
			return label;
		}
		public void setLabel(L label) {
			this.label = label;
		}
		public Integer getNextState() {
			return nextState;
		}
		public void setNextState(Integer nextState) {
			this.nextState = nextState;
		}
	
	}
	
	public AbstractMutableLTS(Alphabet<L> alphabet) {
		this.alphabet = alphabet;
		this.alphabetSize = alphabet.size();	
	}

	@Override
	public Collection<? extends Integer> getTransitions(Integer state, L label) {
		List<Integer> edgeIds = new ArrayList<Integer>();
		List<TransitionLTS> edges = states.get(state);
		for(TransitionLTS edge : edges){
			if(edge.getLabel().equals(label)){
				edgeIds.add(edge.getTransitionId());
			}
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
		TransitionLTS trans = new TransitionLTS(transitionId);
		trans.setNextState(successor);
		transitions.put(transitionId, trans);
		nextTransition++;
	
		return transitionId;
	}
	
	public Integer createTransition(Integer successor) {
		return this.createTransition(successor, null);
	}

	@Override
	public void setTransitions(Integer state, L label,
			Collection<? extends Integer> transitions) {
		if(states.containsKey(state) && alphabet.contains(label)){
			for(Integer transitionId : transitions){
				TransitionLTS transition = this.transitions.get(transitionId);
				if(transition != null){
					transition.setLabel(label);
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
	public Alphabet<L> getInputAlphabet() {
		return alphabet;
	}
	 
	/**
	 * Print a string representation of the LTS in the form:
	 * start label target
	 * 
	 * for each transition.
	 * 
	 */
	public String toString(){
		String lts = "LTS: \n";
		for(Integer state : states.keySet()){
			for(TransitionLTS trans: states.get(state)){
				lts = lts + state + " " + trans.getLabel().toString() + " " + trans.getNextState() + "\n";
			}		
		}
		return lts;
	}

}
