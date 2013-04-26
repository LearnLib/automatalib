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
package net.automatalib.automata;

import java.util.Collection;

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
	public S addState(SP property);
	public S addInitialState(SP property);
	
	public S addState();
	public S addInitialState();

    public void setInitial(S state, boolean initial);
	
	public void setStateProperty(S state, SP property);
	public void setTransitionProperty(T transition, TP property);
    
    public T createTransition(S successor, TP properties);
    
    public void addTransition(S state, I input, T transition);
    public void setTransitions(S state, I input, Collection<T> transitions);
    public void removeTransition(S state, I input, T transition);
    public void removeAllTransitions(S state, I input);
    public void removeAllTransitions(S state);
    
    public void addTransition(S state, I input, S successor, TP properties);
    
    public T copyTransition(T trans, S succ);
}
