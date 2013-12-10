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
package net.automatalib.ts;

import java.util.Collection;
import java.util.Set;

import net.automatalib.ts.simple.SimpleTS;

/**
 * Transition system interface. This interface extends {@link SimpleTS} by introducing
 * the concept of inspectable <i>transitions</i>, allowing to associate other information
 * apart from the successor state with each transition.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class
 * @param <I> input symbol class
 * @param <T> transition class
 */
public interface TransitionSystem<S, I, T> extends SimpleTS<S,I> {
	
	/**
	 * Retrieves the transitions that can be triggered by the given
	 * input symbol.
	 * @param state the source state.
	 * @param input the input symbol.
	 * @return the transitions, or <code>null</code> if no transitions
	 * are triggered by this input symbol.
	 */
	public Collection<? extends T> getTransitions(S state, I input);
	
	/**
	 * Retrieves the successor state of a given transition.
	 * @param transition the transition.
	 * @return the successor state.
	 */
	public S getSuccessor(T transition);
	
	/**
	 * Retrieves a "powerset view" of this transition system.
	 * @return a powerset view of this transition system.
	 */
	public DeterministicTransitionSystem<? extends Set<S>, I, ? extends Collection<T>>
		powersetView();
}
