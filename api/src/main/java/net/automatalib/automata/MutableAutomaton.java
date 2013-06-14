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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A mutable automaton. This interface adds support for non-destructive modifications, i.e.,
 * adding and modifying states and transitions. If also removal of states and single transitions
 * (from the set of outgoing transitions) should be removed, then {@link ShrinkableAutomaton}
 * is the adequate interface.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class.
 * @param <I> input symbol class.
 * @param <T> transition class.
 * @param <SP> state property.
 * @param <TP> transition property.
 */
@ParametersAreNonnullByDefault
public abstract interface MutableAutomaton<S,I,T,SP,TP> 
        extends UniversalAutomaton<S, I, T, SP, TP> {

	/**
	 * Removes all states and transitions.
	 */
	public void clear();
	
	/**
	 * Adds a state to the automaton.
	 * @param property
	 * @return
	 */
	@Nonnull
	public S addState(@Nullable SP property);
	@Nonnull
	public S addInitialState(@Nullable SP property);
	
	@Nonnull
	public S addState();
	@Nonnull
	public S addInitialState();

    public void setInitial(S state, boolean initial);
	
	public void setStateProperty(S state, @Nullable SP property);
	public void setTransitionProperty(T transition, @Nullable TP property);
    
	@Nonnull
    public T createTransition(S successor, @Nullable TP properties);
    
    public void addTransition(S state, @Nullable I input, T transition);
    public void addTransitions(S state, @Nullable I input, Collection<? extends T> transitions);
    public void setTransitions(S state, @Nullable I input, Collection<? extends T> transitions);
    public void removeTransition(S state, @Nullable I input, T transition);
    public void removeAllTransitions(S state, @Nullable I input);
    public void removeAllTransitions(S state);
    
    @Nonnull
    public T addTransition(S state, @Nullable I input, S successor, @Nullable TP properties);
    
    @Nonnull
    public T copyTransition(T trans, S succ);
}
