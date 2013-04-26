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
package net.automatalib.automata.abstractimpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.automata.MutableAutomaton;


/**
 * Abstract base class for mutable automata.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class
 * @param <I> input symbol class
 * @param <T> transition class
 * @param <SP> state property class
 * @param <TP> transition property class
 */
public abstract class AbstractMutableAutomaton<S, I, T,SP,TP> extends AbstractAutomaton<S,I,T>
		implements MutableAutomaton<S,I,T,SP,TP> {

	
	/**
	 * Provides a realization of {@link MutableAutomaton#addInitialState(Object)} using
	 * {@link MutableAutomaton#addState(Object)} and {@link MutableAutomaton#setInitial(Object, boolean)}.
	 * @see MutableAutomaton#addInitialState(Object)
	 */
	public static <S,I,T,SP,TP> S addInitialState(MutableAutomaton<S, I, T, SP, TP> $this, SP property) {
		S state = $this.addState(property);
		$this.setInitial(state, true);
		return state;
	}
	
	/**
	 * Provides a realization of {@link MutableAutomaton#addInitialState()} using
	 * {@link MutableAutomaton#addInitialState(Object)}.
	 * @see MutableAutomaton#addInitialState()
	 */
	public static <S,I,T,SP,TP> S addInitialState(MutableAutomaton<S,I,T,SP,TP> $this) {
		return $this.addInitialState(null);
	}
	
	/**
	 * Provides a realization of {@link MutableAutomaton#addState()} using
	 * {@link MutableAutomaton#addState(Object)}.
	 * @see MutableAutomaton#addState()
	 */
	public static <S,I,T,SP,TP> S addState(MutableAutomaton<S,I,T,SP,TP> $this) {
		return $this.addState(null);
	}
	
	/**
	 * Provides a realization of {@link MutableAutomaton#addTransition(Object, Object, Object)}
	 * using {@link MutableAutomaton#getTransitions(Object, Object)} and
	 * {@link MutableAutomaton#setTransitions(Object, Object, java.util.Collection)}.
	 * @see MutableAutomaton#addTransition(Object, Object, Object)
	 */
	public static <S,I,T,SP,TP> void addTransition(MutableAutomaton<S, I, T, SP, TP> $this, S state, I input, T transition) {
		Set<T> transitions = new HashSet<T>($this.getTransitions(state, input));
		if(!transitions.add(transition))
			return;
		$this.setTransitions(state, input, transitions);
	}
	
	/**
	 * Provides a realization of {@link MutableAutomaton#addTransition(Object, Object, Object, Object)}
	 * using {@link MutableAutomaton#createTransition(Object, Object)} and
	 * {@link MutableAutomaton#addTransition(Object, Object, Object)}.
	 * @see MutableAutomaton#addTransition(Object, Object, Object, Object)
	 */
	public static <S,I,T,SP,TP> void addTransition(MutableAutomaton<S, I, T, SP, TP> $this,
			S state, I input, S succ, TP property) {
		T trans = $this.createTransition(succ, property);
		$this.addTransition(state, input, trans);
	}
	
	/**
	 * Provides a realization of {@link MutableAutomaton#removeTransition(Object, Object, Object)}
	 * using {@link MutableAutomaton#getTransitions(Object, Object)} and
	 * {@link MutableAutomaton#setTransitions(Object, Object, java.util.Collection)}
	 * @see MutableAutomaton#removeTransition(Object, Object, Object)
	 */
	public static <S,I,T,SP,TP> void removeTransition(MutableAutomaton<S, I, T, SP, TP> $this, S state, I input, T transition) {
		Set<T> transitions = new HashSet<T>($this.getTransitions(state, input));
		if(!transitions.remove(transition))
			return;
		$this.setTransitions(state, input, transitions);
	}
	
	/**
	 * Provides a realization of {@link MutableAutomaton#removeAllTransitions(Object, Object)}
	 * using {@link MutableAutomaton#setTransitions(Object, Object, java.util.Collection)}
	 */
	public static <S,I,T,SP,TP> void removeAllTransitions(MutableAutomaton<S,I,T,SP,TP> $this, S state, I input) {
		$this.setTransitions(state, input, Collections.<T>emptySet());
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////

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
	public void addTransition(S state, I input, S successor, TP properties) {
		addTransition(this, state, input, successor, properties);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#addInitialState(java.lang.Object)
	 */
	@Override
	public S addInitialState(SP property) {
		return addInitialState(this, property);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#addState()
	 */
	@Override
	public S addState() {
		return addState(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#addInitialState()
	 */
	@Override
	public S addInitialState() {
		return addInitialState(this);
	}
}
