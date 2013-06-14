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
package net.automatalib.automata.fsa.abstractimpl;

import net.automatalib.automata.abstractimpl.AbstractDeterministicAutomaton;
import net.automatalib.automata.fsa.DFA;

public abstract class AbstractDFA<S, I> extends AbstractDeterministicAutomaton<S, I, S>
		implements DFA<S, I> {

	public static <S,I> boolean accepts(DFA<S, I> $this, Iterable<I> input) {
		S tgt = $this.getState(input);
		if(tgt == null)
			return false;
		return $this.isAccepting(tgt);
	}
	
	
	

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.acceptors.AcceptorTS#accepts(java.lang.Iterable)
	 */
	@Override
	public boolean accepts(Iterable<I> input) {
		return accepts(this, input);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.UniversalTransitionSystem#getStateProperty(java.lang.Object)
	 */
	@Override
	public Boolean getStateProperty(S state) {
		return AbstractFSA.getStateProperty(this, state);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.UniversalTransitionSystem#getTransitionProperty(java.lang.Object)
	 */
	@Override
	public Void getTransitionProperty(S transition) {
		return AbstractFSA.getTransitionProperty(this, transition);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.TransitionSystem#getSuccessor(java.lang.Object)
	 */
	@Override
	public S getSuccessor(S transition) {
		return AbstractFSA.getSuccessor(this, transition);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.SODetOutputAutomaton#computeSuffixOutput(java.lang.Iterable, java.lang.Iterable)
	 */
	@Override
	public Boolean computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
		return AbstractFSA.computeSuffixOutput(this, prefix, suffix);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.OutputAutomaton#computeOutput(java.lang.Iterable)
	 */
	@Override
	public Boolean computeOutput(Iterable<I> input) {
		return AbstractFSA.computeOutput(this, input);
	}
	
}
