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
package net.automatalib.automata.fsa;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.MutableAutomaton;


/**
 *
 * @author fh
 */
@ParametersAreNonnullByDefault
public interface MutableFSA<S,I> extends FiniteStateAcceptor<S,I>,
		MutableAutomaton<S, I, S, Boolean, Void> {
	
	@Override
	default public void setStateProperty(S state, Boolean property) {
		boolean acc = (property != null) ? property.booleanValue() : false;
		setAccepting(state, acc);
	}

	@Override
	default public void setTransitionProperty(S transition, Void property) {}

	default public void flipAcceptance() {
		for (S state : this)
			setAccepting(state, !isAccepting(state));
	}
	
	@Override
	default public S addState() {
		return addState(false);
	}

	@Override
	default public S addState(Boolean property) {
		boolean acc = (property != null) ? property.booleanValue() : false;
		return addState(acc);
	}

	@Override
	default public S addInitialState() {
		return addInitialState(false);
	}
	
	@Override
	default public S addInitialState(Boolean property) {
		boolean acc = (property != null) ? property.booleanValue() : false;
		return addInitialState(acc);
	}
	
	@Override
	default public S createTransition(S successor, Void properties) {
		return successor;
	}

	@Override
	default public S copyTransition(S trans, S succ) {
		return succ;
	}
	
	default public S addInitialState(boolean accepting) {
		S init = addState(accepting);
		setInitial(init, true);
		return init;
	}
	
	@Nonnull
	public S addState(boolean accepting);
	
	public void setAccepting(S state, boolean accepting);
}
