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
package net.automatalib.util.automata;

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.Mappings;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.util.ts.traversal.TSTraversalVisitor;
import net.automatalib.util.ts.traversal.TraversalAction;

class CopyVisitor<S1, I1, T1, S2, I2, T2, SP2, TP2>
		implements TSTraversalVisitor<S1, I1, T1, S2> {

	private final TransitionSystem<S1, I1, T1> in;
	private final MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out;
	private final Mapping<? super I1, ? extends I2> inputsMapping;
	private final Mapping<? super S1, ? extends SP2> statePropMapping;
	private final Mapping<? super T1, ? extends TP2> transPropMapping;

	private final MutableMapping<S1, S2> stateMap;

	public CopyVisitor(TransitionSystem<S1, I1, T1> in,
			MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
			Mapping<? super I1, ? extends I2> inputsMapping,
			Mapping<? super S1, ? extends SP2> statePropMapping,
			Mapping<? super T1, ? extends TP2> transPropMapping) {
		this.in = in;
		this.out = out;
		this.inputsMapping = inputsMapping;
		if (statePropMapping == null)
			statePropMapping = Mappings.nullMapping();
		this.statePropMapping = statePropMapping;
		if (transPropMapping == null)
			transPropMapping = Mappings.nullMapping();
		this.transPropMapping = transPropMapping;
		this.stateMap = in.createStaticStateMapping();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ls5.ts.traversal.TSTraversalVisitor#processInitial(java.lang.Object
	 * )
	 */
	@Override
	public TraversalAction<S2> processInitial(S1 state) {
		SP2 newProps = statePropMapping.get(state);
		S2 newState = out.addInitialState(newProps);
		stateMap.put(state, newState);
		return TSTraversal.explore(newState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ls5.ts.traversal.TSTraversalVisitor#startExploration(java.lang
	 * .Object, java.lang.Object)
	 */
	@Override
	public boolean startExploration(S1 state, S2 data) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ls5.ts.traversal.TSTraversalVisitor#processTransition(java.lang
	 * .Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public TraversalAction<S2> processTransition(S1 source, S2 srcData,
			I1 input, T1 transition) {
		S1 tgt = in.getSuccessor(transition);

		boolean explore = false;
		S2 newTgt = stateMap.get(tgt);
		if (newTgt == null) {
			SP2 newProps = statePropMapping.get(source);
			newTgt = out.addState(newProps);
			stateMap.put(tgt, newTgt);
			explore = true;
		}

		TP2 newTransProps = transPropMapping.get(transition);
		I2 newInput = inputsMapping.get(input);

		T2 newTrans = out.createTransition(newTgt, newTransProps);
		out.addTransition(srcData, newInput, newTrans);

		if (explore)
			return TSTraversal.explore(newTgt);
		return TSTraversal.ignore();
	}

}