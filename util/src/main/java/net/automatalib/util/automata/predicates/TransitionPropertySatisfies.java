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
import net.automatalib.ts.UniversalTransitionSystem;

import com.google.common.base.Predicate;

final class TransitionPropertySatisfies<S, I, T, TP> implements
		TransitionPredicate<S, I, T> {
	
	private final UniversalTransitionSystem<?, ?, ? super T, ?, ? extends TP> uts;
	private final Predicate<? super TP> tpPred;

	public TransitionPropertySatisfies(UniversalTransitionSystem<?, ?, ? super T, ?, ? extends TP> uts,
			Predicate<? super TP> tpPred) {
		this.uts = uts;
		this.tpPred = tpPred;
	}
	
	@Override
	public boolean apply(S source, I input, T transition) {
		TP prop = uts.getTransitionProperty(transition);
		return tpPred.apply(prop);
	}

}
