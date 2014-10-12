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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A mutable automaton that also supports destructive modifications, i.e., removal
 * of states and transitions.
 * 
 * @author Malte Isberner 
 *
 * @param <S> state class
 * @param <I> input symbol class
 * @param <T> transition class
 * @param <SP> state property class
 * @param <TP> transition property class
 */
@ParametersAreNonnullByDefault
public interface ShrinkableAutomaton<S, I, T, SP, TP> extends MutableAutomaton<S,I,T,SP,TP> {
	
	public static <S,I,T,SP,TP> void unlinkState(MutableAutomaton<S,I,T,SP,TP> automaton,
			S state, S replacement, Collection<I> inputs) {
		
		for(S curr : automaton) {
			if(state.equals(curr))
				continue;
			
			for(I input : inputs) {
				Collection<? extends T> transitions = automaton.getTransitions(curr, input);
				if(transitions.isEmpty())
					continue;
				
				boolean modified = false;
				List<T> modTransitions = new LinkedList<T>(transitions); // TODO
					
				ListIterator<T> it = modTransitions.listIterator();
				while(it.hasNext()) {
					T trans = it.next();
					if(automaton.getSuccessor(trans) == state) {
						if(replacement == null)
							it.remove();
						else {
							T transRep = automaton.copyTransition(trans, replacement);
							it.set(transRep);
						}
						modified = true;
					}
				}
					
				if(modified)
					automaton.setTransitions(curr, input, modTransitions);
			}
		}
	}
	
	
		
	/**
     * removes a state from the automaton.
     *
     * @param state state to be removed
     */
    default public void removeState(S state) {
    	removeState(state, null);
    }
    
    //FIXME: should this be replaceState?
    /**
     * Removes a state from the automaton. All ingoing transitions to this state are redirected
     * to the given replacement state. If a <code>null</code> replacement is given, then
     * this method behaves like the above {@link #removeState(Object)}.
     * 
     * @param state the state to remove
     * @param replacement the replacement state, or <code>null</code>
     */
    public void removeState(S state, @Nullable S replacement);    
}
