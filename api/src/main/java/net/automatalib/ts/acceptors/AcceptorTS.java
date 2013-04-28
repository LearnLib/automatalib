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
package net.automatalib.ts.acceptors;

import net.automatalib.ts.UniversalTransitionSystem;

/**
 * A transition system whose semantics are defined by whether a state is "accepting"
 * or not.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class
 * @param <I> input symbol class
 * @param <T> transition class
 */
public interface AcceptorTS<S, I> extends UniversalTransitionSystem<S, I, S, Boolean, Void> {
	
	/**
	 * Checks whether the given state is accepting.
	 * @param state the state
	 * @return <code>true</code> if the state is accepting, <code>false</code>
	 * otherwise.
	 */
	public boolean isAccepting(S state);
	
	/**
	 * Determines whether the given input word is accepted by this acceptor.
	 * @param input the input word.
	 * @return <code>true</code> if the input word is accepted,
	 * <code>false</code> otherwise. 
	 */
	public boolean accepts(Iterable<I> input);
}
