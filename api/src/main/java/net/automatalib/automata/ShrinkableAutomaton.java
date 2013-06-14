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
 * A mutable automaton that also supports destructive modifications, i.e., removal
 * of states and transitions.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class
 * @param <I> input symbol class
 * @param <T> transition class
 * @param <SP> state property class
 * @param <TP> transition property class
 */
@ParametersAreNonnullByDefault
public interface ShrinkableAutomaton<S, I, T, SP, TP> extends MutableAutomaton<S,I,T,SP,TP> {
	/**
     * removes a state from the automaton.
     *
     * @param s: state to be removed
     */
    public void removeState(S state);
    
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
