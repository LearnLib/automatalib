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
package net.automatalib.automata.base.fast;

import java.util.Collection;
import java.util.HashSet;

import net.automatalib.commons.util.nid.AbstractMutableNumericID;


public abstract class FastNondetState<S extends FastNondetState<S, T>, T>
		extends AbstractMutableNumericID {
	private final Collection<T>[] transitions;
	
	
	@SuppressWarnings("unchecked")
	public FastNondetState(int numInputs) {
		this.transitions = new Collection[numInputs];
	}
	
	
	public final Collection<T> getTransitions(int inputIdx) {
		return transitions[inputIdx];
	}
	
	public final void setTransitions(int inputIdx, Collection<T> transitions) {
		this.transitions[inputIdx] = new HashSet<T>(transitions);
	}
	
	public void clearTransitions() {
		for(int i = 0; i < transitions.length; i++)
			transitions[i].clear();
	}

}
