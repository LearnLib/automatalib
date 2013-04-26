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
package net.automatalib.ts.simple;

import java.util.Collection;
import java.util.Set;

import net.automatalib.commons.util.mappings.MutableMapping;

/**
 * A simple transition system. A transition system is a (not necessarily finite) collection
 * of states. For an arbitrary input symbol, each state has a set of successors.
 * 
 * @author Malte Isberner <malte.isberner@cs.uni-dortmund.de>
 *
 * @param <S> state class.
 * @param <I> symbol class.
 */
public interface SimpleTS<S, I> {
	
	/**
	 * Retrieves the set of initial states of the transition system.
	 * @return the initial states.
	 */
	public Set<S> getInitialStates();
	
	/**
	 * Retrieves the set of successors for the given input symbol. 
	 * 
	 * @param state the source state.
	 * @param input the input symbol.
	 * @return the set of successors reachable by this input, or
	 * <code>null</code> if no successor states are reachable by this input.
	 */
	public Set<S> getSuccessors(S state, I input);
	
	/**
	 * Retrieves the set of successors for the given sequence of input symbols.
	 * 
	 * @param state the source state.
	 * @param input the sequence of input symbols.
	 * @return the set of successors reachable by this input, or
	 * <code>null</code> if no successor states are reachable by this input.
	 */
	public Set<S> getSuccessors(S state, Iterable<I> input);
	
	/**
	 * Retrieves the set of all successors that can be reached from any
	 * of the given source states by the specified sequence of input symbols.
	 *  
	 * @param states the source states.
	 * @param input the sequence of input symbols.
	 * @return the set of successors reachable by this input, or <code>null</code>
	 * if no successor states are reachable.
	 */
	public Set<S> getSuccessors(Collection<S> states, Iterable<I> input);

	/**
	 * Retrieves the set of all states reachable by the given sequence of input
	 * symbols from an initial state. Calling this method is equivalent to
	 * <code>getSuccessors(getInitialStates(), input)</code>.
	 * 
	 * @param input the sequence of input symbols.
	 * @return the set of states reachable by this input from an initial state,
	 * or <code>null</code> if no successor state is reachable.
	 */
	public Set<S> getStates(Iterable<I> input);
	
	/**
	 * Creates a {@link MutableMapping} allowing to associate arbitrary data
	 * with this transition system's states. The returned mapping is however
	 * only guaranteed to work correctly if the transition system is not
	 * modified.
	 * @return the mutable mapping
	 */
	public <V> MutableMapping<S,V> createStaticStateMapping();
	
	/**
	 * Creates a {@link MutableMapping} allowing to associate arbitrary data
	 * with this transition system's states. The returned mapping maintains
	 * the association even when the transition system is modified.
	 * @return the mutable mapping
	 */
	public <V> MutableMapping<S,V> createDynamicStateMapping();
	
}
