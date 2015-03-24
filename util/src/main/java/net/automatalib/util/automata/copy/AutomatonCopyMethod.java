/* Copyright (C) 2014 TU Dortmund
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

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.util.traversal.TraversalOrder;


public interface AutomatonCopyMethod {
	
	public static AutomatonCopyMethod STATE_BY_STATE = new AutomatonCopyMethod() {
		@Override
		public <S1, I1, T1, S2, I2, T2, SP2, TP2> LowLevelAutomatonCopier<S1, I1, T1, S2, I2, T2, SP2, TP2> createLowLevelCopier(
				Automaton<S1,? super I1,T1> in,
				Collection<? extends I1> inputs,
				MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
				Function<? super I1, ? extends I2> inputsMapping,
				Function<? super S1, ? extends SP2> spMapping,
				Function<? super T1, ? extends TP2> tpMapping,
				Predicate<? super S1> stateFilter,
				TransitionPredicate<? super S1, ? super I1, ? super T1> transitionFilter) {
			return new PlainAutomatonCopy<S1,I1,T1,S2,I2,T2,SP2,TP2>(in, inputs, out, inputsMapping, spMapping, tpMapping, stateFilter, transitionFilter);
		}
	};
	
	public static AutomatonCopyMethod DFS = new TraversalAutomatonCopy.CopyMethod(TraversalOrder.DEPTH_FIRST);
	public static AutomatonCopyMethod BFS = new TraversalAutomatonCopy.CopyMethod(TraversalOrder.BREADTH_FIRST);
	
	public <S1, I1, T1, S2, I2, T2, SP2, TP2>
	LowLevelAutomatonCopier<S1, I1, T1, S2, I2, T2, SP2, TP2>
	createLowLevelCopier(Automaton<S1,? super I1,T1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
			Function<? super I1, ? extends I2> inputsMapping,
			Function<? super S1, ? extends SP2> spMapping,
			Function<? super T1, ? extends TP2> tpMapping,
			Predicate<? super S1> stateFilter,
			TransitionPredicate<? super S1, ? super I1, ? super T1> transitionFilter);
	
}
