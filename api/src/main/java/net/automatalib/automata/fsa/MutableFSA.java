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
package net.automatalib.automata.fsa;

import net.automatalib.automata.MutableAutomaton;


/**
 *
 * @author fh
 */
public interface MutableFSA<S,I> extends FiniteStateAcceptor<S,I>,
		MutableAutomaton<S, I, S, Boolean, Void> {
	
	public S addState(boolean accepting);
	
	public S addInitialState(boolean accepting);

	public void setAccepting(S state, boolean accepting);
	
	public void flipAcceptance();
}
