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
package net.automatalib.util.automata.predicates;

import net.automatalib.ts.TransitionPredicate;

import com.google.common.base.Predicate;

final class CompositeTransitionPredicate<S,I,T> implements TransitionPredicate<S, I, T> {
	
	private final Predicate<? super S> sourcePred;
	private final Predicate<? super I> inputPred;
	private final Predicate<? super T> transPred;
	
	public CompositeTransitionPredicate(Predicate<? super S> sourcePred, Predicate<? super I> inputPred, Predicate<? super T> transPred) {
		this.sourcePred = sourcePred;
		this.inputPred = inputPred;
		this.transPred = transPred;
	}

	@Override
	public boolean apply(S source, I input, T transition) {
		return sourcePred.apply(source) && inputPred.apply(input) && transPred.apply(transition);
	}

}
