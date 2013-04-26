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
package net.automatalib.util.automata.fsa;

import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.Mappings;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.util.ts.acceptors.Acceptors;
import net.automatalib.words.Alphabet;


public abstract class DFAs {
	
	private static final Mapping<Boolean,Boolean> NEGATE
		= new Mapping<Boolean,Boolean>() {
			@Override
			public Boolean get(Boolean elem) {
				return !elem;
			}
	};
	
	public static <I,S,A extends MutableDFA<S,I>>
	A combine(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs,
			A out,
			AcceptanceCombiner combiner) {
		DeterministicAcceptorTS<?, I> acc
			= Acceptors.combine(dfa1, dfa2, combiner);
		
		return Automata.copyUniversal(acc, inputs, out, Mappings.<I>identity());
	}
	
	
	
	public static <I> FastDFA<I> combine(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Alphabet<I> inputAlphabet,
			AcceptanceCombiner combiner) {
		return combine(dfa1, dfa2, inputAlphabet, new FastDFA<I>(inputAlphabet), combiner);
	}
	
	public static <I,S,A extends MutableDFA<S,I>> 
	A and(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs, A out) {
		return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.AND);
	}
	
	public static <I>
	FastDFA<I> and(DFA<?,I> dfa1, DFA<?,I> dfa2, Alphabet<I> inputAlphabet) {
		return and(dfa1, dfa2, inputAlphabet, new FastDFA<I>(inputAlphabet));
	}
	
	public static <I,S,A extends MutableDFA<S,I>>
	A or(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs, A out) {
		return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.OR);
	}
	
	public static <I>
	FastDFA<I> or(DFA<?,I> dfa1, DFA<?,I> dfa2, Alphabet<I> inputAlphabet) {
		return or(dfa1, dfa2, inputAlphabet, new FastDFA<I>(inputAlphabet));
	}
	
	public static <I,S,A extends MutableDFA<S,I>>
	A xor(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs, A out) {
		return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.XOR);
	}
	
	public static <I>
	FastDFA<I> xor(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Alphabet<I> inputAlphabet) {
		return xor(dfa1, dfa2, inputAlphabet, new FastDFA<I>(inputAlphabet));
	}
	
	public static <I,S,A extends MutableDFA<S,I>>
	A equiv(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs, A out) {
		return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.EQUIV);
	}
	public static <I> FastDFA<I> equiv(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Alphabet<I> inputAlphabet) {
		return equiv(dfa1, dfa2, inputAlphabet, new FastDFA<I>(inputAlphabet));
	}
	
	public static <I,S,A extends MutableDFA<S,I>>
	A impl(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Collection<? extends I> inputs, A out) {
		return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.IMPL);
	}
	
	public static <I> FastDFA<I> impl(DFA<?,I> dfa1, DFA<?,I> dfa2,
			Alphabet<I> inputAlphabet) {
		return impl(dfa1, dfa2, inputAlphabet, new FastDFA<I>(inputAlphabet));
	}
	
	public static <I,S,A extends MutableDFA<S,I>> A complement(DFA<?,I> dfa,
			Collection<? extends I> inputs, A out) {
		return Automata.copyUniversal(dfa, inputs, out, Mappings.<I>identity(), NEGATE, Mappings.<Void,Void>nullMapping());
	}
	
	public static <I> FastDFA<I> complement(DFA<?,I> dfa,
			Alphabet<I> inputAlphabet) {
		return complement(dfa, inputAlphabet, new FastDFA<I>(inputAlphabet));
	}
	
	
	


}
