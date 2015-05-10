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
package net.automatalib.ts;


/**
 * Universal deterministic transition system.
 * 
 * @author Malte Isberner 
 *
 * @see UniversalTransitionSystem
 * @see DeterministicTransitionSystem
 */
public interface UniversalDTS<S, I, T, SP, TP> extends
		UniversalTransitionSystem<S, I, T, SP, TP>,
		DeterministicTransitionSystem<S, I, T> {
	
	/**
	 * Retrieves the transition property of the outgoing transition corresponding
	 * to the given state and input, if it exists. Otherwise, {@code null} is returned.
	 * <p>
	 * Note that this method alone is insufficient for determining whether or not a
	 * transition actually exists, as {@code null} might either be property of an existing
	 * transition, or indicate that the transition does not exist.
	 * 
	 * @param state the source state
	 * @param input the input symbol
	 * @return the property of the outgoing transition, or {@code null}
	 */
	default public TP getTransitionProperty(S state, I input) {
		T trans = getTransition(state, input);
		if (trans != null) {
			return getTransitionProperty(trans);
		}
		return null;
	}
}
