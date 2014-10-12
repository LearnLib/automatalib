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

import net.automatalib.commons.util.IPair;
import net.automatalib.ts.DeterministicTransitionSystem;

public class DTSComposition<S1, S2, I, T1, T2,
TS1 extends DeterministicTransitionSystem<S1, I, T1>,
TS2 extends DeterministicTransitionSystem<S2, I, T2>> implements
		DeterministicTransitionSystem<IPair<S1, S2>, I, IPair<T1, T2>> {
	
	protected final TS1 ts1;
	protected final TS2 ts2;
	protected final boolean allowPartial;
	
	public DTSComposition(TS1 ts1, TS2 ts2, boolean allowPartial) {
		this.ts1 = ts1;
		this.ts2 = ts2;
		this.allowPartial = allowPartial;
	}
	
	public DTSComposition(TS1 ts1, TS2 ts2) {
		this(ts1, ts2, false);
	}
	
	public TS1 getFirstTS() {
		return ts1;
	}
	
	public TS2 getSecondTS() {
		return ts2;
	}

	@Override
	public IPair<S1, S2> getInitialState() {
		return new IPair<>(ts1.getInitialState(), ts2.getInitialState());
	}

	@Override
	public IPair<T1, T2> getTransition(IPair<S1, S2> state, I input) {
		S1 s1 = state.first;
		S2 s2 = state.second;
		
		T1 t1 = (s1 == null) ? null : ts1.getTransition(s1, input);
		if(t1 == null && !allowPartial)
			return null;
		T2 t2 = (s2 == null) ? null : ts2.getTransition(s2, input);
		if(t2 == null && !allowPartial)
			return null;
		return new IPair<>(t1, t2);
	}

	@Override
	public IPair<S1, S2> getSuccessor(IPair<T1, T2> transition) {
		T1 t1 = transition.first;
		T2 t2 = transition.second;
		return new IPair<>((t1 == null) ? null : ts1.getSuccessor(t1),
				(t2 == null) ? null : ts2.getSuccessor(t2));
	}

}
