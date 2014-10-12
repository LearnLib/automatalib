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
package net.automatalib.automata.helpers;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.mappings.MutableMapping;

public class StateIDStaticMapping<S, V> implements MutableMapping<S, V> {
	
	private final StateIDs<S> stateIds;
	private final Object[] storage;
	
	public StateIDStaticMapping(StateIDs<S> stateIds, int size) {
		this.stateIds = stateIds;
		this.storage = new Object[size];
	}

	@Override
	@SuppressWarnings("unchecked")
	public V get(S elem) {
		return (V)storage[stateIds.getStateId(elem)];
	}

	@Override
	@SuppressWarnings("unchecked")
	public V put(S key, V value) {
		V old = (V)storage[stateIds.getStateId(key)];
		storage[stateIds.getStateId(key)] = value;
		return old;
	}

}
