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

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.ts.acceptors.AcceptorTS;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;


public abstract class Acceptors {
	
	private Acceptors() {}
	
	public static <S1,S2,I,T1,T2,
		TS1 extends DeterministicAcceptorTS<S1,I>,
		TS2 extends DeterministicAcceptorTS<S2,I>>
	DetAcceptorComposition<S1, S2, I, TS1, TS2>
	combine(TS1 ts1, TS2 ts2, AcceptanceCombiner combiner) {
		return new DetAcceptorComposition<S1, S2, I, TS1, TS2>(ts1, ts2, combiner);
	}
	
	
	
	public static <S1, S2, I, TS1 extends DeterministicAcceptorTS<S1, I>, TS2 extends DeterministicAcceptorTS<S2, I>>
	DetAcceptorComposition<S1, S2, I, TS1, TS2> and(
			TS1 ts1, TS2 ts2) {
		return combine(ts1, ts2, AcceptanceCombiner.AND);
	}
	
	public static <S1, S2, I, TS1 extends DeterministicAcceptorTS<S1, I>, TS2 extends DeterministicAcceptorTS<S2, I>>
	DetAcceptorComposition<S1, S2, I, TS1, TS2> or(
			TS1 ts1, TS2 ts2) {
		return combine(ts1, ts2, AcceptanceCombiner.OR);
	}
	
	public static <S1, S2, I, TS1 extends DeterministicAcceptorTS<S1, I>, TS2 extends DeterministicAcceptorTS<S2, I>>
	DetAcceptorComposition<S1, S2, I, TS1, TS2> xor(
			TS1 ts1, TS2 ts2) {
		return combine(ts1, ts2, AcceptanceCombiner.XOR);
	}
	
	public static <S1, S2, I, TS1 extends DeterministicAcceptorTS<S1, I>, TS2 extends DeterministicAcceptorTS<S2, I>>
	DetAcceptorComposition<S1, S2, I, TS1, TS2> equiv(
			TS1 ts1, TS2 ts2) {
		return combine(ts1, ts2, AcceptanceCombiner.EQUIV);
	}
	
	public static <S1, S2, I, TS1 extends DeterministicAcceptorTS<S1, I>, TS2 extends DeterministicAcceptorTS<S2, I>>
	DetAcceptorComposition<S1, S2, I, TS1, TS2> impl(
			TS1 ts1, TS2 ts2) {
		return combine(ts1, ts2, AcceptanceCombiner.IMPL);
	}
	
	
	public static <S> Mapping<S,Boolean> acceptance(final AcceptorTS<S, ?> acceptor) {
		return new Mapping<S,Boolean>() {
			@Override
			public Boolean get(S elem) {
				return acceptor.isAccepting(elem);
			}
		};
	}
	
	

}
