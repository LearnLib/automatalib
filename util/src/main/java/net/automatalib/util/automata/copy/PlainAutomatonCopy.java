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
package net.automatalib.util.automata.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.mappings.Mapping;

final class PlainAutomatonCopy<S1, I1, T1, S2, I2, T2, SP2, TP2> extends
		AbstractAutomatonCopy<S1, I1, T1, S2, I2, T2, SP2, TP2, Automaton<S1,I1,T1>> {
	
	private static class StateRec<S1,S2> {
		private final S1 inState;
		private final S2 outState;
		
		public StateRec(S1 inState, S2 outState) {
			this.inState = inState;
			this.outState = outState;
		}
	}

	public PlainAutomatonCopy(Automaton<S1, I1, T1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2, I2, T2, SP2, TP2> out,
			Mapping<? super I1, ? extends I2> inputsMapping,
			Mapping<? super S1, ? extends SP2> spMapping,
			Mapping<? super T1, ? extends TP2> tpMapping) {
		super(in, inputs, out, inputsMapping, spMapping, tpMapping);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.automata.copy.AbstractAutomatonCopy#doCopy()
	 */
	@Override
	public void doCopy() {
		List<StateRec<S1,S2>> outStates = new ArrayList<>(in.size());
		
		for(S1 s1 : in) {
			S2 s2 = copyState(s1);
			outStates.add(new StateRec<>(s1, s2));
		}
		
		for(StateRec<S1,S2> p : outStates) {
			S1 s1 = p.inState;
			S2 s2 = p.outState;
			
			for(I1 i1 : inputs) {
				I2 i2 = inputsMapping.get(i1);
				Collection<? extends T1> transitions1 = in.getTransitions(s1, i1);
				copyTransitions(s2, i2, transitions1);
			}
		}
		
		updateInitials();
	}

}
