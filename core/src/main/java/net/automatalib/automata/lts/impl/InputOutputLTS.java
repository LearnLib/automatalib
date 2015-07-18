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
package net.automatalib.automata.lts.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.concepts.OutputAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.dot.DOTHelperLTS;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.lts.MutableLTS;
import net.automatalib.automata.lts.abstractimpl.AbstractMutableLTS;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.InputOutputLabel;

/**
 * A nondeterministic input output labelled transition system.
 * Transitions leaving the same state, with the same label and reaching the same
 * state, are merged together (no probabilistic approach).
 * 
 * @author Michele Volpato
 *
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
public class InputOutputLTS<I,O> extends AbstractMutableLTS<InputOutputLabel>  implements MutableLTS<InputOutputLabel>,
		DOTPlottableAutomaton<Integer, InputOutputLabel, Integer>, SuffixOutput<InputOutputLabel,InputOutputLabel> {

	public InputOutputLTS(Alphabet<InputOutputLabel> alphabet) {
		super(alphabet);
	}

	@Override
	public Void getStateProperty(Integer state) {
		// no property
		return null;
	}

	@Override
	public Void getTransitionProperty(Integer transition) {
		// no property
		return null;
	}

	@Override
	public void setStateProperty(Integer state, Void property) {
		// no property
		
	}

	@Override
	public void setTransitionProperty(Integer transition, Void property) {
		// no property
		
	}

	@Override
	public GraphDOTHelper<Integer, TransitionEdge<InputOutputLabel, Integer>> getDOTHelper() {
		return new DOTHelperLTS<>(this);
	}
	
	//Methods avoiding multiple transitions with the same label and target state.
	
	@Override
	public void setTransitions(Integer state, InputOutputLabel label,
			Collection<? extends Integer> transitions) {
		List<Integer> tempTrans = new ArrayList<Integer>();	
		Collection<? extends Integer> existingTransitions = this.getTransitions(state, label);
		
		for(Integer transitionId : transitions){
			TransitionLTS transition = this.transitions.get(transitionId);
			if(transition == null) continue;
			boolean transitionAlreadyPresent = false;
			for(Integer existingTransition : existingTransitions){
				if(this.transitions.get(existingTransition).getNextState().equals(transition.getNextState())){
					transitionAlreadyPresent = true;
					break;
				}
			}
			if(!transitionAlreadyPresent){
				tempTrans.add(transitionId);
			}
			else{
				// a transition has been created, but it is not used
				this.removeTransitionId(transitionId);
			}
		}
		super.setTransitions(state, label, tempTrans);
	}

	private boolean removeTransitionId(Integer transitionId) {
		// If there is no state listing the transition transitionId, then remove
		// transitionId from the list of transitions.
		// Return true if the transition has been removed
		TransitionLTS transition = transitions.get(transitionId);
		for(List<TransitionLTS> transitionsInStates : states.values()){
			if(transitionsInStates.contains(transition)){
				return false;
			}
		}
		return transitions.remove(transitionId) != null;
	}

	@Override
	public InputOutputLabel computeOutput(Iterable<? extends InputOutputLabel> input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputOutputLabel computeSuffixOutput(Iterable<? extends InputOutputLabel> prefix,
			Iterable<? extends InputOutputLabel> suffix) {
		// TODO Auto-generated method stub
		return null;
	}
}
