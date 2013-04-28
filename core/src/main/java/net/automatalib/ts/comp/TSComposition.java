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
package net.automatalib.ts.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.commons.util.Pair;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.abstractimpl.AbstractTS;


public class TSComposition<S1, S2, I, T1, T2,
		TS1 extends TransitionSystem<S1, I, T1>,
		TS2 extends TransitionSystem<S2, I, T2>> extends
		AbstractTS<Pair<S1, S2>, I, Pair<T1, T2>> {
	
	protected final TS1 ts1;
	protected final TS2 ts2;
	
	/**
	 * Constructor.
	 * @param ts1 first transition system
	 * @param ts2 second transition system
	 */
	public TSComposition(TS1 ts1,
			TS2 ts2) {
		this.ts1 = ts1;
		this.ts2 = ts2;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.TransitionSystem#getInitialStates()
	 */
	@Override
	public Set<Pair<S1,S2>> getInitialStates() {
		Collection<S1> init1 = ts1.getInitialStates();
		Collection<S2> init2 = ts2.getInitialStates();
		
		Set<Pair<S1,S2>> result = new HashSet<Pair<S1,S2>>(init1.size() * init2.size());
		
		for(S1 s1 : init1) {
			for(S2 s2 : init2)
				result.add(Pair.make(s1, s2));
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.TransitionSystem#getTransitions(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Collection<Pair<T1, T2>> getTransitions(Pair<S1, S2> state, I input) {
		S1 s1 = state.getFirst();
		S2 s2 = state.getSecond();
		Collection<T1> trans1 = ts1.getTransitions(s1, input);
		Collection<T2> trans2 = ts2.getTransitions(s2, input);
		
		if(trans1 == null || trans1.isEmpty() || trans2 == null || trans2.isEmpty())
			return Collections.emptySet();
		
		List<Pair<T1,T2>> result = new ArrayList<Pair<T1,T2>>(trans1.size() * trans2.size());
		
		for(T1 t1 : trans1) {
			for(T2 t2 : trans2)
				result.add(Pair.make(t1, t2));
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.TransitionSystem#getSuccessor(java.lang.Object)
	 */
	@Override
	public Pair<S1, S2> getSuccessor(Pair<T1, T2> transition) {
		T1 t1 = transition.getFirst();
		T2 t2 = transition.getSecond();
		return Pair.make(ts1.getSuccessor(t1), ts2.getSuccessor(t2));
	}

}
