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

import java.util.Collection;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.Mappings;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.UniversalTransitionSystem;
import net.automatalib.util.traversal.TraversalOrder;
import net.automatalib.util.ts.TS;
import net.automatalib.util.ts.traversal.TSTraversal;

public abstract class AutomatonCopy {

	
	
	public static <S1,I1,T1,S2,I2,T2,SP2,TP2>
	Mapping<S1,S2> copyPlain(Automaton<S1,I1,T1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,I2,T2,SP2,TP2> out,
			Mapping<? super I1,? extends I2> inputsMapping,
			Mapping<? super S1,? extends SP2> spMapping,
			Mapping<? super T1,? extends TP2> tpMapping) {
		PlainAutomatonCopy<S1,I1,T1,S2,I2,T2,SP2,TP2> copy = new PlainAutomatonCopy<>(in, inputs, out, inputsMapping, spMapping, tpMapping);
		copy.doCopy();
		return copy.stateMapping;
	}
	
	public static <S1,I1 extends I2,T1,S2,I2,SP2,TP2>
	Mapping<S1,S2> copyPlain(Automaton<S1,I1,T1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,I2,?,SP2,TP2> out,
			Mapping<? super S1,? extends SP2> spMapping,
			Mapping<? super T1,? extends TP2> tpMapping) {
		return copyPlain(in, inputs, out, Mappings.<I1>identity(), spMapping, tpMapping);
	}
	
	public static <S1,I1,T1,SP1,TP1,S2,I2,SP2,TP2>
	Mapping<S1,S2> copyUniversalPlain(UniversalAutomaton<S1,I1,T1,SP1,TP1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,I2,?,SP2,TP2> out,
			Mapping<? super I1,? extends I2> inputsMapping,
			Mapping<? super SP1,? extends SP2> spConversion,
			Mapping<? super TP1,? extends TP2> tpConversion) {
		Mapping<? super S1,? extends SP2> spMapping = Mappings.compose(TS.stateProperties(in), spConversion);
		Mapping<? super T1,? extends TP2> tpMapping = Mappings.compose(TS.transitionProperties(in), tpConversion);
		return copyPlain(in, inputs, out, inputsMapping, spMapping, tpMapping);
	}
	
	public static <S1,I1,SP1,TP1,S2,SP2,TP2>
	Mapping<S1,S2> copyUniversalPlain(UniversalAutomaton<S1,I1,?,SP1,TP1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,? super I1,?,SP2,TP2> out,
			Mapping<? super SP1,? extends SP2> spConversion,
			Mapping<? super TP1,? extends TP2> tpConversion) {
		return copyUniversalPlain(in, inputs, out, Mappings.<I1>identity(), spConversion, tpConversion);
	}
	
	public static <S1,I1,T1,SP1,TP1,S2,I2>
	Mapping<S1,S2> copyUniversalPlain(UniversalAutomaton<S1,I1,T1,SP1,TP1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,I2,?,? super SP1,? super TP1> out,
			Mapping<? super I1,? extends I2> inputsMapping) {
		return copyPlain(in, inputs, out, inputsMapping, TS.stateProperties(in), TS.transitionProperties(in));
	}
	
	public static <S1,I1,T1,SP1,TP1,S2>
	Mapping<S1,S2> copyUniversalPlain(UniversalAutomaton<S1,I1,T1,SP1,TP1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,? super I1,?,? super SP1,? super TP1> out) {
		return copyPlain(in, inputs, out, Mappings.<I1>identity(), TS.stateProperties(in), TS.transitionProperties(in));
	}
	
	
	public static <S1,I1,T1,S2,I2,T2,SP2,TP2>
	Mapping<S1,S2> copyTraversal(
			TraversalOrder order,
			int limit,
			TransitionSystem<S1,I1,T1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,I2,T2,SP2,TP2> out,
			Mapping<? super I1,? extends I2> inputsMapping,
			Mapping<? super S1,? extends SP2> spMapping,
			Mapping<? super T1,? extends TP2> tpMapping) {
		TraversalAutomatonCopy<S1,I1,T1,S2,I2,T2,SP2,TP2> copy = new TraversalAutomatonCopy<>(order, limit, in, inputs, out, inputsMapping, spMapping, tpMapping);
		copy.doCopy();
		return copy.stateMapping;
	}
	
	public static <S1,I1 extends I2,T1,S2,I2,SP2,TP2>
	Mapping<S1,S2> copyDfs(TransitionSystem<S1,I1,T1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,I2,?,SP2,TP2> out,
			Mapping<? super S1,? extends SP2> spMapping,
			Mapping<? super T1,? extends TP2> tpMapping) {
		return copyTraversal(TraversalOrder.DEPTH_FIRST, TSTraversal.NO_LIMIT, in, inputs, out, Mappings.<I1>identity(), spMapping, tpMapping);
	}
	
	public static <S1,I1,T1,SP1,TP1,S2,I2,SP2,TP2>
	Mapping<S1,S2> copyUniversalDfs(UniversalTransitionSystem<S1,I1,T1,SP1,TP1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,I2,?,SP2,TP2> out,
			Mapping<? super I1,? extends I2> inputsMapping,
			Mapping<? super SP1,? extends SP2> spConversion,
			Mapping<? super TP1,? extends TP2> tpConversion) {
		Mapping<? super S1,? extends SP2> spMapping = Mappings.compose(TS.stateProperties(in), spConversion);
		Mapping<? super T1,? extends TP2> tpMapping = Mappings.compose(TS.transitionProperties(in), tpConversion);
		return copyTraversal(TraversalOrder.DEPTH_FIRST, TSTraversal.NO_LIMIT, in, inputs, out, inputsMapping, spMapping, tpMapping);
	}
	
	public static <S1,I1,SP1,TP1,S2,SP2,TP2>
	Mapping<S1,S2> copyUniversalDfs(UniversalTransitionSystem<S1,I1,?,SP1,TP1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,? super I1,?,SP2,TP2> out,
			Mapping<? super SP1,? extends SP2> spConversion,
			Mapping<? super TP1,? extends TP2> tpConversion) {
		return copyUniversalDfs(in, inputs, out, Mappings.<I1>identity(), spConversion, tpConversion);
	}
	
	public static <S1,I1,T1,SP1,TP1,S2,I2>
	Mapping<S1,S2> copyUniversalDfs(UniversalTransitionSystem<S1,I1,T1,SP1,TP1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,I2,?,? super SP1,? super TP1> out,
			Mapping<? super I1,? extends I2> inputsMapping) {
		return copyTraversal(TraversalOrder.DEPTH_FIRST, TSTraversal.NO_LIMIT, in, inputs, out, inputsMapping, TS.stateProperties(in), TS.transitionProperties(in));
	}
	
	public static <S1,I1,T1,SP1,TP1,S2>
	Mapping<S1,S2> copyUniversalDfs(UniversalTransitionSystem<S1,I1,T1,SP1,TP1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,? super I1,?,? super SP1,? super TP1> out) {
		return copyTraversal(TraversalOrder.DEPTH_FIRST, TSTraversal.NO_LIMIT, in, inputs, out, Mappings.<I1>identity(), TS.stateProperties(in), TS.transitionProperties(in));
	}
	

	
	// TODO: Traversal copy
	
	
	private AutomatonCopy() {
	}

}
