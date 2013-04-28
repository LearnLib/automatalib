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
package net.automatalib.automata.abstractimpl;

import java.util.Iterator;

import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.ts.abstractimpl.AbstractDTS;


/**
 * Abstract base class for deterministic automata.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class
 * @param <I> input symbol class
 * @param <T> transition class
 */
public abstract class AbstractDeterministicAutomaton<S, I, T> extends AbstractDTS<S,I,T> implements
		DeterministicAutomaton<S, I, T> {
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.simple.SimpleAutomaton#size()
	 */
	@Override
	public int size() {
		return AbstractAutomaton.size(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<S> iterator() {
		return AbstractAutomaton.iterator(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.simple.SimpleAutomaton#stateIDs()
	 */
	@Override
	public StateIDs<S> stateIDs() {
		return AbstractAutomaton.stateIDs(this);
	}
}
