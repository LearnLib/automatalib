/* Copyright (C) 2013-2015 TU Dortmund
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
package net.automatalib.automata.concepts;

public interface DetSuffixOutputAutomaton<S, I, T, D>
		extends DetOutputAutomaton<S,I,T,D>, SuffixOutput<I,D> {
	public D computeStateOutput(S state, Iterable<? extends I> input);
	
	@Override
	default public D computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
		return computeStateOutput(getState(prefix), suffix);
	}
	
	@Override
	default public D computeOutput(Iterable<? extends I> input) {
		return computeStateOutput(getInitialState(), input);
	}
}
