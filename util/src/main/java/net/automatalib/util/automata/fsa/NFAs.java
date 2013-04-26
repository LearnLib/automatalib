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



public abstract class NFAs {

	/*
	public static <I> FastDFA<I> determinize(NFA<?,I> nfa,
			Alphabet<I> inputAlphabet, boolean partial) {
		FastDFA<I> out = new FastDFA<I>(inputAlphabet);
		doDeterminize(nfa, out, inputAlphabet);
		return out;
	}
	
	public static <I> void determinize(NFA<?,I> nfa,
			MutableDFA<?,I> out,
			Collection<? extends I> inputs, boolean partial) {
		doDeterminize(nfa, out, inputs);
	}
	
	private static <SI,SO,I> void doDeterminize(NFA<SI,I> nfa,
			MutableDFA<SO,I> out,
			Collection<? extends I> inputs) {
		Automata.cop
		Automata.genericCopy(nfa.powersetView(),
				inputs,
				out,
				Mappings.<I>identity(),
				Acceptors.existentialAcceptance(nfa),
				Mappings.<Void,Void>nullMapping());
	}
	
	*/
	
	private NFAs() {}
}
