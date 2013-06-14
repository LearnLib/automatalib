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

/**
 * A "universal" transition system, which captures the possibility to assign
 * properties to states and transitions.
 * 
 * Generally speaking, these properties capture characteristics which are in
 * general observable from the outside, but not captured by the
 * {@link TransitionSystem} interface. For example, neither is whether a state
 * is initial or not a state property, nor is a transition's successor a
 * transition property.
 *  
 * A common example are finite state acceptors (FSAs), such as deterministic
 * finite automata (DFAs). A state can be accepting or non-accepting, thus
 * the state property would likely be a {@link Boolean} signaling acceptance.
 * Transitions have are characterized by their successor state only, thus
 * the transition property would most adequately be realized by the {@link Void}
 * class.
 * 
 * In contrast, in a Mealy Machine do not distinguish between accepting or
 * rejecting states, but transitions generate output symbols. The state property
 * would therefore be {@link Void}, but the transition property would be the
 * output produced by this transition.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class
 * @param <I> input symbol class
 * @param <T> transition class
 * @param <SP> state property class
 * @param <TP> transition property class
 */
public interface UniversalTransitionSystem<S, I, T, SP, TP> extends
		TransitionSystem<S, I, T> {
	
	/**
	 * Retrieves the state property for the given state.
	 * @param state the state.
	 * @return the corresponding property.
	 */
	public SP getStateProperty(S state);
	
	/**
	 * Retrieves the transition property for the given state.
	 * @param transition the transition.
	 * @return the corresponding property.
	 */
	public TP getTransitionProperty(T transition);
}
