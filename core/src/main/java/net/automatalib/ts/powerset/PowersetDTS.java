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
package net.automatalib.ts.powerset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.abstractimpl.AbstractDTS;


public class PowersetDTS<S, I, T> extends AbstractDTS<Set<S>, I, Collection<T>> {
	
	private final TransitionSystem<S, I, T> ts;
	
	public PowersetDTS(TransitionSystem<S,I,T> ts) {
		this.ts = ts;
	}

	@Override
	public Set<S> getInitialState() {
		return ts.getInitialStates();
	}

	@Override
	public Collection<T> getTransition(Set<S> state, I input) {
		List<T> result = new ArrayList<T>();
		for(S s : state) {
			Collection<T> transitions = ts.getTransitions(s, input);
			if(transitions != null)
				result.addAll(transitions);
		}
		return result;
	}

	@Override
	public Set<S> getSuccessor(Collection<T> transition) {
		Set<S> result = new HashSet<S>();
		for(T trans : transition)
			result.add(ts.getSuccessor(trans));
		return result;
	}
	
	@Override
	public Set<S> getSuccessor(Set<S> state, I input) {
		Set<S> result = new HashSet<S>();
		for(S s : state) {
			Collection<T> transitions = ts.getTransitions(s, input);
			if(transitions == null)
				continue;
			for(T t : transitions)
				result.add(ts.getSuccessor(t));
		}
		
		return result;
	}
}
