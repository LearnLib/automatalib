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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.lts.MutableLTS;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.ts.PowersetViewTS;
import net.automatalib.words.Alphabet;

/**
 * A nondeterministic labelled transition system.
 * 
 * @author Michele Volpato
 *
 * @param <S> state class
 * @param <I> input symbol class
 */
public class InputOutputLTS<S, I> implements MutableLTS<S, I>,
		DOTPlottableAutomaton<S, I, S> {

	/**
	 * 
	 */
	public InputOutputLTS() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Collection<? extends S> getTransitions(S state, I input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public S getSuccessor(S transition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PowersetViewTS<?, I, ?, S, S> powersetView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends S> getInitialStates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends S> getSuccessors(S state, I input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends S> getSuccessors(S state, Iterable<? extends I> input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends S> getSuccessors(Collection<? extends S> states,
			Iterable<? extends I> input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends S> getStates(Iterable<? extends I> input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V> MutableMapping<S, V> createStaticStateMapping() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V> MutableMapping<S, V> createDynamicStateMapping() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<S> getStates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public StateIDs<S> stateIDs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<S> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void getStateProperty(S state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void getTransitionProperty(S transition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public S addState(Void property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public S addInitialState(Void property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public S addState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public S addInitialState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInitial(S state, boolean initial) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStateProperty(S state, Void property) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTransitionProperty(S transition, Void property) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public S createTransition(S successor, Void properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTransition(S state, I input, S transition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTransitions(S state, I input,
			Collection<? extends S> transitions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTransitions(S state, I input,
			Collection<? extends S> transitions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTransition(S state, I input, S transition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllTransitions(S state, I input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllTransitions(S state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public S addTransition(S state, I input, S successor, Void properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public S copyTransition(S trans, S succ) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Alphabet<I> getInputAlphabet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphDOTHelper<S, TransitionEdge<I, S>> getDOTHelper() {
		// TODO Auto-generated method stub
		return null;
	}

}
