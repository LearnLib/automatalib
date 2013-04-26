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
package net.automatalib.automata.fsa.abstractimpl;

import java.util.Collection;

import net.automatalib.automata.fsa.NFA;


public abstract class AbstractNFA<S, I> extends AbstractFSA<S,I> implements
		NFA<S, I> {
	
	public static <S,I> boolean isAccepting(NFA<S,I> $this,
			Collection<? extends S> states) {
		if(states == null)
			return false;
		
		for(S state : states) {
			if($this.isAccepting(state))
				return true;
		}
		return false;
	}
	
	public static <S,I> boolean accepts(NFA<S,I> $this, Iterable<I> input) {
		Collection<S> states = $this.getStates(input);
		return $this.isAccepting(states);
	}
	

	@Override
	public boolean isAccepting(Collection<? extends S> states) {
		return isAccepting(this, states);
	}

	
	@Override
	public boolean accepts(Iterable<I> input) {
		return accepts(this, input);
	}

}