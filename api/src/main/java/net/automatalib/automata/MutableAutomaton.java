/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    	Set<T> transitions = new HashSet<>(getTransitions(state, input));
		if(!transitions.add(transition))
			return;
		setTransitions(state, input, transitions);
    }
    
    default public void addTransitions(S state, @Nullable I input, Collection<? extends T> transitions) {
    	Set<T> newTransitions = new HashSet<>(getTransitions(state, input));
		if(!newTransitions.addAll(transitions))
			return;
		setTransitions(state, input, newTransitions);
    }
    
    public void setTransitions(S state, @Nullable I input, Collection<? extends T> transitions);
    
    default public void removeTransition(S state, @Nullable I input, T transition) {
    	Set<T> transitions = new HashSet<>(getTransitions(state, input));
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
