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
package net.automatalib.automata.abstractimpl;

import java.util.Iterator;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.ts.abstractimpl.AbstractTS;



/**
 * Abstract base class for automata.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class.
 * @param <I> input symbol class.
 * @param <T> transition class.
 */
public abstract class AbstractAutomaton<S, I, T> extends AbstractTS<S, I, T> implements Automaton<S, I, T> {
	/**
	 * Provides a realization of {@link Automaton#size()} using
	 * {@link Automaton#getStates()}.
	 * @see Automaton#size()
	 */
	public static <S,I,T> int size(Automaton<S,I,T> $this) {
		return $this.getStates().size();
	}
	
	/**
	 * Provides a realization of {@link Automaton#iterator()} using
	 * {@link Automaton#iterator()}.
	 * @see Automaton#iterator()
	 */
	public static <S,I,T> Iterator<S> iterator(Automaton<S,I,T> $this) {
		return $this.getStates().iterator();
	}
	
	/**
	 * Provides a realization of {@link Automaton#stateIDs()} using
	 * a {@link SimpleStateIDs} object.
	 * @see Automaton#stateIDs()
	 */
	public static <S,I,T> StateIDs<S> stateIDs(Automaton<S,I,T> $this) {
		return new SimpleStateIDs<>($this);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleFiniteTS#size()
	 */
	@Override
	public int size() {
		return size(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<S> iterator() {
		return iterator(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.simple.SimpleAutomaton#stateIDs()
	 */
	@Override
	public StateIDs<S> stateIDs() {
		return stateIDs(this);
	}
}
