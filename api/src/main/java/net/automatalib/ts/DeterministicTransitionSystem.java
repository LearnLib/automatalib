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
package net.automatalib.ts;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.ts.simple.SimpleDTS;


/**
 * Deterministic transition system. Like a {@link TransitionSystem}, but in each state
 * there may exist at most one transition for each input symbol.
 * 
 * @author Malte Isberner
 *
 * @param <S> state class
 * @param <I> input symbol class
 * @param <T> transition class
 */
@ParametersAreNonnullByDefault
public interface DeterministicTransitionSystem<S, I, T> extends
		TransitionSystem<S, I, T>, SimpleDTS<S,I> {

	/**
	 * Retrieves the transition triggered by the given input symbol.
	 * @param state the source state.
	 * @param input the input symbol.
	 * @return the transition triggered by the given input symbol, or
	 * <code>null</code> if no transition is triggered.
	 * @see TransitionSystem#getTransitions(Object, Object)
	 */
	@Nullable
	public T getTransition(S state, @Nullable I input);
	
	
}
