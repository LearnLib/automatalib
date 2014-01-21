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

import net.automatalib.automata.concepts.TransitionOutput;
import net.automatalib.ts.TransitionPredicate;

import com.google.common.base.Predicate;

final class OutputSatisfies<S,I,T,O> implements TransitionPredicate<S, I, T> {

	private final TransitionOutput<? super T, ? extends O> transOut;
	private final Predicate<? super O> outputPred;
	private final boolean negate;
	
	public OutputSatisfies(TransitionOutput<? super T, ? extends O> transOut, Predicate<? super O> outputPred) {
		this(transOut, outputPred, false);
	}
	
	public OutputSatisfies(TransitionOutput<? super T,? extends O> transOut, Predicate<? super O> outputPred, boolean negate) {
		this.transOut = transOut;
		this.outputPred = outputPred;
		this.negate = negate;
	}

	@Override
	public boolean apply(S source, I input, T transition) {
		O out = transOut.getTransitionOutput(transition);
		return negate ^ outputPred.apply(out);
	}
}
