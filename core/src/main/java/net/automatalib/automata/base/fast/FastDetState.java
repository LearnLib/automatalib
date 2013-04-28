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
package net.automatalib.automata.base.fast;

import net.automatalib.commons.util.nid.AbstractMutableNumericID;


public abstract class FastDetState<S extends FastDetState<S,T>, T> extends AbstractMutableNumericID {
	private final Object[] transitions;
	
	public FastDetState(int numInputs) {
		this.transitions = new Object[numInputs];
	}
	
	
	@SuppressWarnings("unchecked")
	public final T getTransition(int inputIdx) {
		return (T)transitions[inputIdx];
	}
	
	public final void setTransition(int inputIdx, T transition) {
		transitions[inputIdx] = transition;
	}
	
	public void clearTransitions() {
		for(int i = 0; i < transitions.length; i++)
			transitions[i] = null;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "s" + getId();
	}
}
