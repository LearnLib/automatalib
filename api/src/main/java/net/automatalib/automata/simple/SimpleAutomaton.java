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
package net.automatalib.automata.simple;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.helpers.SimpleStateIDs;
import net.automatalib.automata.helpers.StateIDStaticMapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.ts.simple.SimpleTS;


/**
 * A simple automaton, i.e., a {@link SimpleTS} with a finite number of states.
 * 
 * @author Malte Isberner 
 *
 * @param <S> state class.
 * @param <I> input symbol class.
 */
public interface SimpleAutomaton<S, I> extends SimpleTS<S,I>, Iterable<S> {
	/**
     * Retrieves all states of the transition system.
     * Implementing classes should return an unmodifiable
     * collection
     * @return all states in the transition system
     */
	@Nonnull
	public Collection<S> getStates();
	
	/**
	 * Retrieves the size (number of states) of this transition system.
	 * @return the number of states of this transition system
	 */
	default public int size() {
		return getStates().size();
	}
	

	@Nonnull
	default public StateIDs<S> stateIDs() {
		return new SimpleStateIDs<S>(this);
	}
	
	@Override
	@Nonnull
	default public Iterator<S> iterator() {
		return getStates().iterator();
	}
	
	@Override
	@Nonnull
	default public <V> MutableMapping<S,V> createStaticStateMapping() {
		return new StateIDStaticMapping<>(stateIDs(), size());
	}
}
