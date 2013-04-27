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
package net.automatalib.automata.base;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.commons.util.mappings.MutableMapping;

public class StateIDGrowingMapping<S,V> implements MutableMapping<S,V> {

	private final Automaton<S,?,?> automaton;
	private final StateIDs<S> stateIds;
	private final ResizingObjectArray storage;
	
	public StateIDGrowingMapping(Automaton<S,?,?> automaton, StateIDs<S> stateIds) {
		this.automaton = automaton;
		this.stateIds = stateIds;
		this.storage = new ResizingObjectArray(automaton.size());
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.commons.util.mappings.Mapping#get(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public V get(S elem) {
		int id = stateIds.getStateId(elem);
		if(id < storage.array.length)
			return (V)storage.array[id];
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.commons.util.mappings.MutableMapping#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public V put(S key, V value) {
		int id = stateIds.getStateId(key);
		if(id >= storage.array.length)
			storage.ensureCapacity(automaton.size());
		V old = (V)storage.array[id];
		storage.array[id] = value;
		return old;
	}

}
