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
package net.automatalib.ts.powerset;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

public class FastPowersetState<S> extends AbstractSet<S> {
	
	private final BitSet bs = new BitSet();
	private final List<S> contents
		= new ArrayList<S>();

	public void add(S state, int id) {
		if(bs.get(id))
			return;
		bs.set(id);
		contents.add(state);
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<S> iterator() {
		return contents.iterator();
	}

	@Override
	public int size() {
		return contents.size();
	}

	
	@Override
	public int hashCode() {
		return bs.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if(obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		FastPowersetState<?> other = (FastPowersetState<?>) obj;
		return bs.equals(other.bs);
	}
	
	
	
	
}
