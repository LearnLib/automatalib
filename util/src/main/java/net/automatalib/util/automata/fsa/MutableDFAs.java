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
package net.automatalib.util.automata.fsa;

import java.util.Collection;

import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.util.automata.Automata;

public abstract class MutableDFAs {
	
	
	public static <S,I>
	void complete(MutableDFA<S,I> dfa, Collection<? extends I> inputs) {
		complete(dfa, inputs, false);
	}
	
	public static <S,I>
	void complete(MutableDFA<S,I> dfa, Collection<? extends I> inputs, boolean minimize) {
		complete(dfa, inputs, minimize, false);
	}
	
	public static <S,I>
	void complete(MutableDFA<S,I> dfa, Collection<? extends I> inputs, boolean minimize, boolean undefinedAcceptance) {
		S sink = null;
		
		for(S state : dfa) {
			for(I input : inputs) {
				S succ = dfa.getSuccessor(state, input);
				if(succ == null) {
					if(sink == null) {
						sink = dfa.addState(undefinedAcceptance);
						for(I inputSym : inputs) {
							dfa.addTransition(sink, inputSym, sink);
						}
					}
					dfa.addTransition(state, input, sink);
				}
			}
		}
		
		if(minimize) {
			Automata.invasiveMinimize(dfa, inputs);
		}
	}
	
	public static <S,I>
	void complement(MutableDFA<S,I> dfa, Collection<? extends I> inputs) {
		dfa.flipAcceptance();
		complete(dfa, inputs, false, true);
	}
	
	private MutableDFAs() {
		throw new IllegalStateException("Constructor should never be invoked");
	}
}
