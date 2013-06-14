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

import java.util.Collection;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.Holder;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.traversal.TraversalOrder;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.util.ts.traversal.TSTraversalAction;
import net.automatalib.util.ts.traversal.TSTraversalVisitor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;


final class TraversalAutomatonCopy<S1, I1, T1, S2, I2, T2, SP2, TP2> extends
		AbstractLowLevelAutomatonCopier<S1, I1, T1, S2, I2, T2, SP2, TP2, TransitionSystem<S1,? super I1,T1>> implements TSTraversalVisitor<S1, I1, T1, S2> {
	
	final static class CopyMethod implements AutomatonCopyMethod {
		private final TraversalOrder traversalOrder;
		
		public CopyMethod(TraversalOrder traversalOrder) {
			this.traversalOrder = traversalOrder;
		}
		
		@Override
		public <S1,I1,T1,S2,I2,T2,SP2,TP2>
		LowLevelAutomatonCopier<S1, I1, T1, S2, I2, T2, SP2, TP2> createLowLevelCopier(
				Automaton<S1, ? super I1, T1> in,
				Collection<? extends I1> inputs,
				MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
				Function<? super I1, ? extends I2> inputsMapping,
				Function<? super S1, ? extends SP2> spMapping,
				Function<? super T1, ? extends TP2> tpMapping,
				Predicate<? super S1> stateFilter,
				TransitionPredicate<? super S1, ? super I1, ? super T1> transitionFilter) {
			return new TraversalAutomatonCopy<S1,I1,T1,S2,I2,T2,SP2,TP2>(traversalOrder, in.size(), in, inputs, out, inputsMapping, spMapping, tpMapping, stateFilter, transitionFilter);
		}
		
	}

	private final TraversalOrder traversalOrder;
	private final int limit;
	
	public TraversalAutomatonCopy(TraversalOrder traversalOrder,
			int limit,
			TransitionSystem<S1, ? super I1, T1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
			Function<? super I1, ? extends I2> inputsMapping,
			Function<? super S1, ? extends SP2> spMapping,
			Function<? super T1, ? extends TP2> tpMapping,
			Predicate<? super S1> stateFilter,
			TransitionPredicate<? super S1,? super I1,? super T1> transFilter) {
		super(in, inputs, out, inputsMapping, spMapping, tpMapping, stateFilter, transFilter);
		this.traversalOrder = traversalOrder;
		this.limit = limit;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.automata.copy.AbstractAutomatonCopy#doCopy()
	 */
	@Override
	public void doCopy() {
		TSTraversal.traverse(traversalOrder, in, limit, inputs, this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.ts.traversal.TSTraversalVisitor#processInitial(java.lang.Object, net.automatalib.commons.util.Holder)
	 */
	@Override
	public TSTraversalAction processInitial(S1 state, Holder<S2> outData) {
		if(stateFilter.apply(state)) {
			outData.value = copyInitialState(state);
			return TSTraversalAction.EXPLORE;
		}
		return TSTraversalAction.IGNORE;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.ts.traversal.TSTraversalVisitor#startExploration(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean startExploration(S1 state, S2 data) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.ts.traversal.TSTraversalVisitor#processTransition(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, net.automatalib.commons.util.Holder)
	 */
	@Override
	public TSTraversalAction processTransition(S1 source, S2 srcData, I1 input,
			T1 transition, S1 succ, Holder<S2> outData) {
		if(transFilter.apply(source, input, transition) && stateFilter.apply(succ)) {
			S2 succ2 = copyTransitionChecked(srcData, inputsMapping.apply(input), transition, succ);
			if(succ2 == null) {
				return TSTraversalAction.IGNORE;
			}
			outData.value = succ2;
			return TSTraversalAction.EXPLORE;
		}
		return TSTraversalAction.IGNORE;
	}

}

