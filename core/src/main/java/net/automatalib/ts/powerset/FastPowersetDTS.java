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

import net.automatalib.commons.util.nid.NumericID;
import net.automatalib.ts.PowersetViewTS;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.abstractimpl.AbstractDTS;


public class FastPowersetDTS<S extends NumericID, I, T> extends
		AbstractDTS<FastPowersetState<S>, I, Set<? extends T>> implements PowersetViewTS<FastPowersetState<S>,I,Set<? extends T>,S,T> {
	
	private final TransitionSystem<S, I, T> ts;
	
	public FastPowersetDTS(TransitionSystem<S,I,T> ts) {
		this.ts = ts;
	}

	@Override
	public FastPowersetState<S> getInitialState() {
		FastPowersetState<S> result = new FastPowersetState<S>();
		for(S init : ts.getInitialStates())
			result.add(init, init.getId());
		return result;
	}

	@Override
	public Set<? extends T> getTransition(FastPowersetState<S> state, I input) {
		Set<T> result = new HashSet<>();
		for(S s : state) {
			Collection<? extends T> transitions = ts.getTransitions(s, input);
			result.addAll(transitions);
		}
		return result;
	}

	@Override
	public FastPowersetState<S> getSuccessor(Set<? extends T> transition) {
		FastPowersetState<S> succ = new FastPowersetState<S>();
		for(T t : transition) {
			S succS = ts.getSuccessor(t);
			succ.add(succS, succS.getId());
		}
		
		return succ;
	}

	@Override
	public FastPowersetState<S> getSuccessor(FastPowersetState<S> state, I input) {
		FastPowersetState<S> succ = new FastPowersetState<S>();
		
		for(S s : state) {
			Collection<? extends S> succs = ts.getSuccessors(s, input);
			for(S succS : succs) {
				succ.add(succS, succS.getId());
			}
		}
		
		return succ;
	}

	@Override
	public Collection<? extends S> getOriginalStates(FastPowersetState<S> state) {
		return state;
	}

	@Override
	public Collection<? extends T> getOriginalTransitions(
			Set<? extends T> transition) {
		return transition;
	}
	
	
	

}
