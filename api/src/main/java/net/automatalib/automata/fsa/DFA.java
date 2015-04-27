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
package net.automatalib.automata.fsa;

import java.util.Collection;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.DetSuffixOutputAutomaton;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;

/**
 * Deterministic finite state acceptor
 */
public interface DFA<S,I> extends
		UniversalDeterministicAutomaton<S,I,S,Boolean,Void>,
		DeterministicAcceptorTS<S, I>,
		DetSuffixOutputAutomaton<S, I, S, Boolean>,
        NFA<S,I> {
	
	@Override
	default public boolean accepts(Iterable<? extends I> input) {
		S tgt = getState(input);
		if(tgt == null)
			return false;
		return isAccepting(tgt);
	}
	
	default public boolean isAccepting(Collection<? extends S> states) {
		return DeterministicAcceptorTS.super.isAccepting(states);
	}
	
	@Override
	default public Boolean computeStateOutput(S state, Iterable<? extends I> input) {
		S tgt = getSuccessor(state, input);
		if (tgt == null) {
			return false;
		}
		return isAccepting(tgt);
	}
	
	@Override
	default public Boolean computeOutput(Iterable<? extends I> input) {
		return accepts(input);
	}
	
	@Override
	default public Boolean computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
		return DetSuffixOutputAutomaton.super.computeSuffixOutput(prefix, suffix);
	}
}
