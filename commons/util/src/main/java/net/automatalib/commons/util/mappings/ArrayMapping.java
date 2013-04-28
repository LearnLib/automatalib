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
package net.automatalib.commons.util.mappings;

import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.commons.util.nid.IDChangeListener;
import net.automatalib.commons.util.nid.NumericID;

public final class ArrayMapping<K extends NumericID, V> implements MutableMapping<K, V>,
		IDChangeListener<K> {

	private final ResizingObjectArray storage;
	
	public ArrayMapping() {
		storage = new ResizingObjectArray();
	}
	
	public ArrayMapping(int initialSize) {
		storage = new ResizingObjectArray(initialSize);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public V get(K elem) {
		int id = elem.getId();
		if(id >= storage.array.length)
			return null;
		return (V)storage.array[id];
	}

	@Override
	@SuppressWarnings("unchecked")
	public V put(K key, V value) {
		int id = key.getId();
		storage.ensureCapacity(id+1);
		V old = (V)storage.array[id];
		storage.array[id] = value;
		return old;
	}

	@Override
	public void idChanged(K obj, int newId, int oldId) {
		if(newId == -1) {
			if(oldId < storage.array.length)
				storage.array[oldId] = null;
			return;
		}
		Object oldVal = null;
		if(oldId < storage.array.length) {
			oldVal = storage.array[oldId];
			storage.array[oldId] = oldVal;
		}
		storage.ensureCapacity(newId+1);
		storage.array[newId] = oldVal;
	}
	
}
