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
package net.automatalib.ts.powerset;

import java.util.Collection;
import java.util.Collections;

import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.PowersetViewTS;
import net.automatalib.ts.abstractimpl.AbstractDTS;

public class DeterministicPowersetView<S, I, T> extends AbstractDTS<S, I, T>
		implements PowersetViewTS<S, I, T, S, T> {

	
	private final DeterministicTransitionSystem<S, I, T> delegate;
	
	public DeterministicPowersetView(DeterministicTransitionSystem<S, I, T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T getTransition(S state, I input) {
		return delegate.getTransition(state, input);
	}

	@Override
	public S getSuccessor(T transition) {
		return delegate.getSuccessor(transition);
	}

	@Override
	public S getInitialState() {
		return delegate.getInitialState();
	}

	@Override
	public Collection<? extends S> getOriginalStates(S state) {
		return Collections.singleton(state);
	}

	@Override
	public Collection<? extends T> getOriginalTransitions(T transition) {
		return Collections.singleton(transition);
	}

}
