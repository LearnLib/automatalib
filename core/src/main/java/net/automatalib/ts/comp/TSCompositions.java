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
package net.automatalib.ts.comp;

import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.TransitionSystem;

public abstract class TSCompositions {
	
	
	public static <S1,S2,I,T1,T2,
			TS1 extends TransitionSystem<S1,I,T1>,
			TS2 extends TransitionSystem<S2,I,T2>>
	TSComposition<S1, S2, I, T1, T2, TS1, TS2> compose(TS1 ts1, TS2 ts2) {
		return new TSComposition<S1, S2, I, T1, T2, TS1, TS2>(ts1, ts2);
	}
	
	public static <S1,S2,I,T1,T2,
	TS1 extends DeterministicTransitionSystem<S1,I,T1>,
	TS2 extends DeterministicTransitionSystem<S2,I,T2>>
	DTSComposition<S1, S2, I, T1, T2, TS1, TS2> compose(TS1 ts1, TS2 ts2) {
		return new DTSComposition<S1, S2, I, T1, T2, TS1, TS2>(ts1, ts2);
	}
}
