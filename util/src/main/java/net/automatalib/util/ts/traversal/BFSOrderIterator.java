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
package net.automatalib.util.ts.traversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.ts.simple.SimpleTS;
import net.automatalib.util.traversal.VisitedState;


public class BFSOrderIterator<S, I> implements Iterator<S> {
	
	private final Collection<? extends I> inputs;
	private final SimpleTS<S, I> ts;
	private final Queue<S> bfsQueue = new ArrayDeque<S>();
	private final MutableMapping<S,VisitedState> seen;
	
	public BFSOrderIterator(SimpleTS<S,I> ts, Collection<? extends I> inputs) {
		this.ts = ts;
		this.inputs = inputs;
		Collection<? extends S> initial = ts.getInitialStates();
		bfsQueue.addAll(initial);
		seen = ts.createStaticStateMapping();
		for(S state : initial)
			seen.put(state, VisitedState.VISITED);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return !bfsQueue.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public S next() {
		S state = bfsQueue.poll();
		
		for(I input : inputs) {
			Collection<? extends S> succs = ts.getSuccessors(state, input);
			for(S succ : succs) {
				if(seen.put(succ, VisitedState.VISITED) != VisitedState.VISITED)
					bfsQueue.add(succ);
			}
		}
		
		return state;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
