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
package net.automatalib.util.ts.acceptors;

import net.automatalib.commons.util.Pair;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.ts.acceptors.abstractimpl.AbstractAcceptorTS;
import net.automatalib.ts.acceptors.abstractimpl.AbstractDeterministicAcceptorTS;
import net.automatalib.ts.comp.DTSComposition;

public class DetAcceptorComposition<S1, S2, I, A1 extends DeterministicAcceptorTS<S1, I>, A2 extends DeterministicAcceptorTS<S2, I>>
		extends DTSComposition<S1, S2, I, S1, S2, A1, A2> implements DeterministicAcceptorTS<Pair<S1,S2>, I> {

	private final AcceptanceCombiner combiner;
	
	public DetAcceptorComposition(A1 ts1, A2 ts2, AcceptanceCombiner combiner) {
		super(ts1, ts2);
		this.combiner = combiner;
	}
	
	@Override
	public boolean isAccepting(Pair<S1, S2> state) {
		S1 s1 = state.getFirst();
		S2 s2 = state.getSecond();
		boolean acc1 = ts1.isAccepting(s1);
		boolean acc2 = ts2.isAccepting(s2);
		return combiner.combine(acc1, acc2);
	}

	@Override
	public boolean accepts(Iterable<I> input) {
		return AbstractDeterministicAcceptorTS.accepts(this, input);
	}

	@Override
	public Boolean getStateProperty(Pair<S1, S2> state) {
		return AbstractAcceptorTS.getStateProperty(this, state);
	}

	@Override
	public Void getTransitionProperty(Pair<S1, S2> transition) {
		return AbstractAcceptorTS.getTransitionProperty(this, transition);
	}

}
