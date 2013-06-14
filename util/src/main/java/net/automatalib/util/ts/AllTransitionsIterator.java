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
package net.automatalib.util.ts;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.ts.TransitionSystem;


final class AllTransitionsIterator<S, I, T> implements Iterator<T> {
	private final TransitionSystem<S, I, T> ts;
	private final S state;
	private final Iterator<I> inputIt;
	private Iterator<T> transIt;
	
	public AllTransitionsIterator(TransitionSystem<S, I, T> ts, S state, Collection<I> inputs) {
		this.ts = ts;
		this.state = state;
		this.inputIt = inputs.iterator();
		findNext();
	}

	@Override
	public boolean hasNext() {
		return transIt.hasNext();
	}

	@Override
	public T next() {
		T t = transIt.next();
		if(!transIt.hasNext())
			findNext();
		return t;
	}

	@Override
	public void remove() {
		transIt.remove();
	}
	
	private void findNext() {
		while(inputIt.hasNext()) {
			I input = inputIt.next();
			Collection<T> trans = ts.getTransitions(state, input);
			if(trans == null || trans.isEmpty())
				continue;
			transIt = trans.iterator();
			break;
		}
	}
	
}
