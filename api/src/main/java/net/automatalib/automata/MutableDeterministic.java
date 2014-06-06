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
package net.automatalib.automata;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for a <i>mutable</i> deterministic automaton.
 * 
 * @author Malte Isberner 
 *
 * @param <S> state class.
 * @param <I> input symbol class.
 * @param <T> transition class.
 * @param <SP> state property.
 * @param <TP> transition property.
 */
@ParametersAreNonnullByDefault
public abstract interface MutableDeterministic<S,I,T,SP,TP> extends UniversalDeterministicAutomaton<S,I,T,SP,TP>,
	MutableAutomaton<S,I,T,SP,TP> {
	
	/**
	 * Sets the initial state to the given state. If the current initial state
	 * should be unset, {@code null} can be passed.
	 * @param state the new initial state, or {@code null}.
	 */
	public void setInitialState(@Nullable S state);
	
	/**
	 * Sets the transition for the given state and input symbol.
	 * @param state the source state
	 * @param input the triggering input symbol
	 * @param transition the transition
	 */
	public void setTransition(S state, @Nullable I input, @Nullable T transition);
	
	/**
	 * Sets the transition for the given state and input symbol to a newly
	 * created one.
	 * @param state the source state
	 * @param input the triggering input symbol
	 * @param successor the target state
	 * @param property the transition's property
	 */
	public void setTransition(S state, @Nullable I input, S successor, @Nullable TP property);
}
