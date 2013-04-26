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

import java.util.Collection;

import net.automatalib.ts.abstractimpl.AbstractTS;
import net.automatalib.ts.acceptors.AcceptorTS;


public abstract class AbstractAcceptorTS<S, I> extends AbstractTS<S, I, S> implements
		AcceptorTS<S, I> {
	
	public static <S,I> boolean accepts(AcceptorTS<S, I> $this, Iterable<I> input) {
		Collection<S> states = $this.getStates(input);
		if(states == null)
			return false;
		
		for(S state : states) {
			if($this.isAccepting(state))
				return true;
		}
		
		return false;
	}


	public static <S,I> Boolean getStateProperty(AcceptorTS<S,I> $this, S state) {
		return Boolean.valueOf($this.isAccepting(state));
	}
	
	public static <S,I> Void getTransitionProperty(AcceptorTS<S,I> $this, S transition) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.acceptors.AcceptorTS#accepts(java.lang.Iterable)
	 */
	@Override
	public boolean accepts(Iterable<I> input) {
		return accepts(this, input);
	}
	
	@Override
	public Boolean getStateProperty(S state) {
		return getStateProperty(this, state);
	}
	
	@Override
	public Void getTransitionProperty(S transition) {
		return getTransitionProperty(this, transition);
	}
	
	
	

}
