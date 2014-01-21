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
package net.automatalib.util.automata.predicates;

import net.automatalib.automata.fsa.FiniteStateAcceptor;

import com.google.common.base.Predicate;

final class AcceptanceStatePredicate<S> implements Predicate<S> {
	private final FiniteStateAcceptor<? super S, ?> fsa;
	private final boolean acceptance;
	
	public AcceptanceStatePredicate(FiniteStateAcceptor<? super S, ?> fsa, boolean acceptance) {
		this.fsa = fsa;
		this.acceptance = acceptance;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.google.common.base.Predicate#apply(java.lang.Object)
	 */
	@Override
	public boolean apply(S state) {
		return fsa.isAccepting(state) == acceptance;
	}
	
	
}
