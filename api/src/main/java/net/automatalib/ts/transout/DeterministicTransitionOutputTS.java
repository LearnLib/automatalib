/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.ts.transout;

import java.util.List;

import net.automatalib.automata.concepts.TransitionOutput;
import net.automatalib.ts.DeterministicTransitionSystem;

public interface DeterministicTransitionOutputTS<S, I, T, O> extends DeterministicTransitionSystem<S,I,T>, TransitionOutput<T, O> {
	
	/**
	 * Retrieves the output for the given input symbol in the given state.
	 * This is roughly equivalent to calling {@link #getTransitionOutput(Object)}
	 * on the transition returned by {@link #getTransition(Object, Object)}, however
	 * it should be noted that this function does not allow distinguishing between
	 * a <code>null</code> output and an undefined transition.
	 * 
	 * @param state the source state
	 * @param input the input symbol
	 * @return the output symbol (or <code>null</code> if the transition is undefined)
	 */
	public O getOutput(S state, I input);
	
	public boolean trace(Iterable<I> input, List<O> output);
	public boolean trace(S state, Iterable<I> input, List<O> output);
}
