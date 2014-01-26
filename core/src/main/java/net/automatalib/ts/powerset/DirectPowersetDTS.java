/* Copyright (C) 2013-2014 TU Dortmund
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.ts.PowersetViewTS;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.abstractimpl.AbstractDTS;


public class DirectPowersetDTS<S, I, T> extends AbstractDTS<Set<? extends S>, I, Set<? extends T>>
		implements PowersetViewTS<Set<? extends S>, I, Set<? extends T>, S, T> {
	
	private final TransitionSystem<S, I, T> ts;
	
	public DirectPowersetDTS(TransitionSystem<S,I,T> ts) {
		this.ts = ts;
	}

	@Override
	public Set<? extends S> getInitialState() {
		return ts.getInitialStates();
	}

	@Override
	public Set<? extends T> getTransition(Set<? extends S> state, I input) {
		Set<T> result = new HashSet<>();
		for(S s : state) {
			Collection<? extends T> transitions = ts.getTransitions(s, input);
			result.addAll(transitions);
		}
		return result;
	}

	@Override
	public Set<? extends S> getSuccessor(Set<? extends T> transition) {
		Set<S> result = new HashSet<S>();
		for(T trans : transition)
			result.add(ts.getSuccessor(trans));
		return result;
	}
	
	@Override
	public Set<? extends S> getSuccessor(Set<? extends S> state, I input) {
		Set<S> result = new HashSet<S>();
		for(S s : state) {
			Collection<? extends T> transitions = ts.getTransitions(s, input);
			for(T t : transitions) {
				result.add(ts.getSuccessor(t));
			}
		}
		
		return result;
	}

	@Override
	public Collection<? extends S> getOriginalStates(Set<? extends S> state) {
		return state;
	}

	@Override
	public Collection<? extends T> getOriginalTransitions(
			Set<? extends T> transition) {
		return transition;
	}
}
