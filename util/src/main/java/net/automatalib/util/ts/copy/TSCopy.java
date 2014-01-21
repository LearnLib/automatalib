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
package net.automatalib.util.ts.copy;

import java.util.Collection;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.UniversalTransitionSystem;
import net.automatalib.util.automata.predicates.TransitionPredicates;
import net.automatalib.util.ts.TS;
import net.automatalib.util.ts.traversal.TSTraversalMethod;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class TSCopy {
	/**
	 * Copies an {@link TransitionSystem} to a {@link MutableAutomaton} with possibly heterogeneous input alphabets and
	 * state and transition properties.
	 * 
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @param inputsMapping the transformation for input symbols
	 * @param spMapping the function for obtaining state properties
	 * @param tpMapping the function for obtaining transition properties
	 * @param stateFilter the filter predicate for states
	 * @param transFilter the filter predicate for transitions
	 * @return a mapping from old to new states
	 */
	public static <S1,I1,T1,S2,I2,T2,SP2,TP2>
	Mapping<S1,S2> rawCopy(TSTraversalMethod method,
			TransitionSystem<S1,? super I1,T1> in,
			int limit,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,I2,T2,? super SP2,? super TP2> out,
			Function<? super I1,? extends I2> inputsMapping,
			Function<? super S1,? extends SP2> spMapping,
			Function<? super T1,? extends TP2> tpMapping,
			Predicate<? super S1> stateFilter,
			TransitionPredicate<? super S1, ? super I1, ? super T1> transFilter) {
		if(spMapping == null) {
			spMapping = Functions.constant(null);
		}
		if(tpMapping == null) {
			tpMapping = Functions.constant(null);
		}
		if(stateFilter == null) {
			stateFilter = Predicates.alwaysTrue();
		}
		if(transFilter == null) {
			transFilter = TransitionPredicates.alwaysTrue();
		}
		
		TSCopyVisitor<S1,I1,T1,S2,I2,T2,SP2,TP2> vis = new TSCopyVisitor<>(in, out, inputsMapping, spMapping, tpMapping, stateFilter, transFilter);
		
		method.traverse(in, limit, inputs, vis);
		return vis.getStateMapping();
	}
	
	/**
	 * Copies an {@link Automaton} to a {@link MutableAutomaton} with possibly heterogeneous input alphabets and
	 * state and transition properties. State and transitions will not be filtered.
	 * 
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @param inputsMapping the transformation for input symbols
	 * @param spMapping the function for obtaining state properties
	 * @param tpMapping the function for obtaining transition properties
	 * @return a mapping from old to new states
	 */
	public static <S1,I1,T1,S2,I2,T2,SP2,TP2>
	Mapping<S1,S2> rawCopy(TSTraversalMethod method,
			TransitionSystem<S1,? super I1,T1> in,
			int limit,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2,I2,T2,? super SP2,? super TP2> out,
			Function<? super I1,? extends I2> inputsMapping,
			Function<? super S1,? extends SP2> spMapping,
			Function<? super T1,? extends TP2> tpMapping) {
		return rawCopy(method, in, limit, inputs, out, inputsMapping, spMapping, tpMapping, Predicates.alwaysTrue(), TransitionPredicates.alwaysTrue());
	}
	
	/**
	 * Copies an {@link Automaton} to a {@link MutableAutomaton} with a compatible input alphabet, but possibly heterogeneous state and
	 * transition properties.
	 * 
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @param spMapping the function for obtaining state properties
	 * @param tpMapping the function for obtaining transition properties
	 * @param stateFilter the filter predicate for states
	 * @param transFilter the filter predicate for transitions
	 * @return a mapping from old to new states
	 */
	public static <S1,I,T1,S2,T2,SP2,TP2>
	Mapping<S1,S2> rawCopy(TSTraversalMethod method,
			TransitionSystem<S1,? super I,T1> in,
			int limit,
			Collection<? extends I> inputs,
			MutableAutomaton<S2,I,T2,SP2,TP2> out,
			Function<? super S1,? extends SP2> spMapping,
			Function<? super T1,? extends TP2> tpMapping,
			Predicate<? super S1> stateFilter,
			TransitionPredicate<? super S1, ? super I, ? super T1> transFilter) {
		return rawCopy(method, in, limit, inputs, out, Functions.<I>identity(), spMapping, tpMapping, stateFilter, transFilter);
	}
	
	/**
	 * Copies an {@link Automaton} to a {@link MutableAutomaton} with a compatible input alphabet, but possibly heterogeneous state and
	 * transition properties. States and transitions will not be filtered.
	 * 
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @param spMapping the function for obtaining state properties
	 * @param tpMapping the function for obtaining transition properties
	 * @param stateFilter the filter predicate for states
	 * @param transFilter the filter predicate for transitions
	 * @return a mapping from old to new states
	 */
	public static <S1,I,T1,S2,T2,SP2,TP2>
	Mapping<S1,S2> rawCopy(TSTraversalMethod method,
			TransitionSystem<S1,? super I,T1> in,
			int limit,
			Collection<? extends I> inputs,
			MutableAutomaton<S2,I,T2,SP2,TP2> out,
			Function<? super S1,? extends SP2> spMapping,
			Function<? super T1,? extends TP2> tpMapping) {
		return rawCopy(method, in, limit, inputs, out, spMapping, tpMapping);
	}
	
	/**
	 * Copies a {@link UniversalAutomaton} to a {@link MutableAutomaton} with possibly heterogeneous input alphabets and state and transition
	 * properties.
	 * 
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @param inputsMapping the transformation for input symbols
	 * @param spTransform the transformation for state properties
	 * @param tpTransform the transformation for transition properties
	 * @param stateFilter the filter predicate for states
	 * @param transFilter the filter predicate for transitions
	 * @return a mapping from old to new states
	 */
	public static <S1,I1,T1,SP1,TP1,S2,I2,T2,SP2,TP2>
	Mapping<S1,S2> copy(TSTraversalMethod method,
			UniversalTransitionSystem<S1,? super I1,T1,? extends SP1, ? extends TP1> in,
			int limit,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
			Function<? super I1,? extends I2> inputsMapping,
			Function<? super SP1,? extends SP2> spTransform,
			Function<? super TP1,? extends TP2> tpTransform,
			Predicate<? super S1> stateFilter,
			TransitionPredicate<? super S1,? super I1, ? super T1> transFilter) {
		Function<? super S1,? extends SP2> spMapping = (spTransform == null) ? null : Functions.compose(spTransform, TS.stateProperties(in));
		Function<? super T1,? extends TP2> tpMapping = (tpTransform == null) ? null : Functions.compose(tpTransform, TS.transitionProperties(in));
		return rawCopy(method, in, limit, inputs, out, inputsMapping, spMapping, tpMapping, stateFilter, transFilter);
	}
	
	/**
	 * Copies a {@link UniversalAutomaton} to a {@link MutableAutomaton} with possibly heterogeneous input alphabets and state and transition
	 * properties. States and transitions will not be filtered
	 * 
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @param inputsMapping the transformation for input symbols
	 * @param spTransform the transformation for state properties
	 * @param tpTransform the transformation for transition properties
	 * @return a mapping from old to new states
	 */
	public static <S1,I1,T1,SP1,TP1,S2,I2,T2,SP2,TP2>
	Mapping<S1,S2> copy(TSTraversalMethod method,
			UniversalTransitionSystem<S1,? super I1,T1,? extends SP1, ? extends TP1> in,
			int limit,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
			Function<? super I1,? extends I2> inputsMapping,
			Function<? super SP1,? extends SP2> spTransform,
			Function<? super TP1,? extends TP2> tpTransform) {
		return copy(method, in, limit, inputs, out, inputsMapping, spTransform, tpTransform, Predicates.alwaysTrue(), TransitionPredicates.alwaysTrue());
	}
	
	/**
	 * Copies a {@link UniversalAutomaton} to a {@link MutableAutomaton} with compatible input alphabets, but possibly heterogeneous
	 * properties.
	 *  
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @param spTransform the transformation for state properties
	 * @param tpTransform the transformation for transition properties
	 * @param stateFilter the filter predicate for states
	 * @param transFilter the filter predicate for transitions
	 * @return a mapping from old to new states
	 */
	public static <S1,I,T1,SP1,TP1,S2,I2,T2,SP2,TP2>
	Mapping<S1,S2> copy(TSTraversalMethod method,
			UniversalTransitionSystem<S1,? super I,T1,? extends SP1, ? extends TP1> in,
			int limit,
			Collection<? extends I> inputs,
			MutableAutomaton<S2, I, T2, ? super SP2, ? super TP2> out,
			Function<? super SP1,? extends SP2> spTransform,
			Function<? super TP1,? extends TP2> tpTransform,
			Predicate<? super S1> stateFilter,
			TransitionPredicate<? super S1, ? super I, ? super T1> transFilter) {
		return copy(method, in, limit, inputs, out, Functions.<I>identity(), spTransform, tpTransform, stateFilter, transFilter);
	}
	
	/**
	 * Copies a {@link UniversalAutomaton} to a {@link MutableAutomaton} with compatible input alphabets, but possibly heterogeneous
	 * properties. States and transitions will not be filtered. 
	 *  
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @param spTransform the transformation for state properties
	 * @param tpTransform the transformation for transition properties
	 * @return a mapping from old to new states
	 */
	public static <S1,I,T1,SP1,TP1,S2,I2,T2,SP2,TP2>
	Mapping<S1,S2> copy(TSTraversalMethod method,
			UniversalTransitionSystem<S1,? super I,T1,? extends SP1, ? extends TP1> in,
			int limit,
			Collection<? extends I> inputs,
			MutableAutomaton<S2, I, T2, ? super SP2, ? super TP2> out,
			Function<? super SP1,? extends SP2> spTransform,
			Function<? super TP1,? extends TP2> tpTransform) {
		return copy(method, in, limit, inputs, out, spTransform, tpTransform, Predicates.alwaysTrue(), TransitionPredicates.alwaysTrue());
	}
	
	/**
	 * Copies a {@link UniversalAutomaton} with possibly heterogeneous input alphabets, but compatible properties.
	 * 
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @param inputsMapping the transformation for input symbols
	 * @param stateFilter the filter predicate for states
	 * @param transFilter the filter predicate for transitions
	 * @return a mapping from old to new states
	 */
	public static <S1,I1,T1,SP,TP,S2,I2,T2>
	Mapping<S1,S2> copy(TSTraversalMethod method,
			UniversalTransitionSystem<S1,? super I1,T1,? extends SP, ? extends TP> in,
			int limit,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2, I2, T2, ? super SP, ? super TP> out,
			Function<? super I1,? extends I2> inputsMapping,
			Predicate<? super S1> stateFilter,
			TransitionPredicate<? super S1, ? super I1, ? super T1> transFilter) {
		return copy(method, in, limit, inputs, out, inputsMapping, Functions.<SP>identity(), Functions.<TP>identity(), stateFilter, transFilter);
	}
	
	/**
	 * Copies a {@link UniversalAutomaton} with possibly heterogeneous input alphabets, but compatible properties. States and
	 * transitions will not be filtered
	 * 
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @param inputsMapping a mapping from inputs in the input automaton to inputs in the output automaton
	 * @return a mapping from old to new states
	 */
	public static <S1,I1,T1,SP,TP,S2,I2,T2>
	Mapping<S1,S2> copy(TSTraversalMethod method,
			UniversalTransitionSystem<S1,? super I1,T1,? extends SP, ? extends TP> in,
			int limit,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2, I2, T2, ? super SP, ? super TP> out,
			Function<? super I1,? extends I2> inputsMapping) {
		return copy(method, in, limit, inputs, out, inputsMapping, Predicates.alwaysTrue(), TransitionPredicates.alwaysTrue());
	}
	
	/**
	 * Copies a {@link UniversalAutomaton} with compatible input alphabets and properties.
	 * 
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @param stateFilter the filter predicate for states
	 * @param transFilter the filter predicate for transitions
	 * @return a mapping from old to new states
	 */
	public static <S1,I,T1,SP,TP,S2,T2>
	Mapping<S1,S2> copy(TSTraversalMethod method,
			UniversalTransitionSystem<S1,? super I,T1,? extends SP, ? extends TP> in,
			int limit,
			Collection<? extends I> inputs,
			MutableAutomaton<S2, I,	T2, ? super SP, ? super TP> out,
			Predicate<? super S1> stateFilter,
			TransitionPredicate<? super S1, ? super I, ? super T1> transFilter) {
		return copy(method, in, limit, inputs, out, Functions.<I>identity(), stateFilter, transFilter);
	}
	
	/**
	 * Copies a {@link UniversalAutomaton} with compatible input alphabets and properties. States and transitions
	 * will not be filtered.
	 * 
	 * @param method the traversal method to use
	 * @param in the input transition system
	 * @param limits the traversal limit, a value less than 0 means no limit
	 * @param inputs the inputs to consider
	 * @param out the output automaton
	 * @return a mapping from old to new states.
	 */
	public static <S1,I,T1,SP,TP,S2,T2>
	Mapping<S1,S2> copy(TSTraversalMethod method,
			UniversalTransitionSystem<S1,? super I,T1,? extends SP, ? extends TP> in,
			int limit,
			Collection<? extends I> inputs,
			MutableAutomaton<S2, I,	T2, ? super SP, ? super TP> out) {
		return copy(method, in, limit, inputs, out, Predicates.alwaysTrue(), TransitionPredicates.alwaysTrue());
	}
	
	
	private TSCopy() {
		throw new IllegalStateException("Constructor should never be invoked");
	}

}
