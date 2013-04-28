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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.commons.util.nid.NumericID;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.abstractimpl.AbstractDTS;


public class FastPowersetDTS<S extends NumericID, I, T> extends
		AbstractDTS<FastPowersetState<S>, I, Collection<T>> {
	
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
	public Collection<T> getTransition(FastPowersetState<S> state, I input) {
		List<T> result = new ArrayList<T>();
		for(S s : state) {
			Collection<T> transitions = ts.getTransitions(s, input);
			if(transitions != null)
				result.addAll(transitions);
		}
		return result;
	}

	@Override
	public FastPowersetState<S> getSuccessor(Collection<T> transition) {
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
			Collection<S> succs = ts.getSuccessors(s, input);
			if(succs == null)
				continue;
			
			for(S succS : succs)
				succ.add(succS, succS.getId());
		}
		
		return succ;
	}
	
	
	

}
