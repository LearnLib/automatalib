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
package net.automatalib.util.automata.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.util.automata.predicates.TransitionPredicates;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

final class PlainAutomatonCopy<S1, I1, T1, S2, I2, T2, SP2, TP2> extends
		AbstractLowLevelAutomatonCopier<S1, I1, T1, S2, I2, T2, SP2, TP2, Automaton<S1,? super I1,T1>> {
	
	private static class StateRec<S1,S2> {
		private final S1 inState;
		private final S2 outState;
		
		public StateRec(S1 inState, S2 outState) {
			this.inState = inState;
			this.outState = outState;
		}
	}

	public PlainAutomatonCopy(Automaton<S1, ? super I1, T1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
			Function<? super I1, ? extends I2> inputsMapping,
			Function<? super S1, ? extends SP2> spMapping,
			Function<? super T1, ? extends TP2> tpMapping,
			Predicate<? super S1> stateFilter,
			TransitionPredicate<? super S1, ? super I1, ? super T1> transFilter) {
		super(in, inputs, out, inputsMapping, spMapping, tpMapping, stateFilter, transFilter);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.automata.copy.AbstractAutomatonCopy#doCopy()
	 */
	@Override
	public void doCopy() {
		List<StateRec<S1,S2>> outStates = new ArrayList<>(in.size());
		
		for(S1 s1 : in) {
			if(stateFilter.apply(s1)) {
				S2 s2 = copyState(s1);
				outStates.add(new StateRec<>(s1, s2));
			}
		}
		
		for(StateRec<S1,S2> p : outStates) {
			S1 s1 = p.inState;
			S2 s2 = p.outState;
			
			for(I1 i1 : inputs) {
				I2 i2 = inputsMapping.apply(i1);
				Collection<? extends T1> transitions1 = in.getTransitions(s1, i1);
				Predicate<T1> transPred = TransitionPredicates.toUnaryPredicate(transFilter, s1, i1);
				copyTransitions(s2, i2, Iterables.filter(transitions1, transPred));
			}
		}
		
		updateInitials();
	}

}