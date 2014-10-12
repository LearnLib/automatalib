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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A mutable automaton. This interface adds support for non-destructive modifications, i.e.,
 * adding and modifying states and transitions. If also removal of states and single transitions
 * (from the set of outgoing transitions) should be removed, then {@link ShrinkableAutomaton}
 * is the adequate interface.
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
public interface MutableAutomaton<S,I,T,SP,TP> 
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
	default public S addInitialState(@Nullable SP property) {
		S state = addState(property);
		setInitial(state, true);
		return state;
	}
	
	@Nonnull
	default public S addState() {
		return addState(null);
	}
	
	@Nonnull
	default public S addInitialState() {
		return addInitialState(null);
	}

    public void setInitial(S state, boolean initial);
	
	public void setStateProperty(S state, @Nullable SP property);
	public void setTransitionProperty(T transition, @Nullable TP property);
    
	@Nonnull
    public T createTransition(S successor, @Nullable TP properties);
    
    default public void addTransition(S state, @Nullable I input, T transition) {
    	Set<T> transitions = new HashSet<T>(getTransitions(state, input));
		if(!transitions.add(transition))
			return;
		setTransitions(state, input, transitions);
    }
    
    default public void addTransitions(S state, @Nullable I input, Collection<? extends T> transitions) {
    	Set<T> newTransitions = new HashSet<T>(getTransitions(state, input));
		if(!newTransitions.addAll(transitions))
			return;
		setTransitions(state, input, newTransitions);
    }
    
    public void setTransitions(S state, @Nullable I input, Collection<? extends T> transitions);
    
    default public void removeTransition(S state, @Nullable I input, T transition) {
    	Set<T> transitions = new HashSet<T>(getTransitions(state, input));
		if(!transitions.remove(transition))
			return;
		setTransitions(state, input, transitions);
    }
    
    default public void removeAllTransitions(S state, @Nullable I input) {
    	setTransitions(state, input, Collections.<T>emptySet());
    }
    
    public void removeAllTransitions(S state);
    
    @Nonnull
    default public T addTransition(S state, @Nullable I input, S successor, @Nullable TP properties) {
    	T trans = createTransition(successor, properties);
		addTransition(state, input, trans);
		return trans;
    }
    
    @Nonnull
    default public T copyTransition(T trans, S succ) {
    	TP property = getTransitionProperty(trans);
    	return createTransition(succ, property);
    }
}
