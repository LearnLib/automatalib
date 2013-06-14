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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.ts.TransitionSystem;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public abstract class AbstractLowLevelAutomatonCopier<S1, I1, T1, S2, I2, T2, SP2, TP2, TS1 extends TransitionSystem<S1, ? super I1, T1>> implements LowLevelAutomatonCopier<S1, I1, T1, S2, I2, T2, SP2, TP2> {

	protected final TS1 in;
	protected final Collection<? extends I1> inputs;
	protected final MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out;
	protected final MutableMapping<S1,S2> stateMapping;
	protected final Function<? super I1,? extends I2> inputsMapping;
	protected final Function<? super S1,? extends SP2> spMapping;
	protected final Function<? super T1,? extends TP2> tpMapping;
	protected final Predicate<? super S1> stateFilter;
	protected final TransitionPredicate<? super S1, ? super I1, ? super T1> transFilter;
	
	
	public AbstractLowLevelAutomatonCopier(TS1 in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
			Function<? super I1, ? extends I2> inputsMapping,
			Function<? super S1, ? extends SP2> spMapping,
			Function<? super T1, ? extends TP2> tpMapping,
			Predicate<? super S1> stateFilter,
			TransitionPredicate<? super S1, ? super I1, ? super T1> transFilter) {
		this.in = in;
		this.inputs = inputs;
		this.out = out;
		this.stateMapping = in.createStaticStateMapping();
		this.inputsMapping = inputsMapping;
		this.spMapping = spMapping;
		this.tpMapping = tpMapping;
		this.stateFilter = stateFilter;
		this.transFilter = transFilter;
	}
	
	@Override
	public Mapping<S1,S2> getStateMapping() {
		return stateMapping;
	}

	protected S2 copyState(S1 s1) {
		SP2 prop = spMapping.apply(s1);
		S2 s2 = out.addState(prop);
		stateMapping.put(s1, s2);
		return s2;
	}
	
	protected S2 copyInitialState(S1 s1) {
		SP2 prop = spMapping.apply(s1);
		S2 s2 = out.addInitialState(prop);
		stateMapping.put(s1, s2);
		return s2;
	}
	
	protected T2 copyTransition(S2 src2, I2 input2, T1 trans1, S1 succ1) {
		TP2 prop = tpMapping.apply(trans1);
		
		S2 succ2 = stateMapping.get(succ1);
		
		T2 trans2 = out.createTransition(succ2, prop);
		out.addTransition(src2, input2, trans2);
		return trans2;
	}
	
	protected void copyTransitions(S2 src2, I2 input2, Iterable<? extends T1> transitions1) {
		List<T2> transitions2 = new ArrayList<>();
		
		for(T1 trans1 : transitions1) {
			S1 succ1 = in.getSuccessor(trans1);
			S2 succ2 = stateMapping.get(succ1);
			TP2 prop = tpMapping.apply(trans1);
			T2 trans2 = out.createTransition(succ2, prop);
			transitions2.add(trans2);
		}
		
		out.addTransitions(src2, input2, transitions2);
	}
	
	protected S2 copyTransitionChecked(S2 src2, I2 input2, T1 trans1, S1 succ1) {
		TP2 prop = tpMapping.apply(trans1);
		
		S2 succ2 = stateMapping.get(succ1);
		S2 freshSucc = null;
		if(succ2 == null) {
			freshSucc = succ2 = copyState(succ1);
		}
		
		T2 trans2 = out.createTransition(succ2, prop);
		out.addTransition(src2, input2, trans2);
		return freshSucc;
	}
	
	/* (non-Javadoc)
	 * @see net.automatalib.util.automata.copy.AutomatonCopier#doCopy()
	 */
	@Override
	public abstract void doCopy();
	
	protected final void updateInitials() {
		for(S1 init1 : in.getInitialStates()) {
			S2 init2 = stateMapping.get(init1);
			if(init2 == null) {
				continue;
			}
			out.setInitial(init2, true);
		}
	}
}
