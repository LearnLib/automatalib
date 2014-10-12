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
package net.automatalib.ts.acceptors;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.UniversalDTS;

/**
 * A deterministic acceptor transition system.
 * 
 * @author Malte Isberner
 *
 * @see AcceptorTS
 * @see DeterministicTransitionSystem
 */
public interface DeterministicAcceptorTS<S, I> extends AcceptorTS<S, I>,
		UniversalDTS<S, I, S, Boolean, Void> {
	
	@Override
	default public boolean isAccepting(Collection<? extends S> states) {
		if (states.isEmpty()) {
			return false;
		}
		Iterator<?extends S> stateIt = states.iterator();
		assert stateIt.hasNext();
		
		S firstState = stateIt.next();
		if (stateIt.hasNext()) {
			throw new IllegalArgumentException("Acceptance of state sets is undefined for DFAs");
		}
		return isAccepting(firstState);
	}
	
	@Override
	default public boolean accepts(Iterable<? extends I> input) {
		S state = getState(input);
		if(state == null)
			return false;
		return isAccepting(state);
	}
}
