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
package net.automatalib.automata.base;

import net.automatalib.automata.Automaton;
import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.commons.util.nid.IDChangeListener;
import net.automatalib.commons.util.nid.NumericID;

public class StateIDDynamicMapping<S extends NumericID, V> implements MutableMapping<S, V>,
		IDChangeListener<S> {
	
	private final Automaton<S, ?, ?> automaton;
	private final ResizingObjectArray storage;

	public StateIDDynamicMapping(Automaton<S,?,?> automaton) {
		this.automaton = automaton;
		this.storage = new ResizingObjectArray(automaton.size());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public V get(S elem) {
		int id = elem.getId();
		if(id < storage.array.length)
			return (V)storage.array[id];
		return null;
	}

	@Override
	public void idChanged(S obj, int newId, int oldId) {
		Object oldValue = null;
		if(oldId > 0 && oldId < storage.array.length) {
			oldValue = storage.array[oldId];
			storage.array[oldId] = null;
		}
		if(newId >= storage.array.length)
			storage.ensureCapacity(automaton.size());
		storage.array[newId] = oldValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	public V put(S key, V value) {
		int id = key.getId();
		if(id >= storage.array.length)
			storage.ensureCapacity(automaton.size());
		V old = (V)storage.array[id];
		storage.array[id] = value;
		return old;
	}

}
