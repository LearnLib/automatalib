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
package net.automatalib.automata.abstractimpl;

import java.util.Collection;

import net.automatalib.automata.MutableDeterministic;


/**
 * Abstract base class for mutable deterministic automata.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class
 * @param <I> input symbol class
 * @param <T> transition class
 * @param <SP> state property class
 * @param <TP> transition property class
 */
public abstract class AbstractMutableDeterministic<S,I,T,SP,TP> extends
		AbstractDeterministicAutomaton<S,I,T> implements
		MutableDeterministic<S,I,T,SP,TP> {
	
	
	/**
	 * Provides a realization of {@link MutableDeterministic#addTransition(Object, Object, Object)}
	 * using {@link MutableDeterministic#getTransition(Object, Object)} and
	 * {@link MutableDeterministic#setTransition(Object, Object, Object)}.
	 * @see MutableDeterministic#addTransition(Object, Object, Object)
	 */
	public static <S,I,T,SP,TP> void addTransition(MutableDeterministic<S, I, T, SP, TP> $this,
			S state, I input, T transition) {
		T currTrans = $this.getTransition(state, input);
		if(currTrans != null)
			throw new IllegalStateException("Cannot add transition " + transition
					+ " to deterministic automaton: transition already defined for state "
					+ state + " and input " + input + ".");
		$this.setTransition(state, input, transition);
	}
	
	/**
	 * Provides a realization of {@link MutableDeterministic#removeTransition(Object, Object, Object)}
	 * using {@link MutableDeterministic#getTransition(Object, Object)} and
	 * {@link MutableDeterministic#setTransition(Object, Object, Object)}.
	 * @see MutableDeterministic#removeTransition(Object, Object, Object)
	 */
	public static <S,I,T,SP,TP> void removeTransition(MutableDeterministic<S, I, T, SP, TP> $this,
			S state, I input, T transition) {
		if(transition == null)
			return;
		T currTrans = $this.getTransition(state, input);
		if(transition.equals(currTrans))
			$this.setTransition(state, input, null);
	}
	
	/**
	 * Provides a realization of {@link MutableDeterministic#removeAllTransitions(Object, Object)}
	 * using {@link MutableDeterministic#setTransition(Object, Object, Object)}.
	 * @see MutableDeterministic#removeAllTransitions(Object, Object)
	 */
	public static <S,I,T,SP,TP> void removeAllTransitions(MutableDeterministic<S,I,T,SP,TP> $this, S state, I input) {
		$this.setTransition(state, input, null);
	}
	
	/**
	 * Provides a realization of {@link MutableDeterministic#setTransitions(Object, Object, Collection)}
	 * using {@link MutableDeterministic#setTransition(Object, Object, Object)}
	 * @see MutableDeterministic#setTransitions(Object, Object, Collection)
	 */
	public static <S,I,T,SP,TP> void setTransitions(MutableDeterministic<S,I,T,SP,TP> $this,
			S state, I input, Collection<T> transitions) {
		
		int num = transitions.size();
		if(num > 1) {
			throw new IllegalArgumentException("Deterministic automaton can not "
					+ "have multiple transitions for the same input symbol.");
		}
		
		T trans = (num > 0) ? transitions.iterator().next() : null;
		
		$this.setTransition(state, input, trans);
	}
	
	/**
	 * Provides a realization of {@link MutableDeterministic#setTransition(Object, Object, Object, Object)}
	 * using {@link MutableDeterministic#createTransition(Object, Object)} and
	 * {@link MutableDeterministic#setTransition(Object, Object, Object)}.
	 * @see MutableDeterministic#setTransition(Object, Object, Object, Object)
	 */
	public static <S,I,T,SP,TP> void setTransition(MutableDeterministic<S, I, T, SP, TP> $this,
			S state, I input, S successor, TP property) {
		T trans = $this.createTransition(successor, property);
		$this.setTransition(state, input, trans);
	}
	
	
	/**
	 * Provides a realization of {@link MutableDeterministic#setInitial(Object, boolean)}
	 * using {@link MutableDeterministic#getInitialState()} and
	 * {@link MutableDeterministic#setInitialState(Object)}.
	 * @see MutableDeterministic#setInitial(Object, boolean)
	 */
	public static <S,I,T,SP,TP> void setInitial(MutableDeterministic<S,I,T,SP,TP> $this,
			S state, boolean initial) {
		S currInitial = $this.getInitialState();
		if(state.equals(currInitial)) {
			if(!initial)
				$this.setInitialState(null);
		}
		else if(currInitial == null)
			$this.setInitialState(state);
		else
			throw new IllegalStateException("Cannot set state '" + state + "' as "
					+ "additional initial state (current initial state: '"
					+ currInitial + "'.");
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#addTransition(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void addTransition(S state, I input, T transition) {
		addTransition(this, state, input, transition);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#addTransition(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void addTransition(S state, I input, S successor, TP property) {
		AbstractMutableAutomaton.addTransition(this, state, input, successor, property);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#removeTransition(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void removeTransition(S state, I input, T transition) {
		removeTransition(this, state, input, transition);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#removeAllTransitions(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void removeAllTransitions(S state, I input) {
		removeAllTransitions(this, state, input);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#setInitial(java.lang.Object, boolean)
	 */
	@Override
	public void setInitial(S state, boolean initial) {
		setInitial(this, state, initial);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#setTransitions(java.lang.Object, java.lang.Object, java.util.Collection)
	 */
	@Override
	public void setTransitions(S state, I input, Collection<T> transitions) {
		setTransitions(this, state, input, transitions);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableDeterministic#setTransition(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setTransition(S state, I input, S succ, TP properties) {
		setTransition(this, state, input, succ, properties);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#addInitialState(java.lang.Object)
	 */
	@Override
	public S addInitialState(SP property) {
		return AbstractMutableAutomaton.addInitialState(this, property);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#addInitialState()
	 */
	@Override
	public S addInitialState() {
		return AbstractMutableAutomaton.addInitialState(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#addState()
	 */
	@Override
	public S addState() {
		return AbstractMutableAutomaton.addState(this);
	}
}
