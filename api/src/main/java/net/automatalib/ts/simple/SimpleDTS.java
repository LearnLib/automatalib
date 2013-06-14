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
package net.automatalib.ts.simple;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.ts.TransitionSystem;

/**
 * A simple deterministic transition system. In a deterministic transition system,
 * there exists in each state at most one successor state for each input symbol.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class
 * @param <I> input symbol class
 */
@ParametersAreNonnullByDefault
public interface SimpleDTS<S, I> extends SimpleTS<S, I> {
	
	/**
	 * Retrieves the initial state of this transition system.
	 * @return the initial state.
	 * @see TransitionSystem#getInitialStates()
	 */
	@Nullable
	public S getInitialState();
	
	/**
	 * Retrieves the successor state reachable by the given input symbol. 
	 * @param state the source state.
	 * @param input the input symbol.
	 * @return the successor state reachable by the given input symbol,
	 * or <code>null</code> if no state is reachable by this symbol.
	 * @see TransitionSystem#getSuccessors(Object, Object)
	 */
	@Nullable
	public S getSuccessor(S state, @Nullable I input);
	
	/**
	 * Retrieves the successor state reachable by the given sequence of
	 * input symbols.
	 * @param state the source state.
	 * @param input the input symbol.
	 * @return the successor state reachable by the given sequence of input
	 * symbols, or <code>null</code> if no state is reachable by this symbol.
	 * @see TransitionSystem#getSuccessors(Object, Iterable)
	 */
	@Nullable
	public S getSuccessor(S state, Iterable<? extends I> input);
	
	/**
	 * Retrieves the state reachable by the given sequence of input symbols
	 * from the initial state.
	 * @param input the input word.
	 * @return the state reachable by the given input word, or <code>null</code>
	 * if no state is reachable by this word.
	 * @see TransitionSystem#getStates(Iterable)
	 */
	@Nullable
	public S getState(Iterable<? extends I> input);
}
