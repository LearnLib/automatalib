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
package net.automatalib.commons.util.nid;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.automatalib.commons.util.ref.Ref;
import net.automatalib.commons.util.ref.StrongRef;
import net.automatalib.commons.util.ref.WeakRef;


public class IDChangeNotifier<T extends NumericID> {
	private final List<Ref<IDChangeListener<T>>> listeners
		= new LinkedList<Ref<IDChangeListener<T>>>();
	
	public void addListener(IDChangeListener<T> listener, boolean weak) {
		Ref<IDChangeListener<T>> ref;
		if(weak)
			ref = new WeakRef<IDChangeListener<T>>(listener);
		else
			ref = new StrongRef<IDChangeListener<T>>(listener);
		
		listeners.add(ref);
	}
	
	public void removeListener(IDChangeListener<T> listener) {
		if(listener == null)
			return;
		
		Iterator<? extends Ref<?>> it = listeners.iterator();
		
		while(it.hasNext()) {
			Object referent = it.next().get();
			if(referent == null)
				it.remove();
			else if(referent.equals(listener))
				it.remove();
		}
	}
	
	public void notifyListeners(T obj, int newId, int oldId) {
		Iterator<Ref<IDChangeListener<T>>> it 
			= listeners.iterator();
		
		while(it.hasNext()) {
			IDChangeListener<T> listener = it.next().get();
			if(listener == null)
				it.remove();
			else
				listener.idChanged(obj, newId, oldId);
		}
	}
}
