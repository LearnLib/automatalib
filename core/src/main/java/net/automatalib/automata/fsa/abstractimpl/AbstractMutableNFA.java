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

import java.util.Collection;

import net.automatalib.automata.abstractimpl.AbstractMutableAutomaton;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.fsa.MutableNFA;


public abstract class AbstractMutableNFA<S, I> extends
		AbstractMutableAutomaton<S, I, S, Boolean, Void> implements
		MutableNFA<S, I>, DOTPlottableAutomaton<S, I, S> {

	@Override
	public boolean isAccepting(Collection<? extends S> states) {
		return AbstractNFA.isAccepting(this, states);
	}

	@Override
	public S getSuccessor(S transition) {
		return AbstractNFA.getSuccessor(this, transition);
	}


	@Override
	public boolean accepts(Iterable<I> input) {
		return AbstractNFA.accepts(this, input);
	}


	@Override
	public void setStateProperty(S state, Boolean property) {
		AbstractMutableFSA.setStateProperty(this, state, property);
	}

	@Override
	public void setTransitionProperty(S transition, Void property) {
		AbstractMutableFSA.setTransitionProperty(this, transition, property);
	}
	
	

	@Override
	public S createTransition(S successor, Void properties) {
		return AbstractMutableFSA.createTransition(this, successor, properties);
	}


	@Override
	public S copyTransition(S trans, S succ) {
		return AbstractMutableFSA.copyTransition(this, trans, succ);
	}
	
	@Override
	public void flipAcceptance() {
		AbstractMutableFSA.flipAcceptance(this);
	}

	

}
