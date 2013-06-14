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
package net.automatalib.ts.abstractimpl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.TransitionSystem;


public abstract class AbstractDTS<S, I, T> extends AbstractTS<S, I, T> implements
		DeterministicTransitionSystem<S, I, T> {
	
	/**
	 * Provides a realization of
	 * {@link DeterministicTransitionSystem#getInitialStates()} using
	 * {@link DeterministicTransitionSystem#getInitialState()}.
	 * @see TransitionSystem#getInitialStates()
	 */
	public static <S,I,T> Set<S> getInitialStates(DeterministicTransitionSystem<S, I, T> $this) {
		S init = $this.getInitialState();
		if(init == null)
			return Collections.emptySet();
		return Collections.singleton(init);
	}
	
	/**
	 * Provides a realization of
	 * {@link DeterministicTransitionSystem#getTransitions(Object, Object)}
	 * using {@link DeterministicTransitionSystem#getTransition(Object, Object)}.
	 * @see TransitionSystem#getTransitions(Object, Object)
	 */
	public static <S,I,T> Set<T> getTransitions(DeterministicTransitionSystem<S, I, T> $this, S state, I input) {
		T trans = $this.getTransition(state, input);
		if(trans == null)
			return Collections.emptySet();
		return Collections.singleton(trans);
	}
	
	/**
	 * Provides a realization of
	 * {@link DeterministicTransitionSystem#getSuccessor(Object)} using
	 * {@link DeterministicTransitionSystem#getTransition(Object, Object)}
	 * and {@link DeterministicTransitionSystem#getSuccessor(Object)}.
	 * @see DeterministicTransitionSystem#getSuccessor(Object)
	 */
	public static <S,I,T> S getSuccessor(DeterministicTransitionSystem<S, I, T> $this, S state, I input) {
		T trans = $this.getTransition(state, input);
		if(trans == null)
			return null;
		return $this.getSuccessor(trans);
	}
	
	/**
	 * Provides a realization of
	 * {@link DeterministicTransitionSystem#getSuccessor(Object, Iterable)} using
	 * {@link DeterministicTransitionSystem#getSuccessor(Object, Object)}.
	 * @see DeterministicTransitionSystem#getSuccessor(Object, Iterable)
	 */
	public static <S,I,T> S getSuccessor(DeterministicTransitionSystem<S, I, T> $this, S state, Iterable<I> input) {
		S curr = state;
		Iterator<I> it = input.iterator();
		
		while(curr != null && it.hasNext()) {
			I sym = it.next();
			curr = $this.getSuccessor(curr, sym);
		}
		
		return curr;
	}
	
	/**
	 * Provides a realization of
	 * {@link DeterministicTransitionSystem#getState(Iterable)} using
	 * {@link DeterministicTransitionSystem#getSuccessor(Object, Iterable)}
	 * and {@link DeterministicTransitionSystem#getInitialState()}.
	 * @see {@link DeterministicTransitionSystem#getState(Iterable)}
	 */
	public static <S,I,T> S getState(DeterministicTransitionSystem<S, I, T> $this, Iterable<I> input) {
		return $this.getSuccessor(
				$this.getInitialState(),
				input);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleTS#getInitialStates()
	 */
	@Override
	public Set<S> getInitialStates() {
		return getInitialStates(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.TransitionSystem#getTransitions(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Collection<T> getTransitions(S state, I input) {
		return getTransitions(this, state, input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleDTS#getSuccessor(java.lang.Object, java.lang.Object)
	 */
	@Override
	public S getSuccessor(S state, I input) {
		return getSuccessor(this, state, input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleDTS#getSuccessor(java.lang.Object, java.lang.Iterable)
	 */
	@Override
	public S getSuccessor(S state, Iterable<I> input) {
		return getSuccessor(this, state, input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleDTS#getState(java.lang.Iterable)
	 */
	@Override
	public S getState(Iterable<I> input) {
		return getState(this, input);
	}

}
