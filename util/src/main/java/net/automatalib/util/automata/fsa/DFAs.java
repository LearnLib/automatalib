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
package net.automatalib.util.automata.fsa;

import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.util.ts.acceptors.Acceptors;
import net.automatalib.util.ts.copy.TSCopy;
import net.automatalib.util.ts.traversal.TSTraversalMethod;
import net.automatalib.words.Alphabet;

import com.google.common.base.Function;
import com.google.common.base.Functions;



/**
 * Operations on {@link DFA}s.
 * <p>
 * Note that the methods provided by this class do not modify their input arguments. Such methods
 * are instead provided by the {@link MutableDFAs} class.
 *  
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public abstract class DFAs {
	
	/*
	 * Function for negating a Boolean value.
	 */
	private static final Function<Boolean,Boolean> NEGATE
		= new Function<Boolean,Boolean>() {
			@Override
			public Boolean apply(Boolean elem) {
				return !elem;
			}
	};
	
	
	/**
	 * Most general way of combining two DFAs. The {@link AcceptanceCombiner} specified via the {@code combiner} parameter
	 * specifies how acceptance values of the DFAs will be combined to an acceptance value in the result DFA.
	 *  
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputs the input symbols to consider
	 * @param out the mutable DFA for storing the result
	 * @param combiner combination method for acceptance values
	 * @return {@code out}, for convenience
	 */
	public static <I,S,A extends MutableDFA<S,I>>
	A combine(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs,
			A out,
			AcceptanceCombiner combiner) {
		DeterministicAcceptorTS<?, I> acc
			= Acceptors.combine(dfa1, dfa2, combiner);
		
		TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, acc, -1, inputs, out);
		return out;
	}
	
	
	/**
	 * Most general way of combining two DFAs. The behavior is the same as of the above
	 * {@link #combine(DFA, DFA, Collection, MutableDFA, AcceptanceCombiner)}, but the result automaton
	 * is automatically created as a {@link CompactDFA}.
	 * 
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputAlphabet the input alphabet
	 * @param combiner combination method for acceptance values
	 * @return a new DFA representing the combination of the specified DFA
	 */
	public static <I> CompactDFA<I> combine(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Alphabet<I> inputAlphabet,
			AcceptanceCombiner combiner) {
		return combine(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet), combiner);
	}
	
	/**
	 * Calculates the conjunction ("and") of two DFA, and stores the result in a given mutable DFA.
	 * 
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputs the input symbols to consider
	 * @param out a mutable DFA for storing the result
	 * @return {@code out}, for convenience
	 */
	public static <I,S,A extends MutableDFA<S,I>> 
	A and(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs, A out) {
		return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.AND);
	}
	
	/**
	 * Calculates the conjunction ("and") of two DFA, and returns the result as a new DFA.
	 * 
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputAlphabet the input alphabet
	 * @return a new DFA representing the conjunction of the specified DFA
	 */
	public static <I>
	CompactDFA<I> and(DFA<?,I> dfa1, DFA<?,I> dfa2, Alphabet<I> inputAlphabet) {
		return and(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet));
	}
	
	/**
	 * Calculates the disjunction ("or") of two DFA, and stores the result in a given mutable DFA.
	 * 
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputs the input symbols to consider
	 * @param out a mutable DFA for storing the result
	 * @return {@code out}, for convenience
	 */
	public static <I,S,A extends MutableDFA<S,I>>
	A or(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs, A out) {
		return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.OR);
	}
	
	/**
	 * Calculates the disjunction ("or") of two DFA, and returns the result as a new DFA.
	 * 
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputAlphabet the input alphabet
	 * @return a new DFA representing the conjunction of the specified DFA
	 */
	public static <I>
	CompactDFA<I> or(DFA<?,I> dfa1, DFA<?,I> dfa2, Alphabet<I> inputAlphabet) {
		return or(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet));
	}
	
	/**
	 * Calculates the exclusive-or ("xor") of two DFA, and stores the result in a given mutable DFA.
	 * 
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputs the input symbols to consider
	 * @param out a mutable DFA for storing the result
	 * @return {@code out}, for convenience
	 */
	public static <I,S,A extends MutableDFA<S,I>>
	A xor(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs, A out) {
		return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.XOR);
	}
	
	/**
	 * Calculates the exclusive-or ("xor") of two DFA, and returns the result as a new DFA.
	 * 
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputAlphabet the input alphabet
	 * @return a new DFA representing the conjunction of the specified DFA
	 */
	public static <I>
	CompactDFA<I> xor(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Alphabet<I> inputAlphabet) {
		return xor(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet));
	}
	
	/**
	 * Calculates the equivalence ("<=>") of two DFA, and stores the result in a given mutable DFA.
	 * 
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputs the input symbols to consider
	 * @param out a mutable DFA for storing the result
	 * @return {@code out}, for convenience
	 */
	public static <I,S,A extends MutableDFA<S,I>>
	A equiv(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs, A out) {
		return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.EQUIV);
	}
	
	/**
	 * Calculates the equivalence ("<=>") of two DFA, and returns the result as a new DFA.
	 * 
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputAlphabet the input alphabet
	 * @return a new DFA representing the conjunction of the specified DFA
	 */
	public static <I>
	CompactDFA<I> equiv(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Alphabet<I> inputAlphabet) {
		return equiv(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet));
	}
	
	/**
	 * Calculates the implication ("=>") of two DFA, and stores the result in a given mutable DFA.
	 * 
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputs the input symbols to consider
	 * @param out a mutable DFA for storing the result
	 * @return {@code out}, for convenience
	 */
	public static <I,S,A extends MutableDFA<S,I>>
	A impl(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs, A out) {
		return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.IMPL);
	}
	
	/**
	 * Calculates the implication ("=>") of two DFA, and returns the result as a new DFA.
	 * 
	 * @param dfa1 the first DFA
	 * @param dfa2 the second DFA
	 * @param inputAlphabet the input alphabet
	 * @return a new DFA representing the conjunction of the specified DFA
	 */
	public static <I>
	CompactDFA<I> impl(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Alphabet<I> inputAlphabet) {
		return impl(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet));
	}
	
	
	/**
	 * Calculates the complement (negation) of a DFA, and stores the result in a given mutable DFA.
	 * <p>
	 * Note that unlike {@link MutableDFA#flipAcceptance()}, undefined transitions are treated as
	 * leading to a rejecting sink state (and are thus turned into an accepting sink).
	 * 
	 * @param dfa the DFA to complement
	 * @param inputs the input symbols to consider
	 * @param out a mutable DFA for storing the result
	 * @return {@code out}, for convenience
	 */
	public static <I,S,A extends MutableDFA<S,I>>
	A complement(DFA<?,I> dfa,
			Collection<? extends I> inputs, A out) {
		AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, dfa, inputs, out, NEGATE, Functions.constant((Void)null));
		MutableDFAs.complete(out, inputs, false, true);
		return out;
	}
	
	
	/**
	 * Calculates the complement (negation) of a DFA, and returns the result as a new DFA.
	 * <p>
	 * Note that unlike {@link MutableDFA#flipAcceptance()}, undefined transitions are treated as
	 * leading to a rejecting sink state (and are thus turned into an accepting sink).
	 * 
	 * @param dfa the DFA to complement
	 * @param inputAlphabet the input alphabet
	 * @return a new DFA representing the complement of the specified DFA
	 */
	public static <I>
	CompactDFA<I> complement(DFA<?,I> dfa,
			Alphabet<I> inputAlphabet) {
		return complement(dfa, inputAlphabet, new CompactDFA<>(inputAlphabet));
	}
	
	
	public static <I,S,A extends MutableDFA<S,I>>
	A complete(DFA<?,I> dfa, Collection<? extends I> inputs, A out) {
		AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, dfa, inputs, out);
		MutableDFAs.complete(out, inputs, true);
		return out;
	}
	
	public static <I>
	CompactDFA<I> complete(DFA<?,I> dfa, Alphabet<I> inputs) {
		return complete(dfa, inputs, new CompactDFA<>(inputs));
	}
			

	private DFAs() {
		throw new IllegalStateException("Constructor should never be invoked");
	}

}
