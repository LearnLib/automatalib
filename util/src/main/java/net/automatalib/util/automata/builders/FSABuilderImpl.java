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
package net.automatalib.util.automata.builders;

import net.automatalib.automata.fsa.MutableFSA;

import com.github.misberner.duzzt.annotations.DSLAction;
import com.github.misberner.duzzt.annotations.GenerateEmbeddedDSL;

@GenerateEmbeddedDSL(name = "FSABuilder", enableAllMethods = false, includeInherited = true,
	syntax = "(((from (on <<to* loop? to*>>)+)+)|withAccepting|withInitial)* create")
class FSABuilderImpl<S, I, A extends MutableFSA<S, ? super I>> extends
		AutomatonBuilderImpl<S, I, S, Boolean, Void, A> {
	
	public FSABuilderImpl(A automaton) {
		super(automaton);
	}
	
	@DSLAction
	public void withAccepting(Object stateId) {
		S state = getState(stateId);
		automaton.setAccepting(state, true);
	}
}
