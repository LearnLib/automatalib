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
package net.automatalib.util.ts.traversal;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.ts.TransitionSystem;

class SimpleDFRecord<S, I, T> {

	public final S state;

	private final Iterator<? extends I> inputsIterator;
	private I input;
	private Iterator<T> transitionIterator;
	private T retractedTransition;

	public SimpleDFRecord(S state, Collection<? extends I> inputs) {
		this.state = state;
		this.inputsIterator = inputs.iterator();
	}

	private void findNext(TransitionSystem<S,I,T> ts) {
		if(transitionIterator != null && transitionIterator.hasNext())
			return;
		while(inputsIterator.hasNext()) {
			input = inputsIterator.next();
			Collection<T> transitions = ts.getTransitions(state, input);
			if(transitions != null && !transitions.isEmpty()) {
				transitionIterator = transitions.iterator();
				break;
			}
		}
	}

	public boolean start(TransitionSystem<S, I, T> ts) {
		if(transitionIterator != null)
			return false;
		
		findNext(ts);
		return true;
	}

	public boolean hasNextTransition() {
		if(retractedTransition != null)
			return true;
		
		if(transitionIterator == null)
			return false;
		return transitionIterator.hasNext();
	}

	public void advance(TransitionSystem<S,I,T> ts) {
		if(transitionIterator.hasNext())
			return;
		findNext(ts);
	}

	public void advanceInput(TransitionSystem<S,I,T> ts) {
		transitionIterator = null;
		findNext(ts);
	}

	public I input() {
		return input;
	}

	public T transition() {
		return transitionIterator.next();
	}

}