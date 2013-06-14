/* Copyright (C) 2013-2014 TU Dortmund
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.commons.util.mappings.MapMapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.ts.PowersetViewTS;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.powerset.DirectPowersetDTS;


public abstract class AbstractTS<S, I, T> implements TransitionSystem<S, I, T> {
	
	/**
	 * Provides a realization of
	 * {@link TransitionSystem#getSuccessors(Object, Object)} using
	 * {@link TransitionSystem#getTransitions(Object, Object)} and
	 * {@link TransitionSystem#getSuccessor(Object)}.
	 * @see TransitionSystem#getSuccessors(Object, Object)
	 */
	public static <S,I,T> Set<S> getSuccessors(TransitionSystem<S, I, T> $this, S state, I input) {
		Collection<? extends T> transitions = $this.getTransitions(state, input);
		if(transitions.isEmpty()) {
			return Collections.emptySet();
		}
		Set<S> result = new HashSet<S>(transitions.size());
		for(T trans : transitions)
			result.add($this.getSuccessor(trans));
		return result;
	}
	
	/**
	 * Provides a realization of
	 * {@link TransitionSystem#getSuccessors(Object, Iterable)} using
	 * {@link TransitionSystem#getSuccessors(Collection, Iterable)}.
	 * @see TransitionSystem#getSuccessors(Object, Iterable)
	 */
	public static <S,I,T> Set<? extends S> getSuccessors(TransitionSystem<S, I, T> $this, S state, Iterable<? extends I> input) {
		return $this.getSuccessors(Collections.singleton(state), input);
	}
	
	/**
	 * Provides a realization of
	 * {@link TransitionSystem#getSuccessors(Collection, Iterable)} using
	 * {@link TransitionSystem#getSuccessors(Object, Object)}.
	 * @see TransitionSystem#getSuccessors(Collection, Iterable)
	 */
	public static <S,I,T> Set<? extends S> getSuccessors(TransitionSystem<S, I, T> $this, Collection<? extends S> states, Iterable<? extends I> input) {
		Set<S> current = new HashSet<S>(states);
		Set<S> succs = new HashSet<S>();
		
		for(I sym : input) {
			for(S state : current) {
				Set<? extends S> currSuccs = $this.getSuccessors(state, sym);
				succs.addAll(currSuccs);
			}
					
			Set<S> tmp = current;
			current = succs;
			succs = tmp;
			succs.clear();
		}
		
		return current;
	}
	
	/**
	 * Provides a realization of
	 * {@link TransitionSystem#getStates(Iterable)} using
	 * {@link TransitionSystem#getSuccessors(Collection, Iterable)} and
	 * {@link TransitionSystem#getInitialStates()}.
	 * @see TransitionSystem#getStates(Iterable)
	 */
	public static <S,I,T> Set<? extends S> getStates(TransitionSystem<S, I, T> $this, Iterable<? extends I> input) {
		return $this.getSuccessors($this.getInitialStates(), input);
	}
	
	/**
	 * Provides a realization of
	 * {@link TransitionSystem#powersetView()}.
	 * @see TransitionSystem#powersetView()
	 * @see DirectPowersetDTS
	 */
	public static <S,I,T> DirectPowersetDTS<S,I,T> powersetView(TransitionSystem<S,I,T> $this) {
		return new DirectPowersetDTS<S,I,T>($this);
	}
	
	
	public static <S,I,T,V> MutableMapping<S,V> createStaticStateMapping(TransitionSystem<S,I,T> $this) {
		return new MapMapping<>(new HashMap<S,V>());
	}
	
	public static <S,I,T,V> MutableMapping<S,V> createDynamicStateMapping(TransitionSystem<S,I,T> $this) {
		return new MapMapping<>(new HashMap<S,V>());
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleTS#getSuccessors(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Set<S> getSuccessors(S state, I input) {
		return getSuccessors(this, state, input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleTS#getSuccessors(java.lang.Object, java.lang.Iterable)
	 */
	@Override
	public Set<? extends S> getSuccessors(S state, Iterable<? extends I> input) {
		return getSuccessors(this, state, input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleTS#getSuccessors(java.util.Collection, java.lang.Iterable)
	 */
	@Override
	public Set<? extends S> getSuccessors(Collection<? extends S> states, Iterable<? extends I> input) {
		return getSuccessors(this, states, input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleTS#getStates(java.lang.Iterable)
	 */
	@Override
	public Set<? extends S> getStates(Iterable<? extends I> input) {
		return getStates(this, input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.TransitionSystem#powersetView()
	 */
	@Override
	public PowersetViewTS<?, I, ?, S, T> powersetView() {
		return powersetView(this);
	}
	
	
	@Override
	public <V> MutableMapping<S,V> createStaticStateMapping() {
		return createStaticStateMapping(this);
	}
	
	@Override
	public <V> MutableMapping<S,V> createDynamicStateMapping() {
		return createDynamicStateMapping(this);
	}
	

}
