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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.concepts.StateIDs;

public class SimpleStateIDs<S> implements StateIDs<S> {
	
	private final Map<S,Integer> stateIds;
	private final List<S> states;
	
	public SimpleStateIDs(Automaton<S,?,?> automaton) {
		this.states = new ArrayList<S>(automaton.getStates());
		int numStates = this.states.size();
		this.stateIds = new HashMap<S,Integer>(numStates);
		
		for(int i = 0; i < numStates; i++) {
			S state = this.states.get(i);
			stateIds.put(state, i);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.StateIDs#getStateId(java.lang.Object)
	 */
	@Override
	public int getStateId(S state) {
		return stateIds.get(state).intValue();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.StateIDs#getState(int)
	 */
	@Override
	public S getState(int id) {
		return states.get(id);
	}

}
