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
package net.automatalib.ts.acceptors.abstractimpl;

import net.automatalib.ts.abstractimpl.AbstractDTS;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;

public abstract class AbstractDeterministicAcceptorTS<S, I> extends
		AbstractDTS<S, I, S> implements DeterministicAcceptorTS<S, I> {
	
	public static <S,I> boolean accepts(DeterministicAcceptorTS<S, I> $this, Iterable<I> input) {
		S state = $this.getState(input);
		if(state == null)
			return false;
		return $this.isAccepting(state);
	}


	@Override
	public boolean accepts(Iterable<I> input) {
		return accepts(this, input);
	}

}
